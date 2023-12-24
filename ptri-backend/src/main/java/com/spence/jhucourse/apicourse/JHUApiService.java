package com.spence.jhucourse.apicourse;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Flux;

@Service
public class JHUApiService {
    private final WebClient webClient;

    public JHUApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<JHUApiCourse> makeApiCall(String apiKey) {
        UriComponents uriComponents = UriComponentsBuilder
                .fromUriString("https://sis.jhu.edu/api/classes/{school}/{department}/{term}")
                .queryParam("key", apiKey)
                .buildAndExpand("Whiting School of Engineering", "EN Computer Science", "Fall 2023");
        // uriComponents.toUri()

        Flux<JHUApiCourse> response = webClient.get()
                .uri(uriComponents.toUriString())
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToFlux(JHUApiCourse.class);

        return response;
    }

}
