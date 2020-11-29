package com.example.rsocketserver.controller;

import com.example.rsocketserver.dto.Message;
import io.rsocket.transport.netty.client.TcpClientTransport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MessageRouterControllerTest {

    private static final String clientId = UUID.randomUUID().toString();
    private static RSocketRequester requester;

    @BeforeAll
    public static void setupOnce(@Autowired RSocketRequester.Builder builder, @Value("${spring.rsocket.server.port}") Integer port) {
        requester = builder
                .setupRoute("client-id")
                .setupData(clientId)
                .connect(TcpClientTransport.create(port))
                .block();
    }

    @Test
    public void testRequestGetsResponse() {

        var messageId = UUID.randomUUID().toString();
        var body = "test";

        // Send a request message (1)
        Mono<Message> result = requester
                .route("message")
                .data(Message.builder()
                        .id(messageId)
                        .clientId(clientId)
                        .body(body)
                        .build())
                .retrieveMono(Message.class);

        // Verify that the response message contains the expected data (2)
        StepVerifier
                .create(result)
                .consumeNextWith(message -> {
                    assertThat(message.getBody()).isEqualTo(body);
                    assertThat(message.getClientId()).isEqualTo(clientId);
                    assertThat(message.getId()).isEqualTo(messageId);
                }).verifyComplete();
    }
}
