package com.appspot.vcdiss.ops;

import com.appspot.vcdiss.ops.servlets.ConfirmEmailServlet;
import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.google.api.client.util.Base64;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class ConfirmEmailServletTest {

    private ConfirmEmailServlet confirmEmailServlet;

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalModulesServiceTestConfig())
                    .setEnvIsLoggedIn(true)
                    .setEnvAuthDomain("localhost")
                    .setEnvEmail("test@localhost");

    DataUtils dataUtils;
    private List<Entity> users = new ArrayList<>();
    private List<Entity> inputs = new ArrayList<>();
    private List<Entity> apps = new ArrayList<>();
    private List<Entity> stats = new ArrayList<>();

    private HttpServletRequest request;
    private HttpServletResponse response;

    private Authoriser authoriser;

    @Before
    public void setupGuestBookServlet() throws EntityNotFoundException {
        helper.setUp();
        confirmEmailServlet = new ConfirmEmailServlet();

        dataUtils = new DataUtils();
        dataUtils.makeSampleData();

        for (Key userKey : dataUtils.getUsers()) {
            users.add(ds.get(userKey));
        }

        for (Key appKey : dataUtils.getApps()) {
            apps.add(ds.get(appKey));
        }

        for (Key inputKey : dataUtils.getInputs()) {
            inputs.add(ds.get(inputKey));
        }

        for (Key statKey : dataUtils.getStats()) {
            stats.add(ds.get(statKey));
        }

        Credentials credentials = new Credentials();
        credentials.setUsername("test1");
        credentials.setPassword("t3heqaNa");

        authoriser = new Authoriser(credentials).authorise();

    }

    @After
    public void tearDownHelper() {
        helper.tearDown();
    }



    private void triggerGet(String code) throws IOException, ServletException {

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);


        when(request.getParameter("code")).thenReturn(code);
        when(request.getRequestDispatcher(Matchers.anyString())).thenReturn(mock(RequestDispatcher.class));


        confirmEmailServlet.doGet(request, response);
    }



    @Test
    public void testDoGetBasic() throws IOException, ServletException, EntityNotFoundException {

        String verificationCode = getNewVerificationCode(users.get(0).getKey());

        Assert.assertFalse(userVerified(users.get(0)));

        triggerGet(verificationCode);

        Assert.assertTrue(userVerified(users.get(0)));
    }


    @Test
    public void testDoGetBadCode() throws IOException, ServletException, EntityNotFoundException {

        Assert.assertFalse(userVerified(users.get(0)));

        triggerGet("*invalidcode*");

        verify(response).setStatus(404);

        Assert.assertFalse(userVerified(users.get(0)));

    }



    @Test
    public void testDoGetDeletedUser() throws IOException, ServletException, EntityNotFoundException {

        String verificationCode = getNewVerificationCode(users.get(0).getKey());

        Assert.assertFalse(userVerified(users.get(0)));

        ds.delete(users.get(0).getKey());

        triggerGet(verificationCode);

        verify(response).setStatus(404);
    }




    private boolean userVerified(Entity user) throws EntityNotFoundException {

        Entity userRead = ds.prepare(
                new Query("user")
                        .setFilter(new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, user.getProperty("name"))))
                .asSingleEntity();

        return (boolean) userRead.getProperty("email_verified");
    }


    private String getNewVerificationCode(Key userKey) {

        //creates new random string and saves it as an email verification code

        String confirmationCode = Base64.encodeBase64URLSafeString(UUID.randomUUID().toString().getBytes());

        Entity newVerificationCode = new Entity("verification_code");
        newVerificationCode.setProperty("issued", new Date());
        newVerificationCode.setProperty("user", userKey);
        newVerificationCode.setProperty("code", confirmationCode);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(newVerificationCode);

        return confirmationCode;

    }



}


