package com.appspot.vcdiss.resultsin.domain;

/**
 * Domain class for incapsulating the inbound json of a new result.
 */
public class TestInboundResult {
    private long input;
    private String result;

    public TestInboundResult(long input, String result) {
        this.input = input;
        this.result = result;
    }

    @Override
    public String toString() {
        return "TestInboundResult{" +
                ", input='" + input + '\'' +
                ", result='" + result + '\'' +
                '}';
    }

    public long getInput() {
        return input;
    }

    public void setInput(long input) {
        this.input = input;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
