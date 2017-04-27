package com.appspot.vcdiss.utils.servlets;

import com.appspot.vcdiss.utils.LiveUrlCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows the cruncher to access a "service catalogue" which details the URLs of all necessary services, as stats-out is the default module.
 */
public class HelperServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
          throws IOException, ServletException {

    Map<String, String> urls = new HashMap<>();

    urls.put("work-out", LiveUrlCreator.getLiveUrl("work-out", ""));
    urls.put("results-in", LiveUrlCreator.getLiveUrl("results-in", ""));
    urls.put("stats-in", LiveUrlCreator.getLiveUrl("stats-in", ""));
    urls.put("ops", LiveUrlCreator.getLiveUrl("ops", ""));


    resp.getWriter().println(new ObjectMapper().writeValueAsString(urls));


  }

}
