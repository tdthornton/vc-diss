package com.appspot.vcdiss.ops;

import com.appspot.vcdiss.ops.servlets.NewPasswordServlet;
import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.appspot.vcdiss.utils.test.MutableHttpServletRequest;
import com.google.api.client.util.Base64;
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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class NewPasswordServletGetTest {

    private NewPasswordServlet newPasswordServlet;

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
        newPasswordServlet = new NewPasswordServlet();

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


        String resetCode = persistAndGetNewResetCode(users.get(0).getKey(), true);


        call(resetCode);


        verify(request).getRequestDispatcher("/WEB-INF/NewPassword.jsp");
        verify(request).setAttribute("resetCode", resetCode);
        verify(mockDispatcher).forward(request, response);


    }

    @Test
    public void testDoGetExpiredCode() throws IOException, EntityNotFoundException, ServletException {


        String resetCode = persistAndGetNewResetCode(users.get(0).getKey(), false);


        call(resetCode);


        verify(request).getRequestDispatcher("/WEB-INF/InfoPage.jsp");
        verify(request).setAttribute("info_text", "You have supplied an invalid password reset code.");

        verify(response).setStatus(404);

        verify(mockDispatcher).forward(request, response);


    }


    @Test
    public void testDoGetInvalidCode() throws IOException, EntityNotFoundException, ServletException {


        String resetCode = "--invalidcode--";


        call(resetCode);


        verify(request).getRequestDispatcher("/WEB-INF/InfoPage.jsp");
        verify(request).setAttribute("info_text", "You have supplied an invalid password reset code.");

        verify(response).setStatus(404);

        verify(mockDispatcher).forward(request, response);


    }



    private void call(String resetCode) throws IOException, ServletException {

        request = mock(MutableHttpServletRequest.class);
        response = mock(HttpServletResponse.class);


        mockDispatcher = mock(RequestDispatcher.class);
        when(request.getRequestDispatcher(Matchers.anyString())).thenReturn(mockDispatcher);

        when(request.getParameter("code")).thenReturn(resetCode);



        newPasswordServlet.doGet(request, response);
    }

    private String persistAndGetNewResetCode(Key userKey, boolean valid) {
        //Create new random string, save it as a code that will allow the user to reset their password.

        String resetCode = Base64.encodeBase64URLSafeString(UUID.randomUUID().toString().getBytes());

        Entity newVerificationCode = new Entity("reset_code");
        if (valid) {
            newVerificationCode.setProperty("issued", new Date());
        } else {
            newVerificationCode.setProperty("issued", new Date(System.currentTimeMillis()-100000000));
        }
        newVerificationCode.setProperty("user", userKey);
        newVerificationCode.setProperty("code", resetCode);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(newVerificationCode);

        return resetCode;

    }


}


