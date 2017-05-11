package com.appspot.vcdiss.workout;

import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.appspot.vcdiss.utils.test.DataUtils;
import com.appspot.vcdiss.utils.test.MutableHttpServletRequest;
import com.appspot.vcdiss.workout.domain.WorkOutTestObject;
import com.appspot.vcdiss.workout.servlets.WorkServlet;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class WorkServletTest {

  private WorkServlet workServlet;
  private DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
          .setEnvIsLoggedIn(true)
          .setEnvAuthDomain("localhost")
          .setEnvEmail("test@localhost");

  private Entity user;
  private Authoriser authoriser;

  DataUtils dataUtils;
  private List<Entity> users = new ArrayList<>();
  private List<Entity> inputs = new ArrayList<>();
  private List<Entity> apps = new ArrayList<>();
  private List<Entity> stats = new ArrayList<>();

  @Before
  public void setupGuestBookServlet() throws EntityNotFoundException {
    helper.setUp();
    dataUtils = new DataUtils();
    workServlet = new WorkServlet();


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
    user = authoriser.getUser();


  }

  @After
  public void tearDownHelper() {
    helper.tearDown();
  }

  @Test
  public void testDoGetBasic() throws IOException {

    ds.delete(inputs.get(5).getKey());
    for (int i = 0; i < 4; i++) {
      ds.delete(inputs.get(i).getKey());
    }

    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doGet(request, response);

    WorkOutTestObject expected = new WorkOutTestObject();
    expected.setCodeHash((String) apps.get(0).getProperty("md5"));
    expected.setInput(String.valueOf(inputs.get(4).getProperty("input")));


    WorkOutTestObject actual = new ObjectMapper().readValue(stringWriter.toString(), WorkOutTestObject.class);

//    verify(response).setStatus(200);
    assertEquals(expected, actual);
  }

  @Test
  public void testDoGetBadAuth() throws IOException {

    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", "badAuthToken");

    workServlet.doGet(request, response);

    verify(response).setStatus(403);
    assertEquals("", stringWriter.toString());
  }

  @Test
  public void testDoGetUserPaused() throws IOException {

    Entity user = authoriser.getUser();

    user.setProperty("status", "stop");
    ds.put(user);

    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doGet(request, response);

    verify(response).setStatus(409);
    assertEquals("", stringWriter.toString());
  }

  @Test
  public void testDoGetNoInputs() throws IOException {

    for (Entity input : inputs) {
      ds.delete(input.getKey());
    }

    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doGet(request, response);

    verify(response).setStatus(410);
    assertEquals("", stringWriter.toString());

  }

  @Test
  public void testDoGetNoApp() throws IOException {

    ds.delete(apps.get(0).getKey());

    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doGet(request, response);

    verify(response).setStatus(410);
    assertEquals("", stringWriter.toString());

  }

  @Test
  public void testDoGetAlreadyDistributedToUser() throws IOException {

    ArrayList<String> distributedTo = new ArrayList<String>();

    distributedTo.add(KeyFactory.keyToString(user.getKey()));
    distributedTo.add(null);
    distributedTo.add(null);
    for (Entity input : inputs) {
      input.setProperty("distributedTo", distributedTo);
      ds.put(input);
    }


    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doGet(request, response);

    verify(response).setStatus(410);
    assertEquals("", stringWriter.toString());

  }

  @Test
  public void testDoGetAlreadyReceivedResultFromUser() throws IOException {

    ArrayList<String> resultsFrom = new ArrayList<String>();

    resultsFrom.add(KeyFactory.keyToString(user.getKey()));
    resultsFrom.add(null);
    resultsFrom.add(null);
    for (Entity input : inputs) {
      input.setProperty("resultsFrom", resultsFrom);
      ds.put(input);
    }

    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    workServlet.doGet(request, response);

    verify(response).setStatus(410);
    assertEquals("", stringWriter.toString());

  }

  @Test
  public void testDoGetBasicExpiredToken() throws IOException {
    MutableHttpServletRequest request = new MutableHttpServletRequest(mock(MutableHttpServletRequest.class));
    HttpServletResponse response = mock(HttpServletResponse.class);

    StringWriter stringWriter = new StringWriter();
    when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

    request.putHeader("X-Auth-Token", authoriser.getToken());

    Entity token = ds.prepare(new Query("token_link")).asSingleEntity();
    token.setProperty("issued", new Date(System.currentTimeMillis()-100000000));
    ds.put(token);

    workServlet.doGet(request, response);

    verify(response).setStatus(403);
    assertEquals("", stringWriter.toString());
  }


}


