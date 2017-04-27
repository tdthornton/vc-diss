package com.appspot.vcdiss.utils;

import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

public class AuthoriserTest {

    private Authoriser authoriser;

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalDatastoreServiceTestConfig()
            )
                    .setEnvIsLoggedIn(true)
                    .setEnvAuthDomain("localhost")
                    .setEnvEmail("test@localhost");


    private DataUtils dataUtils;

    @Before
    public void setupGuestBookServlet() throws IOException {

        helper.setUp();

        dataUtils = new DataUtils();
        dataUtils.makeSampleData();


    }

    @After
    public void tearDownHelper() {
        helper.tearDown();
    }

    @Test
    public void testGoodCredentials() throws IOException, ServletException {

        Credentials credentials = new Credentials();
        credentials.setUsername("test1");
        credentials.setPassword("pass");

        authoriser = new Authoriser(credentials).authorise();

        Assert.assertTrue(authoriser.wasSuccessful());

    }

    @Test
    public void testBadCredentials() throws IOException, ServletException {

        Credentials credentials = new Credentials();
        credentials.setUsername("test1");
        credentials.setPassword("**BADPASS**");

        authoriser = new Authoriser(credentials).authorise();

        Assert.assertFalse(authoriser.wasSuccessful());

    }


    @Test
    public void testLockedAccount() throws IOException, ServletException {

        Credentials badCredentials = new Credentials();
        badCredentials.setUsername("test1");
        badCredentials.setPassword("**BADPASS**");

        authoriser = new Authoriser(badCredentials).authorise();
        Assert.assertFalse(authoriser.wasSuccessful());

        authoriser = new Authoriser(badCredentials).authorise();
        Assert.assertFalse(authoriser.wasSuccessful());

        authoriser = new Authoriser(badCredentials).authorise();
        Assert.assertFalse(authoriser.wasSuccessful());



        Credentials goodCredentialsButLockedByNow = new Credentials();
        badCredentials.setUsername("test1");
        badCredentials.setPassword("pass");

        authoriser = new Authoriser(badCredentials).authorise();
        Assert.assertFalse(authoriser.wasSuccessful());



    }

    @Test
    public void testGetWebAccessToken() throws IOException, ServletException {

        Credentials credentials = new Credentials();
        credentials.setUsername("test1");
        credentials.setPassword("pass");

        authoriser = new Authoriser(credentials).authorise();

        Assert.assertNotNull(authoriser.getWebToken());

    }

    @Test
    public void testGetAccessToken() throws IOException, ServletException {

        Credentials credentials = new Credentials();
        credentials.setUsername("test1");
        credentials.setPassword("pass");

        authoriser = new Authoriser(credentials).authorise();

        Assert.assertNotNull(authoriser.getToken());

    }



}
