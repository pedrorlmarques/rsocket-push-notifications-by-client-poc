package com.example.rsocketclient.dto;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@ToString
public class Message {

    String clientId;
    String id;
    String body;
}
