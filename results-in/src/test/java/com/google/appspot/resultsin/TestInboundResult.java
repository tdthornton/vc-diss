package com.google.appspot.resultsin;

import com.google.appengine.api.datastore.Key;

/**
 * Created by Tom on 13/03/2017.
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
