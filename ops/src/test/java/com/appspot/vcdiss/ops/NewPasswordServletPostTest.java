package com.appspot.vcdiss.ops;

import com.appspot.vcdiss.ops.servlets.NewPasswordServlet;
import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.appspot.vcdiss.utils.test.MutableHttpServletRequest;
import com.google.api.client.util.Base64;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
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
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class NewPasswordServletPostTest {

    private NewPasswordServlet newPasswordServlet;

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalMailServiceTestConfig(), new LocalModulesServiceTestConfig())
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
    public void testDoPostBasic() throws IOException, EntityNotFoundException, ServletException {

        String oldPasswordHash = (String) users.get(0).getProperty("password");

        String resetCode = persistAndGetNewResetCode(users.get(0).getKey(), true);

        call(   "passchangedtothis",   //password
                resetCode);            //reset code


        verify(request).getRequestDispatcher("/WEB-INF/InfoPage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("reset successfully"));

        String newPasswordHash = (String) ds.get(users.get(0).getKey()).getProperty("password");

        Assert.assertNotEquals(oldPasswordHash, newPasswordHash);

    }


    @Test
    public void testDoPostExpiredToken() throws IOException, EntityNotFoundException, ServletException {

        String oldPasswordHash = (String) users.get(0).getProperty("password");

        String resetCode = persistAndGetNewResetCode(users.get(0).getKey(), false);

        call(   "passchangedtothis",   //password
                resetCode);            //reset code


        verify(request).getRequestDispatcher("/WEB-INF/InfoPage.jsp");
        verify(request).setAttribute("info_text", "There was an error with the password reset code.");
        verify(response).setStatus(404);

        String newPasswordHash = (String) ds.get(users.get(0).getKey()).getProperty("password");

        Assert.assertEquals(oldPasswordHash, newPasswordHash); //password was update

    }

    @Test
    public void testDoPostInvalidToken() throws IOException, EntityNotFoundException, ServletException {

        String oldPasswordHash = (String) users.get(0).getProperty("password");


        call(   "passchangedtothis",   //password
                "--invalidtoken--");   //reset code


        verify(request).getRequestDispatcher("/WEB-INF/InfoPage.jsp");
        verify(request).setAttribute("info_text", "There was an error with the password reset code.");
        verify(response).setStatus(404);

        String newPasswordHash = (String) ds.get(users.get(0).getKey()).getProperty("password");

        Assert.assertEquals(oldPasswordHash, newPasswordHash); //password was not updated

    }



    private void call(String password, String code) throws IOException, ServletException {

        request = mock(MutableHttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        mockDispatcher = mock(RequestDispatcher.class);

        stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));


        when(request.getRequestDispatcher(Matchers.anyString())).thenReturn(mockDispatcher);

        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("code")).thenReturn(code);




        newPasswordServlet.doPost(request, response);
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


