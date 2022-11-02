import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Request {
    private final String method;
    private final String path;
    private final List<String> headers;
    private final List<String> bodyList;
    private final String query;

    public Request(String method, String path, List<String> headers, List<String> bodyList, String query) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.bodyList = bodyList;
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

    public List<String> getQueryParams() {
        List<String> queryList = new ArrayList<>();
        List<NameValuePair> nameValuePairs =
                URLEncodedUtils.parse(query, Charset.defaultCharset());
        for (NameValuePair arg : nameValuePairs) {
            queryList.add(arg.getName());
            queryList.add(arg.getValue());
        }
        return queryList;
    }

    public List<String> getPostParams() {
        List<String> postParams = new ArrayList<>();
        for (int i = 0; i < bodyList.size() - 1; i++) {
            String[] pair = bodyList.get(i).split("=");
            postParams.addAll(Arrays.asList(pair));
        }
        return postParams;
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

    public List<String> getBody() {
        return bodyList;
    }

    public String toString() {
        return "Метод запроса: " + method + "\n" +
                "Путь: " + path + "\n" +
                "Заголовки: " + headers + "\n" +
                "Тело запроса: " + bodyList + "\n" +
                "Query: " + query;
    }
}
