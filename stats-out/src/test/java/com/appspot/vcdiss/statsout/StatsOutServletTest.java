package com.appspot.vcdiss.statsout;

import com.appspot.vcdiss.utils.MiscUtils;
import com.appspot.vcdiss.utils.domain.Stat;
import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.servlets.StatsOutServlet;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class StatsOutServletTest {

    private StatsOutServlet statsOutServlet;

    private Entity input;
    private Entity app;
    private Authoriser authoriser;
    private StringWriter stringWriter;
    private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalDatastoreServiceTestConfig(),
                    new LocalModulesServiceTestConfig()
            )
                    .setEnvIsLoggedIn(true)
                    .setEnvAuthDomain("localhost")
                    .setEnvEmail("test@localhost");

    private HttpServletResponse mresponse;
    private HttpServletRequest request;
    private DataUtils dataUtils;

    @Before
    public void setupGuestBookServlet() throws IOException {
        helper.setUp();
        dataUtils = new DataUtils();
        statsOutServlet = new StatsOutServlet();


        ds = DatastoreServiceFactory.getDatastoreService();

        dataUtils.makeSampleData();

        Credentials credentials = new Credentials();
        credentials.setUsername("test1");
        credentials.setPassword("t3heqaNa");

        authoriser = new Authoriser(credentials).authorise();

        app = ds.prepare(new Query("work")).asSingleEntity();

        request = mock(HttpServletRequest.class);
        mresponse = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();

        when(mresponse.getWriter()).thenReturn(new PrintWriter(stringWriter));
        when(request.getRequestDispatcher(Matchers.anyString())).thenReturn(new RequestDispatcher() {
            @Override
            public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                //gets called, but can't do anything because there is no real HTTP environment to forward to.
            }

            @Override
            public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {

            }
        });


    }

    @After
    public void tearDownHelper() {
        helper.tearDown();
    }

    @Test
    public void testDoGet() throws IOException, ServletException {

        ArrayList<Stat> stats = new ArrayList<>();
        stats.add(new Stat("test5", MiscUtils.getWeek(), 1000L));

        statsOutServlet.doGet(request, mresponse);

        verify(request).getRequestDispatcher("/WEB-INF/Homepage.jsp");
        verify(request).setAttribute(eq("stats"), statsListEq(stats));
        verify(request).setAttribute("statscount", 1);
        verify(request).setAttribute("registerurl", "");
        verify(request).setAttribute("reseturl", "");

        verify(mresponse).setContentType("text/html");
        verify(request).setAttribute(eq("footerUrls"), Matchers.any());

        verifyNoMoreInteractions(mresponse);
        verifyNoMoreInteractions(request);

    }

    @Test
    public void testDoGetNoUserFoundError() throws IOException, ServletException {

        DataUtils.clearUsers();
        //With no more users, no stats are reported.

        ArrayList<Stat> stats = new ArrayList<>();

        statsOutServlet.doGet(request, mresponse);

        verify(request).getRequestDispatcher("/WEB-INF/Homepage.jsp");
        verify(request).setAttribute(eq("stats"), statsListEq(stats));
        verify(request).setAttribute("statscount", 0);
        verify(request).setAttribute("registerurl", "");
        verify(request).setAttribute("reseturl", "");

        verify(mresponse).setContentType("text/html");
        verify(request).setAttribute(eq("footerUrls"), Matchers.any());

        verifyNoMoreInteractions(mresponse);
        verifyNoMoreInteractions(request);

    }








    ////////////////////Custom mockito matchers
    static List<Stat> statsListEq(ArrayList<Stat> expected) {
        return argThat(new ValueObjectMatcher(expected));
    }

    static class ValueObjectMatcher extends ArgumentMatcher<List<Stat>> {

        private final ArrayList<Stat> expected;

        public ValueObjectMatcher(ArrayList<Stat> expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object actual) {
            // could improve with null checks
            ArrayList<Stat> actualList = (ArrayList<Stat>) actual;
            for (int i = 0; i < actualList.size(); i++) {
                if (!actualList.get(i).equals(expected.get(i))) {
                    return false;
                }
            }
            return expected.size() == actualList.size();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(expected == null ? null : expected.toString());
        }
    }

}
