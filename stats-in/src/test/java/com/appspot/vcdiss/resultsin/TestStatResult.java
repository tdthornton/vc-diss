package com.appspot.vcdiss.resultsin;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Created by Tom on 16/03/2017.
 */
public class TestStatResult {
    private Key user;
    private long credits;
    private String week;

    public TestStatResult(Entity actual) {
        this.user = (Key) actual.getProperty("user");
        this.credits = (long) actual.getProperty("credits");
        this.week = (String) actual.getProperty("week");
    }

    public TestStatResult(Key user, long credits, String week) {
        this.user = user;
        this.credits = credits;
        this.week = week;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }

    public Key getUser() {
        return user;
    }

    public void setUser(Key user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestStatResult that = (TestStatResult) o;

        if (credits != that.credits) return false;
        if (!user.equals(that.user)) return false;
        return week.equals(that.week);

    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + (int) (credits ^ (credits >>> 32));
        result = 31 * result + week.hashCode();
        return result;
    }
}
