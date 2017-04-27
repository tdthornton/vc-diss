package com.appspot.vcdiss.workout.exceptions;

/**
 * Exception indicating a user whose crunching is currently paused attempted to get work.
 */
public class WorkPausedException extends Exception {
    public WorkPausedException(String message) {
        super(message);
    }
}
