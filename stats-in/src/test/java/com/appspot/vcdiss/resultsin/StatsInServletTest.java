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

package com.appspot.vcdiss.resultsin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import com.appspot.vcdiss.statsin.StatsInServlet;
import com.appspot.vcdiss.utils.DataUtils;
import com.appspot.vcdiss.utils.MiscUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

public class StatsInServletTest {

    private StatsInServlet resultsInServlet;

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
                    .setEnvIsLoggedIn(true)
                    .setEnvAuthDomain("localhost")
                    .setEnvEmail("test@localhost");

    private DataUtils dataUtils;

    private List<Entity> users = new ArrayList<>();
    private List<Entity> inputs = new ArrayList<>();
    private List<Entity> apps = new ArrayList<>();
    private List<Entity> stats = new ArrayList<>();

    @Before
    public void setupGuestBookServlet() throws EntityNotFoundException {
        helper.setUp();
        dataUtils = new DataUtils();
        dataUtils.makeSampleData();
        resultsInServlet = new StatsInServlet();

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

    }

    @After
    public void tearDownHelper() {
        helper.tearDown();
    }


    @Test
    public void testDoPostBasic() throws IOException, EntityNotFoundException {

        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(1).getKey())), 200);

        TestStatResult expected = new TestStatResult(users.get(0).getKey(), 1000, MiscUtils.getWeek());


        Entity stat = ds.prepare(new Query("stat")
                .setFilter(new Query.FilterPredicate("user", Query.FilterOperator.EQUAL, users.get(0).getKey())))
                .asSingleEntity();

        TestStatResult actual = new TestStatResult(stat);

        Assert.assertEquals(expected, actual);

        verifyCredited(true, inputs.get(1).getKey());
        verifyCredited(false, inputs.get(0).getKey());

    }


    private void verifyCredited(boolean credited, Key inputKey) throws EntityNotFoundException {
        Entity latestInput = ds.get(inputKey);
        Assert.assertEquals(latestInput.getProperty("credited"), credited);
    }


    @Test
    public void testDoPostTwoBasic() throws IOException, EntityNotFoundException {

        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(1).getKey())), 200);
        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(5).getKey())), 200);

        TestStatResult expected = new TestStatResult(users.get(0).getKey(), 2000, MiscUtils.getWeek());

        Entity stat = ds.prepare(new Query("stat")
                .setFilter(new Query.FilterPredicate("user", Query.FilterOperator.EQUAL, users.get(0).getKey())))
                .asSingleEntity();

        TestStatResult actual = new TestStatResult(stat);

        Assert.assertEquals(expected, actual);

        verifyCredited(true, inputs.get(1).getKey());
        verifyCredited(true, inputs.get(5).getKey());
    }

    @Test
    public void testDoPostTwoDifferentUsers() throws IOException, EntityNotFoundException {

        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(1).getKey())), 200);
        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(5).getKey())), 200);

        Key[] userKeys = {users.get(0).getKey(), users.get(1).getKey(), users.get(2).getKey()};


        for (int i = 0; i < userKeys.length; i++) {
            TestStatResult expected = new TestStatResult(userKeys[i], 2000, MiscUtils.getWeek());

            Entity stat = ds.prepare(new Query("stat")
                    .setFilter(new Query.FilterPredicate("user", Query.FilterOperator.EQUAL, userKeys[i])))
                    .asSingleEntity();

            TestStatResult actual = new TestStatResult(stat);

            Assert.assertEquals(expected, actual);
        }

        verifyCredited(true, inputs.get(1).getKey());
        verifyCredited(true, inputs.get(5).getKey());

    }


    @Test
    public void testDoPostBadInputs() throws IOException, EntityNotFoundException {

        ds.delete(inputs.get(0).getKey());

        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(0).getKey())), 410);

        verifyCredited(false, inputs.get(1).getKey());

    }

    @Test
    public void testDoPostBadApp() throws IOException, EntityNotFoundException {

        ds.delete(apps.get(0).getKey());

        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(1).getKey())), 410);

        verifyCredited(false, inputs.get(1).getKey());

    }

    @Test
    public void testDoPostAlreadyCredited() throws IOException, EntityNotFoundException {

        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(1).getKey())), 200);
        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(1).getKey())), 407);


        verifyCredited(true, inputs.get(1).getKey());
        verifyCredited(false, inputs.get(0).getKey());
    }

    @Test
    public void testDoPostNotSatisfied() throws IOException, EntityNotFoundException {

        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(0).getKey())), 408);


        verifyCredited(false, inputs.get(0).getKey());
        verifyCredited(false, inputs.get(1).getKey());
    }

    @Test
    public void testDoPostAppDeleted() throws IOException, EntityNotFoundException {

        ds.delete(apps.get(0).getKey());

        postBasicInput(new TestInboundKey(KeyFactory.keyToString(inputs.get(1).getKey())), 410);


        verifyCredited(false, inputs.get(0).getKey());
        verifyCredited(false, inputs.get(1).getKey());
    }


    public void postBasicInput(TestInboundKey result, int expectedStatusCode) throws IOException {
        postBasicInput(result, "authToken", expectedStatusCode);
    }

    public void postBasicInput(TestInboundKey result, String authToken, int expectedStatusCode) throws IOException {
        MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
        HttpServletResponse response = mock(HttpServletResponse.class);


        InputStream is = IOUtils.toInputStream("{\n" +
                "\t\"key\": \"" + result.getKey() + "\"\n" +
                "}", "UTF-8");

        when(request.getInputStream()).thenReturn(new MutableServletInputStream(is));

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        request.putHeader("X-Auth-Token", authToken);

        resultsInServlet.doPost(request, response);

        verify(response).setStatus(expectedStatusCode);
    }
//

}


