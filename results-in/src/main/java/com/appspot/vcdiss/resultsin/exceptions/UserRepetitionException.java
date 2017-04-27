package com.appspot.vcdiss.resultsin.exceptions;

/**
 * Exception indicating that a result was received from a user who had already submitted a result to that input.
 */
public class UserRepetitionException extends Exception {
    public UserRepetitionException(String message) {
        super(message);
    }
}
