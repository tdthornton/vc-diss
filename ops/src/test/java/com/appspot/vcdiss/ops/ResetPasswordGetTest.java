package com.appspot.vcdiss.ops;

import com.appspot.vcdiss.ops.servlets.ResetPasswordServlet;
import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.appspot.vcdiss.utils.test.MutableHttpServletRequest;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ResetPasswordGetTest {

    private ResetPasswordServlet resetPasswordServlet;

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
    private RequestDispatcher mockDispatcher;

    private MutableHttpServletRequest request;
    private HttpServletResponse response;


    @Before
    public void setupGuestBookServlet() throws EntityNotFoundException {
        helper.setUp();
        resetPasswordServlet = new ResetPasswordServlet();

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
    public void testDoGetBasic() throws IOException, EntityNotFoundException, ServletException {

        String email = (String) users.get(0).getProperty("email");

        call(email);


        verify(request).getRequestDispatcher("/WEB-INF/ResetPassword.jsp");

    }

    @Test
    public void testDoGetNoPrefilledEmail() throws IOException, EntityNotFoundException, ServletException {

        String email = (String) users.get(1).getProperty("email");

        call(email);


        verify(request).getRequestDispatcher("/WEB-INF/ResetPassword.jsp");

    }




    private void call(String prefillEmail) throws IOException, ServletException {

        request = mock(MutableHttpServletRequest.class);
        response = mock(HttpServletResponse.class);


        when (request.getParameter("prefill_email")).thenReturn(prefillEmail);
        request.putHeader("Origin", "http://localhost:8080");

        stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));


        mockDispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher(Matchers.anyString())).thenReturn(mockDispatcher);



        resetPasswordServlet.doGet(request, response);
    }


}


