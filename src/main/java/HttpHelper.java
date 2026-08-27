import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpHelper {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Отправляет GET-запрос по указанному URL и возвращает тело ответа в виде строки
     */
    public String sendGetRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP GET-запрос завершился ошибкой. Статус-код: " + response.statusCode());
        }

        return response.body();
    }
}
