package com.appspot.vcdiss.resultsin;

import com.google.appengine.api.datastore.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Tom on 15/03/2017.
 */
public class TestInputResult {
    String input;
    List<String> resultsFrom = new ArrayList<>();
    List<String> distributedTo = new ArrayList<>();
    List<String> results = new ArrayList<>();
    Key work;
    boolean canonical;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestInputResult that = (TestInputResult) o;

        if (canonical != that.canonical) return false;
        if (!input.equals(that.input)) return false;
        if (resultsFrom != null ? !resultsFrom.equals(that.resultsFrom) : that.resultsFrom != null) return false;
        if (distributedTo != null ? !distributedTo.equals(that.distributedTo) : that.distributedTo != null)
            return false;
        if (results != null ? !results.equals(that.results) : that.results != null) return false;
        return work.equals(that.work);

    }

    @Override
    public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + (resultsFrom != null ? resultsFrom.hashCode() : 0);
        result = 31 * result + (distributedTo != null ? distributedTo.hashCode() : 0);
        result = 31 * result + (results != null ? results.hashCode() : 0);
        result = 31 * result + work.hashCode();
        result = 31 * result + (canonical ? 1 : 0);
        return result;
    }

    public TestInputResult(String input, String[] namesToAdd, String[] resultsToAdd, String[] distributedToAdd, Key work, boolean canonical) {
        for (int i = 0; i < namesToAdd.length; i++) {
            resultsFrom.add(namesToAdd[i]);
        }
        for (int i = 0; i < resultsToAdd.length; i++) {
            results.add(resultsToAdd[i]);
        }
        for (int i = 0; i < distributedToAdd.length; i++) {
            distributedTo.add(distributedToAdd[i]);
        }
        this.input = input;
        this.work = work;
        this.canonical = canonical;
    }

    public TestInputResult(Map<String, Object> properties) {
        this.input= String.valueOf(properties.get("input"));
        this.resultsFrom= (List<String>) properties.get("resultsFrom");
        this.distributedTo= (List<String>) properties.get("distributedTo");
        this.results= (List<String>) properties.get("results");
        this.work = (Key) properties.get("app");
        this.canonical = (boolean) properties.get("canonical");
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public List<String> getResultsFrom() {
        return resultsFrom;
    }

    public void setResultsFrom(List<String> resultsFrom) {
        this.resultsFrom = resultsFrom;
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public Key getWork() {
        return work;
    }

    public void setWork(Key work) {
        this.work = work;
    }

    public boolean isCanonical() {
        return canonical;
    }

    public void setCanonical(boolean canonical) {
        this.canonical = canonical;
    }

    @Override
    public String toString() {
        return "TestInputResult{" +
                "input=" + input +
                ", resultsFrom=" + resultsFrom +
                ", distributedTo=" + distributedTo +
                ", results=" + results +
                ", inputEntity='" + work + '\'' +
                ", canonical=" + canonical +
                '}';
    }

}
