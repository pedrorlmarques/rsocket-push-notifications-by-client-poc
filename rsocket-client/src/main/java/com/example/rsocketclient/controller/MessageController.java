package com.example.rsocketclient.controller;

import com.example.rsocketclient.dto.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.example.rsocketclient.config.RSocketConfiguration.clientId;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final RSocketRequester rSocketRequester;

    @GetMapping("/")
    public Mono<Message> retrieveMessage() {

        log.info("Connecting using client ID: {} and username: {}", clientId, "Test");

        return rSocketRequester
                .route("message")
                .data(Message.builder()
                        .id(UUID.randomUUID().toString())
                        .clientId(clientId)
                        .body("test")
                        .build())
                .retrieveMono(Message.class);
    }
}
