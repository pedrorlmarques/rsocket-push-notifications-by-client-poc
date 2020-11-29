package com.example.rsocketserver.controller;


import com.example.rsocketserver.dto.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class MessageRouterController {

    private static final Map<String, RSocketRequester> REQUESTER_MAP = new HashMap<>();

    @ConnectMapping("client-id")
    public void onConnect(RSocketRequester rSocketRequester, @Payload String clientId) {
        rSocketRequester
                .rsocket()
                .onClose()
                .doFirst(() -> {
                    // Add all new clients to a client list
                    log.info("Client: {} CONNECTED.", clientId);
                    REQUESTER_MAP.put(clientId, rSocketRequester);
                })
                .doOnError(error -> {
                    // Warn when channels are closed by clients
                    log.warn("Channel to client {} CLOSED", clientId);
                })
                .doFinally(consumer -> {
                    // Remove disconnected clients from the client list
                    log.info("Client {} DISCONNECTED", clientId);
                    REQUESTER_MAP.remove(clientId, rSocketRequester);
                }).subscribe();
    }

    @MessageMapping("message")
    public void retrieveMessage(Message message) {

        /**
         REQUESTER_MAP.get(message.getClientId())
         .route("client-status")
         .data("OPEN")
         .retrieveFlux(String.class)
         .doOnNext(s -> log.info("Client: {} Free Memory: {}.", message.getClientId(), s))
         .subscribe();
         **/

        REQUESTER_MAP.get(message.getClientId())
                .route("notify")
                .data(message)
                .send()
                .subscribe();
    }

    @PreDestroy
    void shutdown() {
        log.info("Detaching all remaining clients...");
        REQUESTER_MAP.values().forEach(requester -> requester.rsocket().dispose());
        log.info("Shutting down.");
    }
}
