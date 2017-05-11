package com.appspot.vcdiss.workout;

import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.appspot.vcdiss.utils.test.MutableHttpServletRequest;
import com.appspot.vcdiss.workout.servlets.WorkServlet;
import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
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
    credentials.setPassword("t3heqaNa");

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


