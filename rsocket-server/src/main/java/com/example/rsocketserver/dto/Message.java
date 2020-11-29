package com.example.rsocketserver.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Message {

    String clientId;
    String id;
    String body;
}
