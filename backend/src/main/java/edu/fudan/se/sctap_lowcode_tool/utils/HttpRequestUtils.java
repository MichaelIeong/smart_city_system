package edu.fudan.se.sctap_lowcode_tool.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
public class HttpRequestUtils {

    public static void post(String url, String jsonBody) {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("POST request to {} returned status code {}", url, response.statusCode());
        } catch (Exception e) {
            log.error("Failed to send POST request to {}", url, e);
        }
    }


}
