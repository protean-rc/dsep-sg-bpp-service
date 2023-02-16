package com.protean.dsep.bpp.sender;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class Sender {

	private final WebClient webClient;

	public Sender(WebClient.Builder webClientBuilder) {
		this.webClient = webClientBuilder
				.build();
	}

	public void send(String url, HttpHeaders headers, String json) {

		log.info("calling bap url {}", url);
		try {
			Mono<String> response = this.webClient.post()
					.uri(url)
					.headers(h -> {
						h.addAll(headers);
					})
					.contentType(MediaType.APPLICATION_JSON)
					.body(Mono.just(json), String.class)
					.retrieve()
					.bodyToMono(String.class);

			String responseData = response.block();
			log.info("response from post call: {}", responseData);

		} catch (Exception e) {
			log.error("Exception while calling the post at url {}", url);
			log.error("Exception is {}", e.getMessage());
		}

	}

}
