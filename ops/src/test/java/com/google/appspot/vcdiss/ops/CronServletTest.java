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
import com.google.appspot.vcdiss.ops.java.v1.servlets.CronServlet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;

public class CronServletTest {

    private CronServlet cronServlet;

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
        cronServlet = new CronServlet();

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
    public void testTriggerCronNoExpiredTokens() throws IOException, EntityNotFoundException, ServletException {

        Key newTokenKey = persistAndGetNewToken(users.get(0).getKey(), false);


        triggerCron();

        Entity token = ds.prepare(new Query("token_link").setFilter(
                new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, newTokenKey)))
                .asSingleEntity();

        Assert.assertNotNull(token);

    }




    @Test
    public void testTriggerCronExpiredToken() throws IOException, EntityNotFoundException, ServletException {

        Key newTokenKey = persistAndGetNewToken(users.get(0).getKey(), true);

        triggerCron();

        Entity token = ds.prepare(new Query("token_link").setFilter(
                new Query.FilterPredicate(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, newTokenKey)))
                .asSingleEntity();

        Assert.assertNull(token);

    }








    private void triggerCron() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        cronServlet.doGet(request, response);

    }



    public Key persistAndGetNewToken(Key userKey, boolean expired) {

        String newToken = UUID.randomUUID().toString(); //test one, doesn't need to be cryptographically secure

        Entity tokenlink = new Entity("token_link");

        tokenlink.setProperty("token", newToken);
        tokenlink.setProperty("user", userKey);
        tokenlink.setProperty("cruncher_name", "testcruncher");

        if (!expired) {
            tokenlink.setProperty("issued", new Date());
        } else {
            tokenlink.setProperty("issued", new Date(System.currentTimeMillis() - 100000000));
        }


        return ds.put(tokenlink);

    }



}