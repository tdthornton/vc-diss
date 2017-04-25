package com.appspot.vcdiss.utils;

/**
 * Domain class to encapsulate credentials used by Authoriser.
 */
public class Credentials {
    private String username;
    private String password;
    private String cruncherName;

    public String getCruncherName() {
        return cruncherName;
    }

    public void setCruncherName(String cruncherName) {
        this.cruncherName = cruncherName;
    }

    @Override
    public String toString() {
        return "Credentials{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", cruncherName='" + cruncherName + '\'' +
                '}';
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
