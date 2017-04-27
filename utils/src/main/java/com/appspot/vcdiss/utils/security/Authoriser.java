package com.appspot.vcdiss.utils.security;

import com.appspot.vcdiss.utils.EmailUtils;
import com.google.api.client.util.Base64;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Utility class that encapsulates the inner security procedure, leaving servlets both secure and uncluttered.
 */
public class Authoriser {

    private static final Logger log = Logger.getLogger(Authoriser.class.getName());

    private Credentials credentials;
    private Entity user;
    private String token;
    private String webToken;
    private boolean successful;
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    public Authoriser(Credentials credentials) {
        this.credentials = credentials;
        this.successful=false;
    }

    public String getToken() {
        String newToken = getNewToken();

        if (!newToken.equals("")) {

            Date date = new Date();

            Entity tokenlink = new Entity("token_link");

            tokenlink.setProperty("token", newToken);
            tokenlink.setProperty("user", user.getKey());
            tokenlink.setProperty("issued", date);
            tokenlink.setProperty("cruncher_name", credentials.getCruncherName());


            ds.put(tokenlink);

        }
        token = newToken;
        return token;
    }

    public String getWebToken() {
        String newToken = getNewToken();

        if (!newToken.equals("")) {

            Date date = new Date();

            user.setProperty("web_access_token", newToken);
            user.setProperty("web_access_token_issued", date);


            ds.put(user);

        }
        webToken = newToken;
        return webToken;

    }

    public Entity getUser() {
        return user;
    }

    public Authoriser authorise() {

        user = authorise(credentials);

        if (user != null) {

            successful=true;
            log.info(credentials.getUsername() + " successfully authenticated.");

        } else {

            successful=false;
            log.info(credentials.getUsername() + " failed authentication.");

        }

        return this;


    }


    private Entity authorise(Credentials credentials) {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        Entity user = ds.prepare(new Query("user")
                .setFilter(new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, credentials.getUsername()))).asSingleEntity();



        if (user==null) {
            return null;
        }

        Long failedLoginAttemptsToday = (Long) user.getProperty("failed_login_attempts_today");

        if (failedLoginAttemptsToday>2) { //then this is the third, at least

            try {
                EmailUtils.sendEmail("Hi, " + user.getProperty("name") + "\n \n Your account has been locked after many failed login attempts. \nFor security reasons, we recommend you recover your password, but you will not be able to log in until tomorrow.\nPlease let us know if you believe this is suspicious activity towards your account.",
                        "vc-diss: Account locked.", (String) user.getProperty("email"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            log.info("User " + user.getProperty("name") + " locked.");

            return null;
        }


        if (user.getProperty("password").equals(SecurityUtils.hash(credentials.getPassword(), (String) user.getProperty("salt")))) {

            return user;

        } else {
            user.setProperty("failed_login_attempts_today", failedLoginAttemptsToday+1);
            ds.put(user);
        }

        return null;

    }





    private String getNewToken() {
        try {
            SecureRandom sr = null;

            sr = SecureRandom.getInstance("SHA1PRNG");


            byte[] bytes = new byte[32];
            sr.nextBytes(bytes);
            return Base64.encodeBase64String(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean wasSuccessful() {
        return successful;
    }

    public boolean wasAdmin() {
        return (boolean) user.getProperty("admin");
    }
}



