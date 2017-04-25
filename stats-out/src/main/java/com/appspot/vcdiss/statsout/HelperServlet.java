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

package com.appspot.vcdiss.statsout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.appspot.vcdiss.utils.LiveUrlCreator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows the cruncher to access a "service catalogue" which details the URLs of all necessary services.
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
