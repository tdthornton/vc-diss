package com.appspot.vcdiss.ops.servlets;

import com.appspot.vcdiss.utils.LiveUrlCreator;
import com.google.appengine.api.datastore.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Servlet handles incoming "click this link to verify your email address" actions.
 */
public class ConfirmEmailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ConfirmEmailServlet.class.getName());




    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        String confirmationCode = req.getParameter("code");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());


        try {
            Entity code = getConfirmationCodeEntity(confirmationCode, datastore);

            if (code != null) {

                Entity user = datastore.get((Key) code.getProperty("user"));


                if (user != null) {
                    user.setProperty("email_verified", true);
                    datastore.put(user);
                    datastore.delete(code.getKey());
                    resp.setStatus(200);
                    req.setAttribute("info_text", "Thank you for verifying your email address.");
                    resp.setContentType("text/html");

                    RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
                    jsp.forward(req, resp);

                    LOG.info("User " + user.getProperty("name") + " verified email");

                } else {

                    throw new NullPointerException("No user found.");
                }

            } else {

                throw new NullPointerException("No confirmation code found.");
            }

        } catch (NullPointerException | EntityNotFoundException e) {
            resp.setStatus(404);
            LOG.info("Invalid code " + confirmationCode + " was used in an email verification attempt.");
            req.setAttribute("info_text", "There was an error with the email verification code.");
            resp.setContentType("text/html");

            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
            jsp.forward(req, resp);

            e.printStackTrace();
        }

    }

    private Entity getConfirmationCodeEntity(String confirmationCode, DatastoreService datastore) throws EntityNotFoundException {


        return datastore.prepare(
                new Query("verification_code")
                        .setFilter(new Query.FilterPredicate("code", Query.FilterOperator.EQUAL, confirmationCode)))
                .asSingleEntity();
    }


}
