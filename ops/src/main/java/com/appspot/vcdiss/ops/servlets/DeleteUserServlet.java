package com.appspot.vcdiss.ops.servlets;

import com.appspot.vcdiss.utils.LiveUrlCreator;
import com.appspot.vcdiss.utils.MiscUtils;
import com.google.appengine.api.datastore.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;


/**
 * Servlet that toggles the status (crunching/paused) of the user associated with an access token.
 */
public class DeleteUserServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DeleteUserServlet.class.getName());


    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // pre-flight request processing
        if (req.getHeader("Origin").equals("http://localhost:8080") || req.getHeader("Origin").equals("https://vc-diss.appspot.com")) {
            resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        }
        resp.setHeader("Access-Control-Allow-Methods", "GET");
        resp.setHeader("Access-Control-Allow-Headers", "X-Auth-Token");
    }


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // pre-flight request processing
        if (req.getHeader("Origin").equals("http://localhost:8080") || req.getHeader("Origin").equals("https://vc-diss.appspot.com")) {
            resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        }


        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        String webAccessToken = req.getHeader("X-Auth-Token");

        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());


        try {
            Entity user = authenticateUser(webAccessToken, datastore);



            if (user != null) {
                datastore.delete(user.getKey());

                LOG.info("User " + user.getProperty("name") + " successfully deleted.");
                resp.setStatus(200);

            } else {

                throw new SecurityException();

            }

        } catch (SecurityException | EntityNotFoundException e) {
            e.printStackTrace();
            resp.setStatus(403);
        }

    }

    private Entity authenticateUser(String header, DatastoreService datastore) throws EntityNotFoundException {

        //retrieve user based on the supplied access token, if it exists and has not expired.

        Entity user = datastore.prepare(
                new Query("user")
                        .setFilter(new Query.FilterPredicate("web_access_token", Query.FilterOperator.EQUAL, header)))
                .asSingleEntity();

        if (user==null) {
            return null;
        }

        if (MiscUtils.tokenExpired((Date) user.getProperty("web_access_token_issued"))) {
            return null;
        }

        return user;
    }




}
