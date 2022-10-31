import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.List;

public class Request {
    private String method;
    private String path;
    private List<String> headers;
    private String body;
    private String query;


    public Request(String query) {
        this.query = query;
    }

    public Request(String method, String path, List<String> headers, String body, String query) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
        this.query = query;
    }

    public void getQueryParam(String query) {
        List<NameValuePair> nameValuePairs =
                URLEncodedUtils.parse(query, Charset.defaultCharset());
        System.out.println("Получаем Query: " + nameValuePairs);
        System.out.println("\nа теперь извлекаем Query parameters:\n");
        for (NameValuePair arg : nameValuePairs) {
            System.out.println("Name: " + arg.getName() + "\n" +
                    "Value: " + arg.getValue());
        }
    }

    public void getQueryParams() {
    }

    public String getQuery() {
        return query;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public String toString() {
        return "Метод запроса: " + method + "\n" +
                "Путь: " + path + "\n" +
                "Заголовки: " + headers + "\n" +
                "Тело запроса: " + body + "\n" +
                "Query: " + query;
    }
}
