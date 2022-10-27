import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        server.addHandler("GET", "/messages", (request, out) -> {
            out.write((
                    "HTTP/1.1 201 Created\r\n" +
                            "Content-Type: text/plain" + "\r\n" +
                            "Content-Length: " + 17 + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n" +
                            "Hello from my GET!"
            ).getBytes());
            out.flush();
        });

        server.addHandler("POST", "/messages", (request, out) -> {
            out.write((
                    "HTTP/1.1 202 Accepted\r\n" +
                            "Content-Type: " + 0 + "\r\n" +
                            "Content-Length: " + 0 + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        });

        server.listen(9999);
    }
}
