package com.appspot.vcdiss.ops.servlets;

import com.appspot.vcdiss.utils.EmailUtils;
import com.appspot.vcdiss.utils.LiveUrlCreator;
import com.google.api.client.util.Base64;
import com.google.appengine.api.datastore.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servlet to serve password reset page, and to handle sending password reset codes.
 */
public class ResetPasswordServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ResetPasswordServlet.class.getName());




    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        //serve webpage for entering email address

        req.setAttribute("prefill_email", req.getParameter("prefill_email"));
        resp.setContentType("text/html");

        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());

        RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/ResetPassword.jsp");
        jsp.forward(req, resp);


    }


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        String email = req.getParameter("email");

        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());

        try {

            //if user with that email exists, send them a password reset code.

            Entity user = datastore.prepare(new Query("user").setFilter(new Query.FilterPredicate("email", Query.FilterOperator.EQUAL, email))).asSingleEntity();


            String messageBody="Hi, " + user.getProperty("name") + ". \n \n Click here to reset your password: " + LiveUrlCreator.getLiveUrl("ops", "newpassword") + "?code="+getNewResetCode(user.getKey());

            EmailUtils.sendEmail(messageBody, "vc-diss: Temporary password reset link", (String) user.getProperty("email"));

            LOG.info("User " + user.getProperty("name") + " sent password reset email.");

            req.setAttribute("info_text", "We have sent you a link to reset your password.");
            resp.setContentType("text/html");

            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
            jsp.forward(req, resp);



        } catch (PreparedQuery.TooManyResultsException | NullPointerException e) {
            e.printStackTrace();
            req.setAttribute("info_text", "We have no user associated with that email.");
            resp.setContentType("text/html");

            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
            jsp.forward(req, resp);
            resp.setStatus(404);
        }


    }


    private String getNewResetCode(Key userKey) {
        //Create new random string, save it as a code that will allow the user to reset their password.

        String resetCode = Base64.encodeBase64URLSafeString(UUID.randomUUID().toString().getBytes());

        Entity newVerificationCode = new Entity("reset_code");
        newVerificationCode.setProperty("issued", new Date());
        newVerificationCode.setProperty("user", userKey);
        newVerificationCode.setProperty("code", resetCode);

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
        ds.put(newVerificationCode);

        return resetCode;

    }


}
