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

package com.appspot.vcdiss.ops;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.appspot.vcdiss.utils.Authoriser;
import com.appspot.vcdiss.utils.Credentials;
import com.appspot.vcdiss.utils.DataUtils;
import com.appspot.vcdiss.ops.servlets.RegisterServlet;
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

public class RegisterServletPostTest {

    private RegisterServlet registerServlet;

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
        registerServlet = new RegisterServlet();

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

        call(   "newusername",          //username
                "newpassnewpass",       //password
                "newemail@newemail.com",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("Welcome to vc-diss"));

    }

    @Test
    public void testDoPostDidntAgreeToTerms() throws IOException, EntityNotFoundException, ServletException {

        call(   "newusername",          //username
                "newpassnewpass",       //password
                "newemail@newemail.com",//email
                "false",                //business account
                false);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("must agree"));
        verify(response).setStatus(408);

    }

    @Test
    public void testDoPostPasswordTooShort() throws IOException, EntityNotFoundException, ServletException {

        call(   "newusername",          //username
                "newpass",              //password
                "newemail@newemail.com",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("password"));
        verify(response).setStatus(408);

    }

    @Test
    public void testDoPostNullField() throws IOException, EntityNotFoundException, ServletException {

        call(   null,                   //username
                "newpassnewpass",       //password
                "newemail@newemail.com",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("error"));
        verify(response).setStatus(408);

    }


    @Test
    public void testDoPostInvalidEmail() throws IOException, EntityNotFoundException, ServletException {

        call(   "newusername",                   //username
                "newpassnewpass",       //password
                "newemail@z",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("email"));
        verify(response).setStatus(408);

    }


    @Test
    public void testDoPostUsernameTooLong() throws IOException, EntityNotFoundException, ServletException {

        call(   "newusername_newusername",                   //username
                "newpassnewpass",       //password
                "newemail@newemail.com",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("username"));
        verify(response).setStatus(408);

    }

    @Test
    public void testDoPostDuplicateEmail() throws IOException, EntityNotFoundException, ServletException {

        call(   "oldusername",          //username
                "newpassnewpass",       //password
                "newemail@newemail.com",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("Welcome to vc-diss"));

        //repeat same email, different username

        call(   "newusername",          //username
                "newpassnewpass",       //password
                "newemail@newemail.com",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("email"));
        verify(response).setStatus(408);

    }

    @Test
    public void testDoPostDuplicateUsername() throws IOException, EntityNotFoundException, ServletException {

        call(   "oldusername",          //username
                "newpassnewpass",       //password
                "oldemail@newemail.com",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("Welcome to vc-diss"));

        //repeat same username, different email

        call(   "oldusername",          //username
                "newpassnewpass",       //password
                "newemail@newemail.com",//email
                "false",                //business account
                true);                  //agreed to terms


        verify(request).getRequestDispatcher("/WEB-INF/infopage.jsp");
        verify(request).setAttribute(eq("info_text"), Matchers.contains("username"));
        verify(response).setStatus(408);

    }



    private void call(String username, String password, String email, String business, boolean agreed) throws IOException, ServletException {

        request = mock(MutableHttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        mockDispatcher = mock(RequestDispatcher.class);

        stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));



        when(request.getRequestDispatcher(Matchers.anyString())).thenReturn(mockDispatcher);
        when(request.getParameter("username")).thenReturn(username);
        when(request.getParameter("password")).thenReturn(password);
        when(request.getParameter("email")).thenReturn(email);
        when(request.getParameter("business")).thenReturn(business);
        when(request.getParameter("agree")).thenReturn(String.valueOf(agreed));




        registerServlet.doPost(request, response);
    }


}


