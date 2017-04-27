package com.appspot.vcdiss.ops;

import com.appspot.vcdiss.ops.servlets.ToggleUserStatusServlet;
import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.appspot.vcdiss.utils.test.MutableHttpServletRequest;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ToggleUserStatusServletTest {

    private ToggleUserStatusServlet toggleUserStatusServlet;

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

    private Authoriser authoriser;
    private StringWriter stringWriter;


    @Before
    public void setupGuestBookServlet() throws EntityNotFoundException {
        helper.setUp();
        toggleUserStatusServlet = new ToggleUserStatusServlet();

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
        credentials.setPassword("pass");

        authoriser = new Authoriser(credentials).authorise();

    }


    @After
    public void tearDownHelper() {
        helper.tearDown();
    }


    @Test
    public void testDoGetBasic() throws IOException, EntityNotFoundException, ServletException {

        Assert.assertEquals("start", ds.get(users.get(0).getKey()).getProperty("status"));

        toggleStatus(users.get(0));

        Assert.assertEquals("stop", ds.get(users.get(0).getKey()).getProperty("status"));

        Assert.assertEquals("Stopped", stringWriter.toString());

    }

    @Test
    public void testDoGetBasicAndThenBack() throws IOException, EntityNotFoundException, ServletException {

        Assert.assertEquals("start", ds.get(users.get(0).getKey()).getProperty("status"));

        toggleStatus(users.get(0));

        Assert.assertEquals("stop", ds.get(users.get(0).getKey()).getProperty("status"));

        Assert.assertEquals("Stopped", stringWriter.toString());

        toggleStatus(users.get(0));

        Assert.assertEquals("start", ds.get(users.get(0).getKey()).getProperty("status"));

        Assert.assertEquals("Started", stringWriter.toString());

    }

    @Test
    public void testDoGetBadAuth() throws IOException, EntityNotFoundException, ServletException {

        Assert.assertEquals("start", ds.get(users.get(0).getKey()).getProperty("status"));

        Entity userToRuinAuth = users.get(0);
        userToRuinAuth.setProperty("password", "string-that-wont-match-password-hash");
        ds.put(userToRuinAuth);

        toggleStatus(users.get(0));

        Assert.assertEquals("start", ds.get(users.get(0).getKey()).getProperty("status"));

        Assert.assertEquals("", stringWriter.toString());

    }

    @Test
    public void testDoGetTokenExpired() throws IOException, EntityNotFoundException, ServletException {

        Assert.assertEquals("start", ds.get(users.get(0).getKey()).getProperty("status"));


        toggleStatusAfterRuiningToken(users.get(0));

        Assert.assertEquals("start", ds.get(users.get(0).getKey()).getProperty("status"));

        Assert.assertEquals("", stringWriter.toString());

    }

    private void toggleStatusAfterRuiningToken(Entity user) throws IOException, ServletException, EntityNotFoundException {
        MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
        HttpServletResponse response = mock(HttpServletResponse.class);

        Credentials credentials = new Credentials();
        credentials.setUsername((String) user.getProperty("name"));
        credentials.setPassword("pass");

        authoriser = new Authoriser(credentials).authorise();

        if (authoriser.wasSuccessful()) {
            request.putHeader("X-Auth-Token", authoriser.getWebToken());
        } else {
            request.putHeader("X-Auth-Token", "bad*token");
        }

        user = ds.get(user.getKey());

        request.putHeader("Origin", "http://localhost:8080");

        stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        user.setProperty("web_access_token_issued", new Date(System.currentTimeMillis() - 100000000));
        ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(user);

        toggleUserStatusServlet.doGet(request, response);
    }


    private void toggleStatus(Entity user) throws IOException, ServletException {

        MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
        HttpServletResponse response = mock(HttpServletResponse.class);

        Credentials credentials = new Credentials();
        credentials.setUsername((String) user.getProperty("name"));
        credentials.setPassword("pass");

        authoriser = new Authoriser(credentials).authorise();

        if (authoriser.wasSuccessful()) {
            request.putHeader("X-Auth-Token", authoriser.getWebToken());
        } else {
            request.putHeader("X-Auth-Token", "bad*token");
        }

        request.putHeader("Origin", "http://localhost:8080");

        stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));


        toggleUserStatusServlet.doGet(request, response);
    }


}


