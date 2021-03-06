package com.appspot.vcdiss.ops;

import com.appspot.vcdiss.ops.servlets.CronServlet;
import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

public class CronServletTest {

    private CronServlet cronServlet;

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

    @Before
    public void setupGuestBookServlet() throws EntityNotFoundException {
        helper.setUp();
        cronServlet = new CronServlet();

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




    @Test
    public void testTriggerCronNoExpiredTokens() throws IOException, EntityNotFoundException, ServletException {

        Key newTokenKey = persistAndGetNewToken(users.get(0).getKey(), false);


        triggerCron();

        Entity token = ds.prepare(new Query("token_link").setFilter(
                new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, newTokenKey)))
                .asSingleEntity();

        Assert.assertNotNull(token);

    }




    @Test
    public void testTriggerCronExpiredToken() throws IOException, EntityNotFoundException, ServletException {

        Key newTokenKey = persistAndGetNewToken(users.get(0).getKey(), true);

        triggerCron();

        Entity token = ds.prepare(new Query("token_link").setFilter(
                new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, newTokenKey)))
                .asSingleEntity();

        Assert.assertNull(token);

    }








    private void triggerCron() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        cronServlet.doGet(request, response);

    }



    public Key persistAndGetNewToken(Key userKey, boolean expired) {

        String newToken = UUID.randomUUID().toString(); //test one, doesn't need to be cryptographically secure

        Entity tokenlink = new Entity("token_link");

        tokenlink.setProperty("token", newToken);
        tokenlink.setProperty("user", userKey);
        tokenlink.setProperty("cruncher_name", "testcruncher");

        if (!expired) {
            tokenlink.setProperty("issued", new Date());
        } else {
            tokenlink.setProperty("issued", new Date(System.currentTimeMillis() - 100000000));
        }


        return ds.put(tokenlink);

    }



}