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

package com.appspot.vcdiss.ops.servlets;

import com.google.appengine.api.datastore.*;


import com.appspot.vcdiss.utils.EmailUtils;
import com.appspot.vcdiss.utils.MiscUtils;
import com.appspot.vcdiss.utils.SecurityUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

public class NewPasswordServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(NewPasswordServlet.class.getName());



    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        String code = req.getParameter("code");

        try {

            if (getUserFromResetCode(code, datastore)!=null) {
                resp.setContentType("text/html");

                req.setAttribute("resetCode", code);

                RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/newpassword.jsp");
                jsp.forward(req, resp);
            } else {
                throw new NullPointerException("Unauthorised password reset attempt with code: " + code);
            }

        } catch (NullPointerException | EntityNotFoundException e) {
            e.printStackTrace();
            resp.setStatus(404);
            resp.setContentType("text/html");

            req.setAttribute("info_text", "You have supplied an invalid password reset code.");

            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/infopage.jsp");
            jsp.forward(req, resp);

        }



    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        String password = req.getParameter("password");
        String code = req.getParameter("code");

        try {

            Entity user = getUserFromResetCode(code, datastore);

            if (user != null) {

                LOG.info("Processing new password request for " + user.getProperty("name"));

                String salt = SecurityUtils.getNewSalt();
                user.setProperty("salt", salt);
                user.setProperty("password", SecurityUtils.hash(password, salt));
                datastore.put(user);
                req.setAttribute("info_text", "Your password has been reset successfully.");
                resp.setContentType("text/html");

                EmailUtils.sendEmail("Hi, " + user.getProperty("name") + "\n \n The password for your account was recently changed." +
                        "\nPlease contact us if this was not you." +
                        "\n\nThanks, \n vc-diss team", "vc-diss: Security Alert", (String) user.getProperty("email"));

                RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/infopage.jsp");
                jsp.forward(req, resp);
            } else {
                throw new NullPointerException("No user associated with reset code " + code);
            }


        } catch (PreparedQuery.TooManyResultsException | EntityNotFoundException | NullPointerException e) {
            e.printStackTrace();
            resp.setStatus(404);

            LOG.info("Invalid password reset code " + code + " used in password reset attempt.");
            req.setAttribute("info_text", "There was an error with the password reset code.");
            resp.setContentType("text/html");

            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/infopage.jsp");
            jsp.forward(req, resp);
        }


    }
    private Entity getUserFromResetCode(String code, DatastoreService datastore) throws EntityNotFoundException, NullPointerException {


        Entity resetCode= datastore.prepare(
                new Query("reset_code")
                        .setFilter(new Query.FilterPredicate("code", Query.FilterOperator.EQUAL, code)))
                .asSingleEntity();

        if (resetCode==null) {
            return null;
        }


        if (!MiscUtils.tokenExpired((Date) resetCode.getProperty("issued"))) {
            return datastore.get((Key) resetCode.getProperty("user"));
        } else {
            return null;
        }

    }



}
