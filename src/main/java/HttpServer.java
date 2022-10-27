import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class HttpServer {
    private final List<String> validPaths = List.of("/index.html", "/spring.svg",
            "/spring.png", "/resources.html", "/styles.css",
            "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);
    private final int port;

    public HttpServer(int port) { this.port = port;}

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                System.out.println("\nСервак стартовал");
                Socket socket = serverSocket.accept();
                    threadPool.execute(() -> handle(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handle (Socket socket) {
        try (var in = new BufferedReader((new InputStreamReader(socket.getInputStream())));
        var out = new BufferedOutputStream(socket.getOutputStream());
        socket) {
            final var requestLine = in.readLine();
            System.out.printf("Поток %s обрабатывает запрос: %s%n", Thread.currentThread().getName(), requestLine);
            final var parts = requestLine.split(" ");
            if (parts.length != 3) return;

            final var path = parts[1];
            if (!validPaths.contains(path)) {
                out.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                                "Content-Length: 0\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.flush();
                return;
            }

            final var filePath = Path.of(".", "public", path);
            System.out.println("Путь к файлу: " + filePath);
            final var mimeType = Files.probeContentType(filePath);
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
//                            "Content-Type: " + "text/plain" + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);//копирование байтов из файла в поток
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class MainServer {
    public static void main(String[] args) {
        int serverPort = 9999;
        HttpServer httpServer = new HttpServer(serverPort);
        httpServer.start();
    }
}

