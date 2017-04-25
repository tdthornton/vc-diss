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

package com.appspot.vcdiss.workout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.*;

import com.appspot.vcdiss.utils.MiscUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * Servlet provides the next input to a cruncher (who has a valid access token), as well as the necessary code.
 */
public class WorkServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(WorkServlet.class.getName());
    private static final int MIN_STARTER_OFFSET = 0;
    private static final int MAX_STARTER_OFFSET = 150;
    private Entity newWork;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        String auth = req.getHeader("X-Auth-Token");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


        try {

            Entity user = authenticateUser(auth, datastore);

            if (user == null) {
                throw new SecurityException();
            }

            if (user.getProperty("status").equals("stop")) {
                throw new WorkPausedException("User " + user.getProperty("name") + " requested work but is set to pause, cruncher should idle...");
            }

            Entity ow = getNewWork(datastore, user);

            Entity work = datastore.get((Key) ow.getProperty("app"));

            //create response entity to post as JSON: input, and hash of code it is allowed to run against.
            WorkOut workOut = new WorkOut((String) work.getProperty("md5"), String.valueOf(ow.getProperty("input")));

            List<String> distributedTo = (List<String>) ow.getProperty("distributedTo");
            distributedTo.add(KeyFactory.keyToString(user.getKey()));
            ow.setProperty("distributedTo", distributedTo);
            datastore.put(ow);

            LOG.info("Found work for " + user.getProperty("name") + ": " + workOut);

            resp.getWriter().print(new ObjectMapper().writeValueAsString(workOut));

        } catch (EntityNotFoundException | NoSuchElementException e) {
            e.printStackTrace();
            resp.setStatus(410);
        } catch (WorkPausedException e) {
            resp.setStatus(409);
        } catch (SecurityException e) {
            e.printStackTrace();
            resp.setStatus(403);
        }

    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        /*allow cruncher to download the code it needs to process the input.
        All inputs come with a code signature to check what this endpoint provides BEFORE running it.*/

        String auth = req.getHeader("X-Auth-Token");

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();


        try {

            Entity user = authenticateUser(auth, datastore);

            if (user == null) {
                throw new SecurityException();
            }


            if (user.getProperty("status").equals("stop")) {
                throw new WorkPausedException("User " + user.getProperty("name") + " requested code but is paused.");
            }

            Entity work = datastore.get((Key) user.getProperty("app"));
            resp.getWriter().print(work.getProperty("code"));

        } catch (WorkPausedException e) {
            resp.setStatus(409);
            e.printStackTrace();
        } catch (SecurityException e) {
            resp.setStatus(403);
            e.printStackTrace();
        } catch (Exception e) {
            resp.setStatus(410);
            e.printStackTrace();
        }


    }

    private Entity authenticateUser(String header, DatastoreService datastore) throws EntityNotFoundException {
        Entity tokenlink = datastore.prepare(
                new Query("token_link")
                        .setFilter(new Query.FilterPredicate("token", Query.FilterOperator.EQUAL, header)))
                .asSingleEntity();

        if (tokenlink == null) {
            return null;
        }

        if (MiscUtils.tokenExpired((Date) tokenlink.getProperty("issued"))) {
            datastore.delete(tokenlink.getKey());
            return null;
        }

        return datastore.get((Key) tokenlink.getProperty("user"));
    }


    public Entity getNewWork(DatastoreService datastore, Entity user) {

        /*EXTREMELY HORRIBLE in-memory filtering. Datastore cannot handle a "WHERE val NOT IN <array>",
        the necessity of doing it manually is discussed in the original vc-diss report.


        The following algorithm gets the next x number of unsatisfied inputs for a given app, then checks them all to see if
        they are eligible for distribution to our user (no existing results from them, not been distributed to them before).

        If they are not eligible, the next x inputs are retrieved (by offsetting the query) and the process is repeated.

        The actual value of x (offset) is hazed a little to prevent all crunchers getting the same input all the time,
        resulting in lots of errors at results-in.*/

        Entity ow = new Entity("input");
        List<String> resultsFrom = new ArrayList<String>();
        List<String> distributedTo = new ArrayList<String>();


        PreparedQuery pq2 = datastore.prepare(new Query("input")
                .setFilter(Query.CompositeFilterOperator.and(
                        Query.FilterOperator.EQUAL.of("app", user.getProperty("app")),
                        Query.FilterOperator.EQUAL.of("canonical", false)
                )));


        int offset = 0;
        int starterOffset = ThreadLocalRandom.current().nextInt(MIN_STARTER_OFFSET, MAX_STARTER_OFFSET + 1);
        boolean keepGoing = true;

        do {
            Iterator it = pq2.asIterable(FetchOptions.Builder.withLimit(starterOffset).offset(offset)).iterator();
            if (!it.hasNext()) {
                throw new NoSuchElementException("");
            }
            while (it.hasNext()) {
                ow = (Entity) it.next();
                resultsFrom = (List<String>) ow.getProperty("resultsFrom");
                distributedTo = (List<String>) ow.getProperty("distributedTo");
                if (!resultsFrom.contains(KeyFactory.keyToString(user.getKey())) && !distributedTo.contains(KeyFactory.keyToString(user.getKey()))) {
                    keepGoing = false;
                    break;
                }


            }
            offset = offset + starterOffset;
        } while (keepGoing);

        return ow;


    }
}
