package by.mpkrepak.btkatsserver.service;

import lombok.Data;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

public class CookieFetcher {

    private final RestTemplate restTemplate;

    public CookieFetcher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchCookie(String url, String pbxId, String username, String password) {
        try {
            // Создание JSON-объекта
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(new RequestBody(pbxId, username, password));

            // Настройка заголовков
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // Добавление заголовка Accept

            // Создание HTTP-запроса
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            // Отправка POST-запроса
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Получение значения Set-Cookie из заголовков
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (cookies != null && !cookies.isEmpty()) {
                for (var cookie : cookies) {
                    if (cookie.startsWith("jwt-token")) {
                        return cookie;
                    }
                }
                System.err.println("JWT-токен не найден.");
                return null;
            } else {
                System.err.println("Заголовок Set-Cookie не найден.");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
            return null;
        }
    }
    @Data
    private static class RequestBody {
        private String pbxId;
        private String username;
        private String password;

        public RequestBody(String pbxId, String username, String password) {
            this.pbxId = pbxId;
            this.username = username;
            this.password = password;
        }
    }
}