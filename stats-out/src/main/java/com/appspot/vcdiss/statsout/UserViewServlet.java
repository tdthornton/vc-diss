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

package com.appspot.vcdiss.statsout;

import com.google.appengine.api.datastore.*;

import com.appspot.vcdiss.utils.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Servlet provides HTML for a logged-in web view of the system, where users can view their own statistics and pause their crunchers.
 * Responds to LOG-in form on StatsOutServlet.
 */
public class UserViewServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(UserViewServlet.class.getName());


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        try {
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

            Credentials credentials = new Credentials();
            credentials.setUsername(req.getParameter("username"));
            credentials.setPassword(req.getParameter("password"));


            Authoriser authoriser = new Authoriser(credentials).authorise();

            resp.setContentType("text/html");

            if (authoriser.wasSuccessful()) {

                String token = authoriser.getWebToken(); //new web-features-only access token will be available to the webpage
                Entity user = authoriser.getUser();

                Entity app = datastore.get((Key) user.getProperty("app"));

                Long weeklyTotal = getWeeklyTotal(user, datastore);

                HighscoresCheck badgeChecker = new HighscoresCheck(user);

                req.setAttribute("username", user.getProperty("name"));
                req.setAttribute("app", app.getProperty("app"));
                req.setAttribute("accesstoken", token);
                req.setAttribute("deleteAccountUrl", LiveUrlCreator.getLiveUrl("ops", "deleteaccount"));





                if (!authoriser.wasAdmin()) {
                    LOG.info("User " + credentials.getUsername() + " successfully logged into stats-out.");

                    req.setAttribute("status", user.getProperty("status"));
                    req.setAttribute("vmcount", getCruncherCount(user, datastore));
                    req.setAttribute("weeklytotal", weeklyTotal);
                    req.setAttribute("alltimetotal", (Long) user.getProperty("life_time_credits") + weeklyTotal);
                    req.setAttribute("badgelevelalltime", badgeChecker.getBadgeLevelAllTime());
                    req.setAttribute("badgelevelthisweek", badgeChecker.getBadgeLevelThisWeek());
                    req.setAttribute("togglestatusurl", LiveUrlCreator.getLiveUrl("ops", "toggleuserstatus"));

                    RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/loggedin.jsp");
                    jsp.forward(req, resp);

                } else {
                    LOG.info("Admin " + credentials.getUsername() + " successfully logged into stats-out.");

                    req.setAttribute("updateCodeUrl", LiveUrlCreator.getLiveUrl("ops", "updatecode"));
                    req.setAttribute("uploadInputsUrl", LiveUrlCreator.getLiveUrl("ops", "uploadinputs"));

                    RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/adminpage.jsp");
                    jsp.forward(req, resp);
                }
            } else {
                LOG.info("User " + credentials.getUsername() + " failed to LOG-in to stats-out.");

                req.setAttribute("error_text", "Your LOG-in attempt was unsuccessful, please try again. <br>" +
                        "Your account will be locked upon three failed login attempts in one day.");
                RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/errorpage.jsp");
                jsp.forward(req, resp);
            }
        } catch (EntityNotFoundException e) {
            resp.setStatus(404);
            e.printStackTrace();
        }

    }


    private Long getWeeklyTotal(Entity user, DatastoreService datastore) {

        Entity existingStat = datastore.prepare(new Query("stat")
                .setFilter(Query.CompositeFilterOperator.and(
                        Query.FilterOperator.EQUAL.of("user", user.getKey()),
                        Query.FilterOperator.EQUAL.of("week", MiscUtils.getWeek())
                ))).asSingleEntity();

        if (existingStat == null) {
            return 0L;
        } else {
            return (Long) existingStat.getProperty("credits");
        }
    }

    private int getCruncherCount(Entity user, DatastoreService datastore) {

        /*every cruncher that the user has supplied with a unique "cruncher name" will have its own recent access token,
          so we can determine how many unique crunchers have operated in the last hour.*/

        Query q = new Query("token_link")
            .addProjection(new PropertyProjection("cruncher_name", String.class))
            .setDistinct(true)
            .setFilter(Query.FilterOperator.EQUAL.of("user", user.getKey()));

        return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults()).size();
    }


}
