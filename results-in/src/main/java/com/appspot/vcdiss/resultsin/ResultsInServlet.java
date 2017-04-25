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

package com.appspot.vcdiss.resultsin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.modules.ModulesServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;


import com.appspot.vcdiss.utils.MiscUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that accepts outputs of computed inputs, and saves them as results if they meet certain criteria.
 */
public class ResultsInServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ResultsInServlet.class.getName());
    private static final int CANONICAL_THRESHOLD = 3;


    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        InboundResult inboundResult = parseInboundResult(req.getInputStream());


        try {

            Entity user = authenticateUser(req.getHeader("X-Auth-Token"), datastore);

            if (user == null) {
                throw new SecurityException();
            }

            PreparedQuery pq2 = datastore.prepare(new Query("input") //find the original input in the datastore
                    .setFilter(Query.CompositeFilterOperator.and(
                            Query.FilterOperator.EQUAL.of("app", user.getProperty("app")),
                            Query.FilterOperator.EQUAL.of("input", String.valueOf(inboundResult.getInput()))
                    )));


            Entity inputFound = pq2.asSingleEntity();

            if (inputFound == null) {
                throw new NullPointerException("No input found for inbound result " + inboundResult.getResult() + " for app " + user.getProperty("app"));
            }

            if ((boolean) inputFound.getProperty("canonical")) {
                throw new ResultSatisfiedException("Received a new result for an input that has already been satisfied with a canonical result." +
                        " Usually this is just the fast distribution of work-out rather than any error.");
            }

            List<String> existingResults = (List<String>) inputFound.getProperty("results");
            List<String> existingResultsFrom = (List<String>) inputFound.getProperty("resultsFrom");
            List<String> distributedTo = (List<String>) inputFound.getProperty("distributedTo");

            if (existingResultsFrom.contains(KeyFactory.keyToString(user.getKey()))) {
                //don't allow a user to submit two results for a single input.
                throw new UserRepetitionException("User " + user.getProperty("name") + " already submitted a result for this input.");
            }

            if (!distributedTo.contains(KeyFactory.keyToString(user.getKey()))) {
                //don't allow a user to submit a result to an input we never distributed to them.
                throw new PotentialFoulPlayException("User: " + user.getProperty("name") + " was never sent this input.");
            }


            if (isDisagreement(existingResults, inboundResult.getResult())) {
                //handle conflicts in inbound vs existing results
                existingResults = reset(existingResults);
                existingResultsFrom = reset(existingResultsFrom);
            }

            List<String> newResults = addNewResult(existingResults, inboundResult.getResult());
            List<String> newResultsFrom = addNewResult(existingResultsFrom, KeyFactory.keyToString(user.getKey()));

            inputFound.setProperty("results", newResults);
            inputFound.setProperty("resultsFrom", newResultsFrom);


            if (achievedCanonical(newResults)) { //if CANONICAL_THRESHOLD consecutive results match, credit the users with new stats
                inputFound.setProperty("canonical", true);
                inputFound.setProperty("canonicalAchieved", new Date());
                datastore.put(inputFound);
                notifyStatsService(inputFound);
            } else {
                datastore.put(inputFound);
            }

            LOG.info("Successfully processed new result from " + user.getProperty("name") + " for " + user.getProperty("app"));
            resp.setStatus(200);

        } catch (EntityNotFoundException | NullPointerException e) {
            e.printStackTrace();
            resp.setStatus(410);
        } catch (ResultSatisfiedException e) {
            e.printStackTrace();
            resp.setStatus(405);
        } catch (UserRepetitionException e) {
            e.printStackTrace();
            resp.setStatus(408);
        } catch (SecurityException | PotentialFoulPlayException e) {
            e.printStackTrace();
            resp.setStatus(403);
        }

    }



    private void notifyStatsService(Entity inputFound) throws IOException {
        OutboundStat outboundStat = new OutboundStat(KeyFactory.keyToString(inputFound.getKey()));
        String outboundJson = mapOutboundStatToJson(outboundStat);

        LOG.info("Adding input " + outboundStat.getKey() + " to stats-in queue for crediting");

        //outbound json (telling the receiving service which input to credit) goes onto a queue, to only be processed in batches
        Queue q = QueueFactory.getDefaultQueue();
        q.add(TaskOptions.Builder.withPayload(outboundJson).header("Host",
                ModulesServiceFactory.getModulesService().getVersionHostname("stats-in", null)));
    }

    private String mapOutboundStatToJson(OutboundStat outboundStat) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(outboundStat);
    }

    private List<String> reset(List<String> list) {
        for (int i = 0; i < list.size(); i++) {
            list.set(i, null);
        }
        return list;
    }

    private Entity authenticateUser(String header, DatastoreService datastore) throws EntityNotFoundException {
        Entity tokenlink = datastore.prepare(
                new Query("token_link")
                        .setFilter(new Query.FilterPredicate("token", Query.FilterOperator.EQUAL, header)))
                .asSingleEntity();

        if (tokenlink==null) {
            return null;
        }

        if (MiscUtils.tokenExpired((Date) tokenlink.getProperty("issued"))) {
            datastore.delete(tokenlink.getKey());
            LOG.info("Rejected attempted use of expired token");
            return null;
        }

        return datastore.get((Key) tokenlink.getProperty("user"));
    }


    private InboundResult parseInboundResult(ServletInputStream inputStream) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, InboundResult.class);

    }


    private boolean achievedCanonical(List<String> newResults) {

        //have CANONICAL_THRESHOLD consecutive matching results been received?
        if (Collections.frequency(newResults, newResults.get(0)) == CANONICAL_THRESHOLD) {
            return true;
        }

        return false;
    }


    private List<String> addNewResult(List<String> oldResults, String result) {

        //preserve length of list, add new result to nearest null
        for (int i = 0; i < oldResults.size(); i++) {
            if (oldResults.get(i) == null) {
                oldResults.set(i, result);
                break;
            }
        }

        return oldResults;
    }


    private boolean isDisagreement(List<String> oldResults, String result) {

        //only need to check 0 as the list only gets longer if they all match anyway
        if (oldResults.get(0) != null && !oldResults.get(0).equals(result)) { //will need to get to CANONICAL_THRESHOLD results in a row to be left alone
            return true;
        }
        return false;
    }


}
