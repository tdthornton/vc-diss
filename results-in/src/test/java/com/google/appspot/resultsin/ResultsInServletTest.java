/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appspot.resultsin;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import com.google.appspot.resultsin.java.v1.ResultsInServlet;
import com.google.appspot.utils.Authoriser;
import com.google.appspot.utils.Credentials;
import com.google.appspot.utils.DataUtils;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class ResultsInServletTest {

    private ResultsInServlet resultsInServlet;

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

    @Before
    public void setupGuestBookServlet() throws EntityNotFoundException {
        helper.setUp();
        resultsInServlet = new ResultsInServlet();

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
    public void testDoPostBasic() throws IOException, EntityNotFoundException {

        long inputNo = Long.parseLong((String) inputs.get(0).getProperty("input"));

        postBasicInput(new TestInboundResult(inputNo, "true"), 200, users.get(0));

        TestInputResult expected = new TestInputResult(String.valueOf(inputNo),
                new String[]{KeyFactory.keyToString(users.get(0).getKey()), null, null}, //resultsFrom
                new String[]{"true", null, null}, //results
                new String[]{KeyFactory.keyToString(users.get(0).getKey()), KeyFactory.keyToString(users.get(1).getKey()), KeyFactory.keyToString(users.get(2).getKey())}, //distributedto
                (Key) inputs.get(0).getProperty("app"),
                false);

        TestInputResult actual = new TestInputResult(ds.get(inputs.get(0).getKey()).getProperties());

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testDoPostAlreadyCanonicalForced() throws IOException, EntityNotFoundException {
        long inputNo = Long.parseLong((String) inputs.get(1).getProperty("input"));

        postBasicInput(new TestInboundResult(inputNo, "false"), 405, users.get(0));

    }

    @Test
    public void testDoPostRepeatedUser() throws IOException, EntityNotFoundException {

        long inputNo = Long.parseLong((String) inputs.get(0).getProperty("input"));

        postBasicInput(new TestInboundResult(inputNo, "true"), 200, users.get(2)); //once, fine
        postBasicInput(new TestInboundResult(inputNo, "true"), 408, users.get(2)); //twice, error

    }


    @Test
    public void testDoPostTwoSameResults() throws IOException, EntityNotFoundException {

        long inputNo = Long.parseLong((String) inputs.get(0).getProperty("input"));

        postBasicInput(new TestInboundResult(inputNo, "true"), 200, users.get(0));
        postBasicInput(new TestInboundResult(inputNo, "true"), 200, users.get(1));



        TestInputResult expected = new TestInputResult(String.valueOf(inputNo),
                new String[]{KeyFactory.keyToString(users.get(0).getKey()), KeyFactory.keyToString(users.get(1).getKey()), null},
                new String[]{"true", "true", null },
                new String[]{KeyFactory.keyToString(users.get(0).getKey()), KeyFactory.keyToString(users.get(1).getKey()), KeyFactory.keyToString(users.get(2).getKey())},
                (Key) inputs.get(0).getProperty("app"),
                false);

        TestInputResult actual = new TestInputResult(ds.get(inputs.get(0).getKey()).getProperties());

        Assert.assertEquals(expected, actual);
    }
//
//
    @Test
    public void testDoGetWithConflict() throws IOException, EntityNotFoundException {

        long inputNo = Long.parseLong((String) inputs.get(0).getProperty("input"));

        postBasicInput(new TestInboundResult(inputNo, "false"), 200, users.get(0));
        postBasicInput(new TestInboundResult(inputNo, "false"), 200, users.get(1));
        postBasicInput(new TestInboundResult(inputNo, "true"), 200, users.get(2));


        TestInputResult expected = new TestInputResult(String.valueOf(inputNo),
                new String[]{KeyFactory.keyToString(users.get(2).getKey()), null, null},
                new String[]{"true", null , null },
                new String[]{KeyFactory.keyToString(users.get(0).getKey()), KeyFactory.keyToString(users.get(1).getKey()), KeyFactory.keyToString(users.get(2).getKey())},
                (Key) inputs.get(0).getProperty("app"),
                false);


        TestInputResult actual = new TestInputResult(ds.get(inputs.get(0).getKey()).getProperties());


        Assert.assertEquals(expected, actual);

    }

    @Test
    public void testDoPostBadAuth() throws IOException, EntityNotFoundException {

        long inputNo = Long.parseLong((String) inputs.get(0).getProperty("input"));

        postBasicInput(new TestInboundResult(inputNo, "true"), "**BADAUTHTOKEN**", 403);


    }

    @Test
    public void testDoPostBadInput() throws IOException, EntityNotFoundException {

        postBasicInput(new TestInboundResult(0, "true"), 410, users.get(0)); //invalid input value "0"

    }

    @Test
    public void testDoPostClearAfterTwoResults() throws IOException, EntityNotFoundException {

        long inputNo = Long.parseLong((String) inputs.get(0).getProperty("input"));

        postBasicInput(new TestInboundResult(inputNo, "false"), 200, users.get(0));
        postBasicInput(new TestInboundResult(inputNo, "false"), 200, users.get(1));

        postBasicInput(new TestInboundResult(inputNo, "true"), 200, users.get(2));
        postBasicInput(new TestInboundResult(inputNo, "true"), 200, users.get(1));

        postBasicInput(new TestInboundResult(inputNo, "false"), 200, users.get(0));


        TestInputResult expected = new TestInputResult(String.valueOf(inputNo),
                new String[]{KeyFactory.keyToString(users.get(0).getKey()), null, null},
                new String[]{"false", null , null },
                new String[]{KeyFactory.keyToString(users.get(0).getKey()), KeyFactory.keyToString(users.get(1).getKey()), KeyFactory.keyToString(users.get(2).getKey())},
                (Key) inputs.get(0).getProperty("app"),
                false);


        TestInputResult actual = new TestInputResult(ds.get(inputs.get(0).getKey()).getProperties());


        Assert.assertEquals(expected, actual);

    }


    private void postBasicInput(TestInboundResult result, int expectedStatusCode, Entity user) throws IOException {
        Credentials creds = new Credentials();
        creds.setUsername((String) user.getProperty("name"));
        creds.setPassword("pass");

        authoriser = new Authoriser(creds).authorise();
        postBasicInput(result, authoriser.getToken(), expectedStatusCode);
    }


    public void postBasicInput(TestInboundResult result, String authToken, int expectedStatusCode) throws IOException {
        MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
        HttpServletResponse response = mock(HttpServletResponse.class);


        InputStream is = IOUtils.toInputStream("{\n" +
                "\t\"input\": " + result.getInput() + ",\n" +
                "\t\"result\": " + result.getResult() + "\n" +
                "}", "UTF-8");

        when(request.getInputStream()).thenReturn(new MutableServletInputStream(is));

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));


        request.putHeader("X-Auth-Token", authToken);

        resultsInServlet.doPost(request, response);

        verify(response).setStatus(expectedStatusCode);
    }
}


