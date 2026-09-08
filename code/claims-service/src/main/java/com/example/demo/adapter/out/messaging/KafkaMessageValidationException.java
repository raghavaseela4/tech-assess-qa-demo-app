package com.example.demo.adapter.out.messaging;

public class KafkaMessageValidationException extends RuntimeException {

    public KafkaMessageValidationException(String message) {
        super(message);
    }

    public KafkaMessageValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
