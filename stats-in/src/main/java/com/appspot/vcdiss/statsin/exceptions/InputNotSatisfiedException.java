package com.appspot.vcdiss.statsin.exceptions;

/**
 * An exception indicating that the input sent to the service is not yet ready for crediting.
 */
public class InputNotSatisfiedException extends Throwable {

    public InputNotSatisfiedException(String message) {
        super(message);
    }
}
