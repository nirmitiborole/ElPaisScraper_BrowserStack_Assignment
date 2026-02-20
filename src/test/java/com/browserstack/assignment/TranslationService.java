package com.browserstack.assignment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

public class TranslationService {

    private String apiKey;
    private String apiHost;
    private String apiUrl;
    private final HttpClient client;

    public TranslationService() {
        this.client = HttpClient.newHttpClient();
        loadConfig();
    }

    private void loadConfig() {
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            prop.load(input);
            this.apiKey = prop.getProperty("rapidapi.key");
            this.apiHost = prop.getProperty("rapidapi.host");
            this.apiUrl = prop.getProperty("rapidapi.url");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String translate(String text) {
        if (text == null || text.trim().isEmpty()) return "";

        try {
            String jsonBody = String.format(
                    "{\"from\":\"es\",\"to\":\"en\",\"json\":{\"title\":\"%s\"}}",
                    escapeJson(text)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("content-type", "application/json")
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", apiHost)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                int start = body.indexOf("\"title\":\"") + 9;
                int end = body.indexOf("\"", start);
                if (start > 9 && end > start) {
                    return body.substring(start, end);
                }
            } else {
                System.err.println("Translation API Failed: " + response.statusCode());
            }

        } catch (Exception e) {
            System.err.println("Translation Error: " + e.getMessage());
        }

        return text;
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}