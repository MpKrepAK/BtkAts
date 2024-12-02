package by.mpkrepak.btkatsserver.service;

import by.mpkrepak.btkatsserver.domain.Record;
import by.mpkrepak.btkatsserver.repository.RecordsRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class ScheduledService {
    private final RestTemplate restTemplate;
    private final RecordsRepository recordsRepository;
    @Value("${settings.url}")
    private String url;
    @Value("${settings.request_body}")
    private String body;
    @Value("${settings.download_path}")
    private String downloadPath;


    @Value("${settings.login_url}")
    private String loginUrl;
    @Value("${settings.login_data.pbxId}")
    private String pbxId;
    @Value("${settings.login_data.username}")
    private String username;
    @Value("${settings.login_data.password}")
    private String password;
    private String cookie;

    @Scheduled(fixedRate = 1800000)
    public void executeTask() {
        CookieFetcher cookieFetcher = new CookieFetcher(restTemplate);
        cookie = cookieFetcher.fetchCookie(loginUrl, pbxId, username, password);
        if (cookie == null) {
            System.err.println("-----------------Авторизация не удалась!!!!" + "\t" + new Date());
            return;
        }

        Date date = new Date();
        System.out.println("Начат запрос данных - " + date);
        int totalRequests = 150;
        List<Record> records = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", cookie);
        var lastRecord = recordsRepository.findFirstByOrderByIdDesc().orElse(null);
        for (int i = 0; i < totalRequests; i++) {
            try {
                String requestBody = body.replace("#first", String.valueOf(i))
                        .replace("#count", "1");

                HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode recordsNode = rootNode.path("records");

                if (recordsNode.isArray() && recordsNode.size() > 0) {
                    Record record = objectMapper.treeToValue(recordsNode.get(0), Record.class);
                    if (lastRecord!=null && lastRecord.getPath().equals(record.getPath())) {
                        break;
                    }
                    //System.out.println(record.getCaller() + " - " + record.getDuration());
                    record.getPath();
                    records.add(record);
                } else {
                    //System.err.println("Запись не найдена для индекса: " + i);
                }

            } catch (Exception e) {
                System.err.println("Ошибка при получении записи для индекса " + i + ": " + e.getMessage());
            }
        }

        System.out.println("Общее количество записей: " + records.size());
        downloadFiles(records);
        recordsRepository.saveAll(records.reversed());
        date = new Date();
        System.out.println("Завершен запрос данных - " + date);
    }

    public void downloadFiles(List<Record> records) {
        Date date = new Date();
        System.out.println("Начато скачивание файлов - " + date);
        Path path = Paths.get(downloadPath);
        try {
            Files.createDirectories(path);
            //System.out.println("Директория создана: " + path.toString());
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию: " + e.getMessage());
        }

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", cookie);

        for (Record record : records) {
            executorService.submit(() -> {
                String filePath = record.getPath();
                String fileUrl = url + "/download?fileName=" + filePath;

                try {
                    HttpEntity<String> entity = new HttpEntity<>(headers);
                    ResponseEntity<byte[]> response = restTemplate.exchange(fileUrl, HttpMethod.GET, entity, byte[].class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
                        File outputFile = new File(downloadPath + fileName);

                        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                            outputStream.write(response.getBody());
                            System.out.println("Файл скачан: " + outputFile.getAbsolutePath());
                        }
                    } else {
                        System.err.println("Ошибка при скачивании файла: " + response.getStatusCode());
                    }
                } catch (IOException e) {
                    System.err.println("Ошибка при скачивании файла: " + e.getMessage());
                }
            });
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {

        }
        date = new Date();
        System.out.println("Скачивание файлов завершено - " + date);
    }

}
