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

package com.appspot.vcdiss.statsout;

import com.appspot.vcdiss.statsout.servlets.UserViewServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalModulesServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.appspot.vcdiss.utils.test.DataUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

public class UserViewServletTest {

    private UserViewServlet userViewServlet;
    private StringWriter stringWriter;
    private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalDatastoreServiceTestConfig(),
                    new LocalModulesServiceTestConfig().addDefaultModuleVersion()
            )
                    .setEnvIsLoggedIn(true)
                    .setEnvAuthDomain("localhost")
                    .setEnvEmail("test@localhost");

    private HttpServletResponse mresponse;
    private HttpServletRequest request;
    private RequestDispatcher  mockDispatcher;

    private DataUtils dataUtils;

    @Before
    public void setupGuestBookServlet() throws IOException {
        helper.setUp();
        dataUtils = new DataUtils();
        userViewServlet = new UserViewServlet();

        ds = DatastoreServiceFactory.getDatastoreService();

        dataUtils.makeSampleData();

        request = mock(HttpServletRequest.class);
        mresponse = mock(HttpServletResponse.class);
        mockDispatcher = mock(RequestDispatcher.class);

        stringWriter = new StringWriter();

        when(mresponse.getWriter()).thenReturn(new PrintWriter(stringWriter));
        when(request.getRequestDispatcher(Matchers.anyString())).thenReturn(mockDispatcher);



    }

    @After
    public void tearDownHelper() {
        helper.tearDown();
    }

    @Test
    public void testDoGet() throws IOException, ServletException {

        when(request.getParameter("username")).thenReturn("test1");
        when(request.getParameter("password")).thenReturn("pass");

        userViewServlet.doPost(request, mresponse);

        verify(request).getParameter("username");
        verify(request).getParameter("password");


        verify(request).getRequestDispatcher("/WEB-INF/UserPage.jsp");
        verify(request).setAttribute("username", "test1");
        verify(request).setAttribute("app", "Super Prime Checker 1.0");
        verify(request).setAttribute("status", "start");
        verify(request).setAttribute("vmCount", 0);
        verify(request).setAttribute("weeklyTotal", 0L);
        verify(request).setAttribute("allTimeTotal", 1000L);
        verify(request).setAttribute(eq("accessToken"), Matchers.anyString());
        verify(request).setAttribute("badgeLevelAllTime", "GOLD");
        verify(request).setAttribute("badgeLevelThisWeek", "STANDARD");
        verify(request).setAttribute("toggleStatusUrl", "");
        verify(request).setAttribute("toggleStatusUrl", "");
        verify(request).setAttribute("weekBadgeUrl", null);
        verify(request).setAttribute("allTimeBadgeUrl", "/badges/vc-diss-badge-gold-all-time.png");
        verify(request).setAttribute(eq("deleteAccountUrl"), Matchers.anyString());

        verify(mresponse).setContentType("text/html");

        verify(mockDispatcher).forward(request, mresponse);

        verifyNoMoreInteractions(mresponse);
        verifyNoMoreInteractions(request);

    }

    @Test
    public void testDoGetWithStats() throws IOException, ServletException {

        when(request.getParameter("username")).thenReturn("test5");
        when(request.getParameter("password")).thenReturn("pass");

        userViewServlet.doPost(request, mresponse);

        verify(request).getParameter("username");
        verify(request).getParameter("password");


        verify(request).getRequestDispatcher("/WEB-INF/UserPage.jsp");
        verify(request).setAttribute("username", "test5");
        verify(request).setAttribute("app", "Super Prime Checker 1.0");
        verify(request).setAttribute("status", "start");
        verify(request).setAttribute("vmCount", 0);
        verify(request).setAttribute("weeklyTotal", 1000L);
        verify(request).setAttribute("allTimeTotal", 2000L);
        verify(request).setAttribute(eq("accessToken"), Matchers.anyString());
        verify(request).setAttribute("badgeLevelAllTime", "GOLD");
        verify(request).setAttribute("badgeLevelThisWeek", "GOLD");
        verify(request).setAttribute("toggleStatusUrl", "");
        verify(request).setAttribute("deleteAccountUrl", "");
        verify(request).setAttribute("allTimeBadgeUrl", "/badges/vc-diss-badge-gold-all-time.png");
        verify(request).setAttribute("weekBadgeUrl", "/badges/vc-diss-badge-gold-this-week.png");

        verify(mresponse).setContentType("text/html");

        verify(mockDispatcher).forward(request, mresponse);

        verifyNoMoreInteractions(mresponse);
        verifyNoMoreInteractions(request);

    }

    @Test
    public void testDoGetAdmin() throws IOException, ServletException {

        when(request.getParameter("username")).thenReturn("admin1");
        when(request.getParameter("password")).thenReturn("pass");

        userViewServlet.doPost(request, mresponse);

        verify(request).getRequestDispatcher("/WEB-INF/AdminPage.jsp");
        verify(request).setAttribute("username", "admin1");

        verify(mresponse).setContentType("text/html");

    }

    @Test
    public void testDoGetBadCredentials() throws IOException, ServletException {

        when(request.getParameter("username")).thenReturn("test1");
        when(request.getParameter("password")).thenReturn("*INCORRECT*");

        userViewServlet.doPost(request, mresponse);

        verify(mresponse).setContentType("text/html");
        verify(request).getRequestDispatcher("/WEB-INF/ErrorPage.jsp");

    }

    @Test
    public void testDoGetDeletedApp() throws IOException, ServletException {

        DataUtils.clearApps();

        when(request.getParameter("username")).thenReturn("test1");
        when(request.getParameter("password")).thenReturn("pass");

        userViewServlet.doPost(request, mresponse);
        verify(mresponse).setStatus(404);

    }




}
