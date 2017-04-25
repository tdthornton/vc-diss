/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appspot.vcdiss.ops;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appspot.utils.Authoriser;
import com.google.appspot.utils.Credentials;
import com.google.appspot.utils.DataUtils;
import com.google.appspot.vcdiss.ops.java.v1.servlets.ResetPasswordServlet;
import com.google.appspot.vcdiss.ops.java.v1.servlets.ToggleUserStatusServlet;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ResetPasswordGetTest {

    private ResetPasswordServlet resetPasswordServlet;

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
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
        credentials.setPassword("pass");

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


        verify(request).getRequestDispatcher("/WEB-INF/resetpassword.jsp");

    }

    @Test
    public void testDoGetNoPrefilledEmail() throws IOException, EntityNotFoundException, ServletException {

        String email = (String) users.get(1).getProperty("email");

        call(email);


        verify(request).getRequestDispatcher("/WEB-INF/resetpassword.jsp");

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


