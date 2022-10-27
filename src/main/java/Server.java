import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> allowedMethods = List.of("GET", "POST");

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>>
            allHandlers = new ConcurrentHashMap<String, ConcurrentHashMap<String, Handler>>();

    public void addHandler(String method, String path, Handler handler) {
        var methodMap = allHandlers.get(method);
        if (allHandlers.get(method) == null) {
            methodMap = new ConcurrentHashMap<>();
            allHandlers.put(method, methodMap);
        }
        methodMap.put(path, handler);
    }

    public void listen(int port) {
        if (port <= 0) throw new IllegalArgumentException("Порт должен быть больше нуля");
        final ExecutorService threadPool = Executors.newFixedThreadPool(64);
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {
        try (var in = new BufferedInputStream(socket.getInputStream());
             var out = new BufferedOutputStream(socket.getOutputStream())) {

            System.out.println(Thread.currentThread().getName());

            final var limit = 4096;

            in.mark(limit);
            final var buffer = new byte[limit];
            final var read = in.read(buffer);
            // ищем request line
            final var requestLineDelimiter = new byte[]{'\r', '\n'};
            final var requestLineEnd =
                    indexOf(buffer, requestLineDelimiter, 0, read);
            if (requestLineEnd == -1) {
                badRequest(out);
                return;
            }
            // читаем request line
            final var requestLine =
                    new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
                return;
            }
            System.out.println("Получили реквест лайн: " + Arrays.toString(requestLine));

            // вытаскиваем метод из request line
            final var method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
                return;
            }
            // вытаскиваем путь из request line
            final var path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                return;
            }
            // заголовки
            final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            final var headersStart = requestLineEnd + headersDelimiter.length;
            final var headersEnd =
                    indexOf(buffer, headersDelimiter, headersStart, read);
            if (headersEnd == -1) {
                badRequest(out);
                return;
            }
            // отматываем на начало буфера
            in.reset();
            // пропускаем request line
            in.skip(headersStart);
            final var headersBytes = in.readNBytes(headersEnd - headersStart);
            final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

            final var request = new Request(method, path, headers, null);
            System.out.println("Итого получился объект реквест: " + request);

            // тело запроса, для GET тела нет
            if (!method.equals("GET")) {
                in.skip(headersDelimiter.length);
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);
                    final var body = new String(bodyBytes);
                    System.out.println(body);
                }
            }

            var methodMap =
                    allHandlers.get(request.getMethod());
            System.out.println("мапа методмап: " + methodMap);

            if (methodMap == null) {
                notFound(out);
                return;
            }

            var handler = methodMap.get(request.getPath());
            System.out.println("хендлер: " + handler);

            if (handler == null) {
                notFound(out);
                return;
            }

            handler.handle(request, out);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void notFound(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found!\r\n" +
                        "Content-Length: 5\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    private Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }
}
