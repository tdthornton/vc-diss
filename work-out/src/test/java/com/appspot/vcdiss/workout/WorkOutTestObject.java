package com.appspot.vcdiss.workout;

/**
 * Created by Tom on 25/03/2017.
 */
public class WorkOutTestObject {
    private String codeHash;
    private String input;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkOutTestObject that = (WorkOutTestObject) o;

        if (!codeHash.equals(that.codeHash)) return false;
        return input.equals(that.input);

    }

    @Override
    public int hashCode() {
        int result = codeHash.hashCode();
        result = 31 * result + input.hashCode();
        return result;
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

    public void setInput(String input) {
        this.input = input;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }
}
