package com.appspot.vcdiss.resultsin.exceptions;

/**
 * Exception indicating a result from a user who was never sent the relevant input.
 */
public class PotentialFoulPlayException extends Exception {
    public PotentialFoulPlayException(String message) {
        super(message);
    }
}
