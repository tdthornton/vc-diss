package com.appspot.vcdiss.ops.servlets;

import com.appspot.vcdiss.utils.LiveUrlCreator;
import com.appspot.vcdiss.utils.MiscUtils;
import com.google.appengine.api.datastore.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Logger;


/**
 * Servlet that accepts and processes the upload of updated algorithm code.
 */
public class UpdateCodeServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UpdateCodeServlet.class.getName());


    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getHeader("Origin").equals("https://vc-diss.appspot.com")) {
            resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        }
        resp.setHeader("Access-Control-Allow-Methods", "POST");
        resp.setHeader("Access-Control-Allow-Headers", "X-Auth-Token");
    }


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        // pre-flight request processing
        if (req.getHeader("Origin").equals("https://vc-diss.appspot.com") || req.getHeader("Origin").equals("https://1-dot-default-dot-vc-diss.appspot.com")) {
            resp.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        String hash = "";
        String token = "";
        String newCode = "";

        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());

        try { //just extracting file and md5 textbox inputs from the POST in a nasty javax-style manual mutlipart/form handler
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = null;


            iterator = upload.getItemIterator(req);

            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                if (item.isFormField()) {
                    if (item.getFieldName().equals("md5")) {
                        hash = IOUtils.toString(item.openStream(), "utf-8");
                    } else if (item.getFieldName().equals("token")) {
                        token = IOUtils.toString(item.openStream(), "utf-8");
                    }
                } else {
                    newCode = IOUtils.toString(item.openStream(), "utf-8");

                }
            }
        } catch (Exception e) {
            req.setAttribute("info_text", "There was an error with your inputs, please try again.");
            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
            jsp.forward(req, resp);
        }


        try {
            if (hashMatches(newCode, hash)) {
                Entity user = authenticateUser(token, datastore);


                if (user != null) {

                    Entity app = datastore.get((Key) user.getProperty("app"));

                    app.setProperty("code", newCode);
                    app.setProperty("version", (Long) app.getProperty("version") + 1);
                    datastore.put(app);


                    LOG.info("User " + user.getProperty("name") + " updated code for : " + app.getProperty("name"));

                    resp.setStatus(200);

                } else {

                    throw new SecurityException();

                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (SecurityException | EntityNotFoundException e) {
            e.printStackTrace();
            resp.setStatus(403);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("info_text", "There was an error with your inputs, please try again.");
            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
            jsp.forward(req, resp);
        }


    }

    private boolean hashMatches(String newCode, String hash) throws IOException, NoSuchAlgorithmException {

        return hash.equalsIgnoreCase(DigestUtils.md5Hex(newCode.replaceAll("[\r]", "")));

    }

    private Entity authenticateUser(String header, DatastoreService datastore) throws EntityNotFoundException {

        //retrieve user based on the supplied access token, if it exists and has not expired.

        Entity user = datastore.prepare(
                new Query("user")
                        .setFilter(new Query.FilterPredicate("web_access_token", Query.FilterOperator.EQUAL, header)))
                .asSingleEntity();

        if (user == null) {
            return null;
        }

        if (!(boolean)user.getProperty("admin")) {
            return null;
        }

        if (MiscUtils.tokenExpired((Date) user.getProperty("web_access_token_issued"))) {
            return null;
        }

        return user;

    }


}
