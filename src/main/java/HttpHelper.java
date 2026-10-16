import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import io.qameta.allure.Attachment;
import io.qameta.allure.Step;

public class HttpHelper {
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Отправляет GET-запрос по указанному URL и возвращает тело ответа в виде строки
     */
    @Step("Выполнение HTTP GET запроса на URL: {0}")
    public String sendGetRequest(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        logStatusCode(response.statusCode());

        attachResponseBody(response.body());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP GET-запрос завершился ошибкой. Статус-код: " + response.statusCode());
        }

        return response.body();
    }

    @Step("Получен статус-код ответа: {statusCode}")
    private void logStatusCode(int statusCode) {
    }

    @Attachment(value = "Тело ответа (JSON)", type = "application/json")
    private String attachResponseBody(String responseBody) {
        return responseBody;
    }
}
