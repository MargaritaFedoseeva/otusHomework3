import com.github.tomakehurst.wiremock.client.WireMock;
import config.WireMockConfig;
import dto.Course;
import dto.User;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wiremock.com.fasterxml.jackson.core.type.TypeReference;
import wiremock.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Epic("WireMock")
@Feature("Выполнение запросов")
public class WiremockTest {
    private static final String WIREMOCK_HOST = "wiremock.local";
    private static final int WIREMOCK_PORT = 80;

    @BeforeAll
    public static void setup() {
        // Подключаем Java-клиент к WireMock, работающему в Minikube
        RestAssured.filters(new AllureRestAssured());
        WireMock.configureFor(WireMockConfig.getWiremockHost(), WireMockConfig.getWiremockPort());
    }

    @Test
    @Story("Проверка интеграции с WireMock в Minikube")
    @Description("Удаленное создание стаба в Minikube и проверка отправки GET-запроса через HttpHelper")
    public void testWiremockInMinikube() throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        String url = String.format("http://%s:%d/api/v1/user/get/1",
                WireMockConfig.getWiremockHost(),
                WireMockConfig.getWiremockPort());

        // Создаем стаб (маппинг) удаленно внутри Minikube
        stubFor(WireMock.get(urlEqualTo("/api/v1/user/get/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"name\": \"Test user\", \"score\": 11}")));

        // Выполняем HTTP GET-запрос через HttpHelper к сервису в Minikube
        String jsonResponse = httpHelper.sendGetRequest(url);
        assertNotNull(jsonResponse, "Ответ от WireMock не должен быть пустым");

        // Проверка ответа
        assertEquals("{\"name\": \"Test user\", \"score\": 11}", jsonResponse);

        // Подтверждение, что запрос действительно дошел до WireMock
        verify(getRequestedFor(urlEqualTo("/api/v1/user/get/1")));
    }
    @Test
    @Story("Получение данных пользователя")
    @Description("Проверка парсинга JSON-ответа от стаба WireMock в Java-объект User и валидация его полей (name, course, email, age)")
    public void testGetUserFromMinikubeStub() throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        ObjectMapper objectMapper = new ObjectMapper();
        String url = String.format("http://%s:%d/api/v1/user/get/all",
                WireMockConfig.getWiremockHost(),
                WireMockConfig.getWiremockPort());

        String jsonResponse = httpHelper.sendGetRequest(url);

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
    @Story("Получение списка курсов")
    @Description("Запрос списка доступных курсов из стаба и проверка корректности структуры массива, цен и названий")
    public void testGetCoursesFromMinikubeStub() throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        ObjectMapper objectMapper = new ObjectMapper();
        String url = String.format("http://%s:%d/api/v1/course/get/all",
                WireMockConfig.getWiremockHost(),
                WireMockConfig.getWiremockPort());

        String jsonResponse = httpHelper.sendGetRequest(url);

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
