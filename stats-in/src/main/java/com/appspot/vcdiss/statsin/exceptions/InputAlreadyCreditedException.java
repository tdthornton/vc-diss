package com.appspot.vcdiss.statsin.exceptions;

/**
 * Exception indicating that the input sent to the service has already paid out its credits.
 */
public class InputAlreadyCreditedException extends Throwable {

    public InputAlreadyCreditedException(String message) {
        super(message);
    }
}
