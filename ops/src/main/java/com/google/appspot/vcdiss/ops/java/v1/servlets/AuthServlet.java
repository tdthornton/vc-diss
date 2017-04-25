/* Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appspot.vcdiss.ops.java.v1.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.appspot.utils.Authoriser;

import com.google.appspot.utils.Credentials;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Servlet gives access tokens to users with valid credentials, which the cruncher uses as authentication.
 */
public class AuthServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AuthServlet.class.getName());


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {


        Credentials credentials = new ObjectMapper().readValue(req.getInputStream(), Credentials.class);
        credentials.setCruncherName(req.getHeader("crunchername"));

        Authoriser authoriser = new Authoriser(credentials).authorise();


        if (authoriser.wasSuccessful()) { //send new token

            LOG.info("User " + credentials.getUsername() + " successfully authenticated");
            resp.getWriter().print(authoriser.getToken());

        } else { //security error

            LOG.info("User " + credentials.getUsername() + " failed authentication");

            resp.setStatus(403);

        }

    }
}
