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



import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Servlet triggered at regular intervals (configured in cron.xml) to clear expired data.
 */
public class CronServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ResetPasswordServlet.class.getName());

    private final int MAX_SIMULTANEOUS_READS = 1000;


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {


        LOG.info("Expired data cron triggered.");

        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        Date expiryThreshold = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));

        List<Entity> keys = new ArrayList<>();
        List<Entity> users = new ArrayList<>();
        int results = 0;
        int totalResults = 0;

        

        do { //continuously get and delete the next 1000 tokens with expired dates
            totalResults+=results;
            results = 0;
            Query q = new Query("token_link").setKeysOnly().setFilter(new Query.FilterPredicate("issued", Query.FilterOperator.LESS_THAN, expiryThreshold));
            keys = ds.prepare(q).asList(FetchOptions.Builder.withLimit(MAX_SIMULTANEOUS_READS));

            results = keys.size();

            for (Entity key : keys) {
                ds.delete(key.getKey());
                results++;
            }

            totalResults+=results;

            keys.clear();

        } while (results>0);


        do { //continuously get and delete the next 1000 email verification codes with expired dates
            totalResults+=results;
            results = 0;
            Query q = new Query("verification_code").setKeysOnly().setFilter(new Query.FilterPredicate("issued", Query.FilterOperator.LESS_THAN, expiryThreshold));
            keys = ds.prepare(q).asList(FetchOptions.Builder.withLimit(MAX_SIMULTANEOUS_READS));

            results = keys.size();

            for (Entity key : keys) {
                ds.delete(key.getKey());
                results++;
            }

            totalResults+=results;

            keys.clear();

        } while (results>0);


        do { //continuously get and delete the next 1000 password reset codes with expired dates
            totalResults+=results;
            results = 0;
            Query q = new Query("reset_code").setKeysOnly().setFilter(new Query.FilterPredicate("issued", Query.FilterOperator.LESS_THAN, expiryThreshold));
            keys = ds.prepare(q).asList(FetchOptions.Builder.withLimit(MAX_SIMULTANEOUS_READS));

            results = keys.size();

            for (Entity key : keys) {
                ds.delete(key.getKey());
                results++;
            }

            totalResults+=results;

            keys.clear();

        } while (results>0);


        do { //continuously get and reset the next 1000 users with failed logins (unlock accounts)
            totalResults+=results;
            results = 0;
            Query q = new Query("user").setFilter(new Query.FilterPredicate("failed_login_attempts_today", Query.FilterOperator.GREATER_THAN_OR_EQUAL, 3));
            users = ds.prepare(q).asList(FetchOptions.Builder.withLimit(MAX_SIMULTANEOUS_READS));

            results = users.size();

            for (Entity user : users) {
                user.setProperty("failed_login_attempts_today", 0);
                ds.put(user);
                results++;
            }

            totalResults+=results;

            users.clear();

        } while (results>0);

        LOG.info("Expired data cron completed. Deleted " + totalResults + " expired entities.");



    }




}
