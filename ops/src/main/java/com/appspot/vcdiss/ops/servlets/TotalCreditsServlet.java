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


import com.appspot.vcdiss.utils.MiscUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A servlet triggered by cron to total all credits earned this week into user's lifetime totals.
 */
public class TotalCreditsServlet extends HttpServlet {


    private static final Logger log = Logger.getLogger(TotalCreditsServlet.class.getName());
    private static final int MAX_SIMULTANEOUS_READS = 1000;


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {


        log.info("Lifetime total credits cron triggered.");

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();


        List<Entity> stats = new ArrayList<>();
        int results = 0;
        int offset = 0;

        do { //loop through 1000 stats from this week at a time, adding their value to their user's lifetime credits
            offset+=results;
            results = 0;
            Query q = new Query("stat").setFilter(new Query.FilterPredicate("week", Query.FilterOperator.EQUAL, MiscUtils.getWeek()));

            stats = ds.prepare(q).asList(FetchOptions.Builder.withLimit(MAX_SIMULTANEOUS_READS).offset(offset));

            results = stats.size();

            for (Entity stat : stats) {
                try {
                    Entity user = ds.get((Key) stat.getProperty("user"));
                    Long credits = (Long) user.getProperty("life_time_credits");
                    credits=credits+(Long)stat.getProperty("credits");
                    user.setProperty("life_time_credits", credits);
                    ds.put(user);
                } catch (EntityNotFoundException e) {
                    e.printStackTrace();
                }
                results++;
            }

            stats.clear();

        } while (results>0);

        log.info("Lifetime total credits cron finished.");



    }






}
