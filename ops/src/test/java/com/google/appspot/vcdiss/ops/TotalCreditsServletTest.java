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

package com.google.appspot.vcdiss.ops;

import com.google.appengine.api.datastore.*;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

import com.google.appspot.utils.Authoriser;
import com.google.appspot.utils.Credentials;
import com.google.appspot.utils.DataUtils;
import com.google.appspot.vcdiss.ops.java.v1.servlets.TotalCreditsServlet;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class TotalCreditsServletTest {

    private TotalCreditsServlet totalCreditsServlet;

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
        totalCreditsServlet = new TotalCreditsServlet();

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
    public void testDoGetBasic() throws IOException, EntityNotFoundException, ServletException {


        triggerCron();

        TestUserWithTotalCredits expected = new TestUserWithTotalCredits("test5", 2000);

        TestUserWithTotalCredits actual = new TestUserWithTotalCredits(ds.get(users.get(5).getKey()).getProperties());
//
        Assert.assertEquals(expected, actual);
    }

    private void triggerCron() throws IOException, ServletException {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);


        totalCreditsServlet.doGet(request, response);
    }

    @Test
    public void testDoGetNoStats() throws IOException, ServletException, EntityNotFoundException {

        for (Entity stat : stats) {
            ds.delete(stat.getKey());
        }


        triggerCron();


        TestUserWithTotalCredits expected = new TestUserWithTotalCredits("test5", 1000);
        TestUserWithTotalCredits actual = new TestUserWithTotalCredits(ds.get(users.get(5).getKey()).getProperties());


        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testCantFindUserError() throws IOException, ServletException {

        for (Entity user : users) {
            ds.delete(user.getKey());
        }

        triggerCron(); //just check no errors thrown, test passes, situation is fine

    }


    @Test
    public void testoijsdf() {
        String string = "";

        for (int i = 0; i < 1000; i++) {
            string = string.concat(new BigInteger(26, 1, new Random()).toString());
            string = string.concat(",");
        }

        System.out.println(string);
    }






}


