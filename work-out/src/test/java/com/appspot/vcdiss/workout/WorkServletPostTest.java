/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appspot.vcdiss.workout;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.appspot.vcdiss.utils.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class WorkServletPostTest {

  private WorkServlet workServlet;
  private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
          .setEnvIsLoggedIn(true)
          .setEnvAuthDomain("localhost")
          .setEnvEmail("test@localhost");

  private Entity input;
  private Entity user;
  private Entity app;
  private Authoriser authoriser;
  private DataUtils dataUtils;


  @Before
  public void setupGuestBookServlet() throws EntityNotFoundException {
    helper.setUp();
    dataUtils = new DataUtils();
    workServlet = new WorkServlet();


    dataUtils.makeSampleData();

    Credentials credentials = new Credentials();
    credentials.setUsername("test1");
    credentials.setPassword("pass");

    authoriser = new Authoriser(credentials).authorise();

    input= ds.get(dataUtils.makeSampleInput(0,0));
    app= ds.prepare(new Query("work")).asSingleEntity();

  }

  @After
  public void tearDownHelper() {
    helper.tearDown();
  }

  @Test
  public void testDoPostBasic() throws IOException {
    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doPost(request, response);



//    verify(response).setStatus(200);
    assertEquals(app.getProperty("code"), stringWriter.toString());
  }

  @Test
  public void testDoPostCrunchingPaused() throws IOException {
    Entity user = authoriser.getUser();

    user.setProperty("status", "stop");
    ds.put(user);


    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doPost(request, response);



    verify(response).setStatus(409);
    assertEquals("", stringWriter.toString());
  }

  @Test
  public void testDoPostNoApp() throws IOException {
    ds.delete(app.getKey());


    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doPost(request, response);



    verify(response).setStatus(410);
    assertEquals("", stringWriter.toString());
  }

  @Test
  public void testDoPostBadAuth() throws IOException {


    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", "badAuthToken");

    workServlet.doPost(request, response);



    verify(response).setStatus(403);
    assertEquals("", stringWriter.toString());
  }


}


