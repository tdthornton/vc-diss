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
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


/**
 * Servlet that accepts and processes the upload of new inputs for the platform.
 */
public class UploadInputsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UploadInputsServlet.class.getName());


    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // pre-flight request processing
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
        String newInputs = "";

        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());

        try { //just extracting csv file and md5 textbox inputs from the POST in a nasty javax-style manual mutlipart/form handler
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = null;


            iterator = upload.getItemIterator(req);

            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                if (item.isFormField()) {
                    System.out.println(item.getFieldName());
                    if (item.getFieldName().equals("md5")) {
                        hash = IOUtils.toString(item.openStream(), "utf-8");
                    } else if (item.getFieldName().equals("token")) {
                        token = IOUtils.toString(item.openStream(), "utf-8");
                    }
                } else {
                    System.out.println(item.getFieldName());
                    newInputs = IOUtils.toString(item.openStream(), "utf-8");

                }
            }
        } catch (Exception e) {
            req.setAttribute("error_text", "There was an error with your inputs, please try again.");
            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
            jsp.forward(req, resp);
        }


        try {
            if (hashMatches(newInputs, hash)) {
                Entity user = authenticateUser(token, datastore);


                if (user != null && (boolean) user.getProperty("admin")) {

                    Entity app = datastore.get((Key) user.getProperty("app"));

                    List<String> newInputValues = getNewInputValuesFromCSV(IOUtils.toInputStream(newInputs, "UTF-8"));

                    LOG.info("User " + user.getProperty("name") + " uploaded new inputs for : " + app.getProperty("name"));

                    persistNewInputs(newInputValues, app);

                    resp.setStatus(200);
                    req.setAttribute("info_text", "Your new inputs were uploaded successfully.");
                    RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
                    jsp.forward(req, resp);

                } else {

                    throw new SecurityException();

                }
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("info_text", "There was an error with your inputs, please try again.");
            RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/InfoPage.jsp");
            jsp.forward(req, resp);
        }


    }

    private void persistNewInputs(List<String> newInputValues, Entity app) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


        for (String newInputValue : newInputValues) {


            Entity newInput = new Entity("input");
            newInput.setProperty("app", app.getKey());

            List<String> results = new ArrayList<>();
            List<String> resultsFrom = new ArrayList<>();
            List<String> distributedTo = new ArrayList<>();

            final int STARTER_ARRAY_POPULATION = 10;

            for (int i = 0; i < STARTER_ARRAY_POPULATION; i++) {
                distributedTo.add(null);
                results.add(null);
                resultsFrom.add(null);
            }


            newInput.setProperty("input", newInputValue);
            newInput.setProperty("results", results);
            newInput.setProperty("resultsFrom", resultsFrom);
            newInput.setProperty("distributedTo", distributedTo);

            newInput.setProperty("credited", false);
            newInput.setProperty("canonical", false);
            datastore.put(newInput);
        }
    }

    private List<String> getNewInputValuesFromCSV(InputStream newInputs) throws IOException {
        List<String> inputs = new ArrayList<>();

        List<String> lines = IOUtils.readLines(newInputs, "UTF-8");

        for (String line : lines) {
            if (line != null) {

                Collections.addAll(inputs, line.split(","));

            }
        }

    return inputs;

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

        if (MiscUtils.tokenExpired((Date) user.getProperty("web_access_token_issued"))) {
            return null;
        }

        return user;

    }


}
