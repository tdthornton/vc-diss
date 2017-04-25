package com.google.appspot.vcdiss.ops;

import java.util.Map;

/**
 * Created by Tom on 19/04/2017.
 */
public class TestUserWithTotalCredits {

    private String username;
    private long credits;

    public TestUserWithTotalCredits(String username, long credits) {
        this.username = username;
        this.credits = credits;
    }

    public TestUserWithTotalCredits(Map<String, Object> properties) {
        this.username= (String) properties.get("name");
        this.credits= (long) properties.get("life_time_credits");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestUserWithTotalCredits that = (TestUserWithTotalCredits) o;

        if (credits != that.credits) return false;
        return username.equals(that.username);

    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + (int) (credits ^ (credits >>> 32));
        return result;
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
