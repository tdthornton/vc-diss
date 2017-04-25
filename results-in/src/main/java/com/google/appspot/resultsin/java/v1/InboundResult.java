package com.google.appspot.resultsin.java.v1;

/**
 * Domain class for encapsulating inbound JSON that holds information on a computed result.
 */
public class InboundResult {
    private Long input;
    private String result;

    public Long getInput() {
        return input;
    }

    public void setInput(Long input) {
        this.input = input;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
