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

import com.google.api.client.util.Base64;
import com.google.appengine.api.datastore.*;


import com.google.appspot.utils.EmailUtils;
import com.google.appspot.utils.LiveUrlCreator;
import com.google.appspot.utils.SecurityUtils;
import org.apache.commons.validator.routines.EmailValidator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servlet provides registration HTML form and persists its results.
 */
public class RegisterServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(RegisterServlet.class.getName());
    private int MIN_PASSWORD_LENGTH = 8;
    private int MAX_USERNAME_LENGTH = 12;


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setContentType("text/html");

        RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/register.jsp");
        jsp.forward(req, resp);

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        RegistrationRequest registrationRequest = new RegistrationRequest(req.getParameter("username"), req.getParameter("password"), Boolean.valueOf(req.getParameter("business")), Boolean.valueOf(req.getParameter("agree")), req.getParameter("email"));

        LOG.info("Received new registration request from: " + registrationRequest.getUsername());

        String validationError = checkValidationError(registrationRequest);

        if (validationError.equals("None")) {
            Key userKey = persist(registrationRequest);
            String verificationCode = getNewVerificationCode(userKey);
            sendVerificationEmail(verificationCode, registrationRequest);

            req.setAttribute("info_text", "Welcome to vc-diss, " + registrationRequest.getUsername() + ".\nWe have sent you a link to verify your email address.");
            resp.setContentType("text/html");

            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/infopage.jsp");
            jsp.forward(req, resp);
        } else {
            resp.setStatus(408);
            req.setAttribute("info_text", "There was an error processing your registration: " + validationError);
            resp.setContentType("text/html");

            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/infopage.jsp");
            jsp.forward(req, resp);
        }


    }

    private void sendVerificationEmail(String verificationCode, RegistrationRequest registrationRequest) throws MalformedURLException {

        String messageBody = "Hi, " + registrationRequest.getUsername() + ". \n \n Welcome to vc-diss, " +
                "please click here to verify your email address: " + LiveUrlCreator.getLiveUrl("ops", "verifyEmail") + "?code=" + verificationCode + "" +
                " \n \n Many thanks.";


        EmailUtils.sendEmail(messageBody, "vc-diss: Please verify your email", registrationRequest.getEmail());
        LOG.info("Sent email verification code to " + registrationRequest.getUsername());

    }


    private String getNewVerificationCode(Key userKey) {

        //creates new random string and saves it as an email verification code

        String confirmationCode = Base64.encodeBase64URLSafeString(UUID.randomUUID().toString().getBytes());

        Entity newVerificationCode = new Entity("verification_code");
        newVerificationCode.setProperty("issued", new Date());
        newVerificationCode.setProperty("user", userKey);
        newVerificationCode.setProperty("code", confirmationCode);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(newVerificationCode);

        return confirmationCode;

    }

    private Key persist(RegistrationRequest registrationRequest) {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        //handle password hashing and store new user.

        Entity newUser = new Entity("user");
        newUser.setProperty("name", registrationRequest.getUsername());
        newUser.setProperty("email", registrationRequest.getEmail());
        newUser.setProperty("admin", isAdmin(registrationRequest.getEmail()));
        newUser.setProperty("email_verified", false);
        newUser.setProperty("corporate_account", registrationRequest.isCorporate());
        newUser.setProperty("status", "start");
        newUser.setProperty("app", ds.prepare(new Query("work")).asSingleEntity().getKey());
        String salt = SecurityUtils.getNewSalt();
        newUser.setProperty("password", SecurityUtils.hash(registrationRequest.getPassword(), salt));
        newUser.setProperty("salt", salt);
        newUser.setProperty("life_time_credits", 0);

        LOG.info("Persisting new user: " + registrationRequest.getUsername());

        return ds.put(newUser);
    }

    private boolean isAdmin(String email) {

        return  email.equals("tomthornton.123@gmail.com");

    }


    private String checkValidationError(RegistrationRequest registrationRequest) {

        if (registrationRequest.anyFieldIsNullOrInvalid()) {
            return "There was an error with your details. Please try again.";
        }

        if (!registrationRequest.agreesToTerms()) {
            return "You must agree to our terms to register.";
        }

        if (registrationRequest.getPassword().length() < MIN_PASSWORD_LENGTH) {
            return "Your password must be at least " + MIN_PASSWORD_LENGTH + " characters long.";
        }

        if (registrationRequest.getUsername().length() > MAX_USERNAME_LENGTH) {
            return "Your username must be a maximum of " + MAX_USERNAME_LENGTH + " characters long.";
        }

        if (!isValidEmail(registrationRequest.getEmail())) {
            return "Your email address is invalid.";
        }

        if (duplicateProperty("name", registrationRequest.getUsername())) {
            return "That username has been taken.";
        }

        if (duplicateProperty("email", registrationRequest.getEmail())) {
            return "Your email address is already registered with us. Click <a href=\"/resetpassword?prefill_email=" + registrationRequest.getEmail() + "\">here</a> to reset your password.";
        }

        return "None";
    }

    private boolean duplicateProperty(String property, String username) {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        Entity user = ds.prepare(new Query("user")
                .setFilter(new Query.FilterPredicate(property, Query.FilterOperator.EQUAL, username))).asSingleEntity();


        if (user != null) {

            return true;

        }

        return false;
    }


    private boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

}
