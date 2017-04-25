package com.appspot.vcdiss.workout;


/**
 * Domain class to encapsulate outbound work, instantiated from either a datastore entity or raw data.
 */
public class WorkOut {
    private String codeHash;
    private String input;

    public WorkOut(String codeHash, String input) {
        this.codeHash = codeHash;
        this.input = input;
    }

    @Override
    public String toString() {
        return "WorkOut{" +
                "codeHash='" + codeHash + '\'' +
                ", input='" + input + '\'' +
                '}';
    }

    public String getInput() {
        return input;
    }

    public String getCodeHash() {
        return codeHash;
    }
}
