package com.metrocard.exceptions;

public class RateNotDefinedException extends RuntimeException{
    public RateNotDefinedException(String message) {
        super(message);
    }
}
