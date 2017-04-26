package com.appspot.vcdiss.ops.domain;

/**
 * Domain class for encapsulating registration details.
 */
public class RegistrationRequest {
    private final boolean agree;
    private String username;
    private String password;
    private boolean corporate;
    private String email;

    public RegistrationRequest(String username, String password, boolean corporate, boolean agree, String email) {
        this.username = username;
        this.password = password;
        this.corporate = corporate;
        this.email = email;
        this.agree = agree;
    }

    public boolean agreesToTerms() {
        return agree;
    }

    @Override
    public String toString() {
        return "RegistrationRequest{" +
                "agree=" + agree +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", corporate=" + corporate +
                ", email='" + email + '\'' +
                '}';
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCorporate() {
        return corporate;
    }

    public void setCorporate(boolean corporate) {
        this.corporate = corporate;
    }

    public boolean anyFieldIsNullOrInvalid() {
        return password == null || password.length()>250 || username==null  || email == null;
    }
}
