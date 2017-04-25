package com.appspot.vcdiss.resultsin;

/**
 * Domain class for encapsulating outbound JSON that holds information on an input that has been satisfied, and needs crediting.
 */
public class OutboundStat {
    private String key;

    public OutboundStat(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "OutboundStat{" +
                "key='" + key + '\'' +
                '}';
    }
}
