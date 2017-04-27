package com.appspot.vcdiss.ops;

import com.appspot.vcdiss.ops.domain.TestUserWithTotalCredits;
import com.appspot.vcdiss.ops.servlets.TotalCreditsServlet;
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
import java.util.List;

import static org.mockito.Mockito.mock;

public class TotalCreditsServletTest {

    private TotalCreditsServlet totalCreditsServlet;

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
        totalCreditsServlet = new TotalCreditsServlet();

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
    public void getWeekNo() {
        //GAE doesn't have J8 ZonedDateTime, only javautildate and jodatime....

    }


    @Test
    public void testDoGetBasic() throws IOException, EntityNotFoundException, ServletException {


        triggerCron();

        TestUserWithTotalCredits expected = new TestUserWithTotalCredits("test5", 2000);

        TestUserWithTotalCredits actual = new TestUserWithTotalCredits(ds.get(users.get(5).getKey()).getProperties());
//
        Assert.assertEquals(expected, actual);
    }

    private void triggerCron() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);


        totalCreditsServlet.doGet(request, response);
    }

    @Test
    public void testDoGetNoStats() throws IOException, ServletException, EntityNotFoundException {

        for (Entity stat : stats) {
            ds.delete(stat.getKey());
        }


        triggerCron();


        TestUserWithTotalCredits expected = new TestUserWithTotalCredits("test5", 1000);
        TestUserWithTotalCredits actual = new TestUserWithTotalCredits(ds.get(users.get(5).getKey()).getProperties());


        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testCantFindUserError() throws IOException, ServletException {

        for (Entity user : users) {
            ds.delete(user.getKey());
        }

        triggerCron(); //just check no errors thrown, test passes, situation is fine

    }






}


