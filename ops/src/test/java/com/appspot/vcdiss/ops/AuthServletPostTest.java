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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.appspot.vcdiss.utils.Authoriser;
import com.appspot.vcdiss.utils.Credentials;
import com.appspot.vcdiss.utils.DataUtils;
import com.appspot.vcdiss.ops.servlets.AuthServlet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class AuthServletPostTest {

    private AuthServlet authServlet;

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
        authServlet = new AuthServlet();

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


        call((String) users.get(0).getProperty("name"), "pass");

        Assert.assertNotEquals(stringWriter.toString(), "");

    }

    @Test
    public void testDoPostBadAuth() throws IOException, EntityNotFoundException, ServletException {


        call((String) users.get(0).getProperty("username"), "**invalidpass**");

        Assert.assertEquals(stringWriter.toString(), "");
        verify(response).setStatus(403);

    }



    private void call(String username, String password) throws IOException, ServletException {

        request = mock(MutableHttpServletRequest.class);
        response = mock(HttpServletResponse.class);



        stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        Credentials credentials = new Credentials();
        credentials.setUsername(username);
        credentials.setPassword(password);

        String jsonString = new ObjectMapper().writeValueAsString(credentials);

        MutableServletInputStream payload = new MutableServletInputStream(jsonString);

        when(request.getInputStream()).thenReturn(payload);


        authServlet.doPost(request, response);

    }


}


