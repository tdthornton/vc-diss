package com.appspot.vcdiss.statsout;

/**
 * Domain class for holding data that will be turned into a bar chart on the homepage.
 */
public class Stat {
    private String user;
    private String week;
    private Long credits;

    public Stat(String user, String week, Long credits) {
        this.user = user;
        this.week = week;
        this.credits = credits;
    }

    @Override
    public String toString() {
        return "Stat{" +
                "user='" + user + '\'' +
                ", week='" + week + '\'' +
                ", credits='" + credits + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stat stat = (Stat) o;

        if (user != null ? !user.equals(stat.user) : stat.user != null) return false;
        if (week != null ? !week.equals(stat.week) : stat.week != null) return false;
        return credits != null ? credits.equals(stat.credits) : stat.credits == null;

    }

    @Override
    public int hashCode() {
        int result = user != null ? user.hashCode() : 0;
        result = 31 * result + (week != null ? week.hashCode() : 0);
        result = 31 * result + (credits != null ? credits.hashCode() : 0);
        return result;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public Long getCredits() {
        return credits;
    }

    public void setCredits(Long credits) {
        this.credits = credits;
    }

}
