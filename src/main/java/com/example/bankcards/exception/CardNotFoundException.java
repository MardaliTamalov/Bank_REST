package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException {

    private String message;

    public CardNotFoundException(String message) {super(message);}
}
