import com.github.tomakehurst.wiremock.client.WireMock;
import dto.Course;
import dto.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiremock.com.fasterxml.jackson.core.type.TypeReference;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class WiremockTest {
    private static final String WIREMOCK_HOST = "wiremock.local";
    private static final int WIREMOCK_PORT = 80;

    @BeforeAll
    public static void setup() {
        // Подключаем Java-клиент к WireMock, работающему в Minikube
        WireMock.configureFor(WIREMOCK_HOST, WIREMOCK_PORT);
    }

    @Test
    public void testWiremockInMinikube() throws Exception {
        HttpHelper httpHelper = new HttpHelper();

        // Создаем стаб (маппинг) удаленно внутри Minikube
        stubFor(WireMock.get(urlEqualTo("/api/v1/user/get/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"name\": \"Test user\", \"score\": 11}")));

        // Выполняем HTTP GET-запрос через HttpHelper к сервису в Minikube
        String jsonResponse = httpHelper.sendGetRequest("http://" + WIREMOCK_HOST + ":" + WIREMOCK_PORT + "/api/v1/user/get/1");
        assertNotNull(jsonResponse, "Ответ от WireMock не должен быть пустым");

        // Проверяем ответ
        assertEquals("{\"name\": \"Test user\", \"score\": 11}", jsonResponse);

        // Верифицируем, что запрос действительно дошел до WireMock
        verify(getRequestedFor(urlEqualTo("/api/v1/user/get/1")));
    }
    @Test
    public void testGetUserFromMinikubeStub() throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonResponse = httpHelper.sendGetRequest("http://" + WIREMOCK_HOST + ":" + WIREMOCK_PORT + "/api/v1/user/get/all");
        assertNotNull(jsonResponse, "Ответ от WireMock не должен быть пустым");

        // JSON-строку преобразуем в Java-объект User
        User user = objectMapper.readValue(jsonResponse, User.class);

        // Проверяем, что поля объекта полностью соответствуют данным из стаба в Minikube
        assertEquals("Test user", user.name());
        assertEquals("QA", user.course());
        assertEquals("test@test.test", user.email());
        assertEquals(23, user.age());
    }

    @Test
    public void testGetCoursesFromMinikubeStub() throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonResponse = httpHelper.sendGetRequest("http://" + WIREMOCK_HOST + ":" + WIREMOCK_PORT + "/api/v1/course/get/all");

        assertNotNull(jsonResponse, "Ответ от WireMock не должен быть пустым");
        List<Course> courses = objectMapper.readValue(jsonResponse, new TypeReference<List<Course>>() {});

        assertNotNull(courses, "Список курсов не должен быть null");
        assertEquals(2, courses.size(), "Количество курсов в ответе не совпадает");

        // Проверка первого элемента
        Course firstCourse = courses.get(0);
        assertEquals("QA java", firstCourse.name());
        assertEquals(15000, firstCourse.price());

        // Проверка второго элемента
        Course secondCourse = courses.get(1);
        assertEquals("Java", secondCourse.name());
        assertEquals(12000, secondCourse.price());
    }

}
