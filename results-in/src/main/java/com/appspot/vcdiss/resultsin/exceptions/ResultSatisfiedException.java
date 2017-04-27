package com.appspot.vcdiss.resultsin.exceptions;

/**
 * Exception indicating that a result was received for an input that was already satisfied.
 */
public class ResultSatisfiedException extends Exception {
    public ResultSatisfiedException(String message) {
        super(message);
    }
}
