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

package com.appspot.vcdiss.statsout.servlets;

import com.appspot.vcdiss.utils.LiveUrlCreator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that provides HTML for the homepage, as stats-out is the default module.
 * This is because the homepage primarily shows statistics, so stats-out handling it makes the most sense for the data access scaling patterns.
 */

public class PrivacyPolicyServlet extends HttpServlet {





    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setContentType("text/html");
        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());

        RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/PrivacyPolicyPage.jsp");
        jsp.forward(req, resp);

    }



}
