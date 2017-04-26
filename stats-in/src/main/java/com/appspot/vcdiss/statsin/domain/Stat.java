package com.appspot.vcdiss.statsin.domain;

/**
 * Domain class to encapsulate the inbound JSON from the queue, detailing inputs to credit by their key.
 */
public class Stat {
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "Stat{" +
                "key='" + key + '\'' +
                '}';
    }
}
