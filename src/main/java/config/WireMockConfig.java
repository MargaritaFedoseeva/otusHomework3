package config;

import java.util.Optional;

public class WireMockConfig {
    // Константы по умолчанию (для локального запуска)
    private static final String DEFAULT_WIREMOCK_HOST = "wiremock.local";
    private static final int DEFAULT_WIREMOCK_PORT = 80;

    public static String getWiremockHost() {
        // Ищет переменную WIREMOCK_HOST в Jenkins. Если её нет - берет дефолт
        return Optional.ofNullable(System.getenv("WIREMOCK_HOST"))
                .orElse(DEFAULT_WIREMOCK_HOST);
    }

    public static int getWiremockPort() {
        // Ищет переменную WIREMOCK_PORT, безопасно переводит в число
        return Optional.ofNullable(System.getenv("WIREMOCK_PORT"))
                .map(Integer::parseInt)
                .orElse(DEFAULT_WIREMOCK_PORT);
    }
}
