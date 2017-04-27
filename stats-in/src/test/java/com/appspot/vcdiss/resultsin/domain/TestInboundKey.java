package com.appspot.vcdiss.resultsin.domain;

/**
 * Domain class for encapsulating the json sent to the service via the task queue.
 */
public class TestInboundKey {
    private String key;

    public TestInboundKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

}
