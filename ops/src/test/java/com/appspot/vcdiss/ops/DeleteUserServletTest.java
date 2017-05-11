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

package com.appspot.vcdiss.ops;

import com.appspot.vcdiss.ops.servlets.DeleteUserServlet;
import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeleteUserServletTest {

    private DeleteUserServlet deleteUserServlet;

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

    @Before
    public void setupGuestBookServlet() throws EntityNotFoundException {
        helper.setUp();
        deleteUserServlet = new DeleteUserServlet();

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



    private void triggerGet(String token) throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getHeader("X-Auth-Token")).thenReturn(token);
        when(request.getHeader("Origin")).thenReturn("https://vc-diss.appspot.com");


        deleteUserServlet.doGet(request, response);
    }



    @Test
    public void testDoGetBasic() throws IOException, ServletException, EntityNotFoundException {

        Assert.assertTrue(userExists(users.get(0)));

        triggerGet(authoriser.getWebToken());

        Assert.assertFalse(userExists(users.get(0)));
    }

    @Test
    public void testDoGetBadToken() throws IOException, ServletException, EntityNotFoundException {

        Assert.assertTrue(userExists(users.get(0)));

        triggerGet("*Badtoken*");

        Assert.assertTrue(userExists(users.get(0)));
    }

    @Test
    public void testDoGetExpiredToken() throws IOException, ServletException, EntityNotFoundException {

        String tokenString = authoriser.getToken();

        Entity token = ds.prepare(new Query("token_link")).asSingleEntity(); //find the token and force its expiry
        token.setProperty("issued", new Date(System.currentTimeMillis()-100000000));
        ds.put(token);

        Assert.assertTrue(userExists(users.get(0)));

        triggerGet(tokenString);

        Assert.assertTrue(userExists(users.get(0)));
    }



    private boolean userExists(Entity user) throws EntityNotFoundException {

        Entity userRead = ds.prepare(
                new Query("user")
                        .setFilter(new Query.FilterPredicate("name", Query.FilterOperator.EQUAL, user.getProperty("name"))))
                .asSingleEntity();

        return userRead != null;
    }


}


