package com.appspot.vcdiss.ops.servlets;

import com.appspot.vcdiss.utils.security.Authoriser;
import com.appspot.vcdiss.utils.security.Credentials;
import com.fasterxml.jackson.databind.ObjectMapper;

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
