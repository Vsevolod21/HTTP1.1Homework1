import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

public class UrlParsing {
    public static void main(String[] args) throws URISyntaxException {
        String url1 = "localhost:9999/messages?actionParameter=hello";
        String url2="http://mysite.com/index.php?name=john&id=42";
        List<NameValuePair> nameValuePairs =
                URLEncodedUtils.parse(url2, Charset.defaultCharset());
        System.out.println(nameValuePairs);
        for (NameValuePair arg : nameValuePairs) {
                System.out.println(arg.getValue());
        }

        String url3 = "http://www.example.com/something.html" +
                "?one=1&two=2&three=3&three=3a";
        List<NameValuePair> params = URLEncodedUtils.parse(
                new URI(url2), "UTF-8");
        System.out.println(params);
        for (NameValuePair param : params) {
            System.out.println(param.getName() + " : " + param.getValue());
        }
    }
}
