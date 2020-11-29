package com.example.rsocketclient;

import com.example.rsocketclient.dto.Message;
import io.rsocket.SocketAcceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class RSocketConfiguration {

    public static final String clientId = UUID.randomUUID().toString();

    @Bean
    RSocketRequester rSocketRequester(RSocketRequester.Builder builder, RSocketStrategies rSocketStrategies) {

        SocketAcceptor responder = RSocketMessageHandler.responder(rSocketStrategies, new ClientHandler());

        return builder
                .setupRoute("client-id")
                .setupData(clientId)
                .rsocketConnector(connector -> connector.acceptor(responder))
                .tcp("localhost", 8888);
    }

    @Slf4j
    private static class ClientHandler {

        @MessageMapping("client-status")
        public Flux<String> statusUpdate(String status) {
            log.info("Connection {}", status);
            return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
        }

        @MessageMapping("notify")
        public Mono<Void> notify(Mono<Message> message) {
            return message
                    .doOnNext(m -> log.info("Notification Received: {}", m))
                    .then();
        }
    }
}
