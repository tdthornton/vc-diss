package com.appspot.vcdiss.statsin.servlets;

import com.appspot.vcdiss.statsin.domain.Stat;
import com.appspot.vcdiss.statsin.exceptions.InputAlreadyCreditedException;
import com.appspot.vcdiss.statsin.exceptions.InputNotSatisfiedException;
import com.appspot.vcdiss.utils.MiscUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.datastore.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Servlet accepts work from the task queue to credit users who provided results to a satisfied input.
 */
public class StatsInServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StatsInServlet.class.getName());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        Stat inboundStat = parseInboundResult(req.getInputStream());

        try {

            LOG.info("Task taken from queue: Crediting users who submitted results to input " + inboundStat.getKey());

            Entity input = datastore.get(KeyFactory.stringToKey(inboundStat.getKey()));



            if ((boolean) input.getProperty("credited")) {
                throw new InputAlreadyCreditedException("Input sent to stats-in that has already paid credit: " + inboundStat.getKey());
            }

            if (!(boolean) input.getProperty("canonical")) {
                throw new InputNotSatisfiedException("Input sent to stats-in that has not been satisfied: " + inboundStat.getKey());
            }



            Entity app = datastore.get((Key) input.getProperty("app"));

            if (app==null) {
                throw new NullPointerException("App related to input sent to stats-in no longer exists: " + inboundStat.getKey());
            }



            List<String> resultsFrom = (List<String>) input.getProperty("resultsFrom");

            for (String keyString : resultsFrom) { //for each user who submitted, increment their stat for this week (or create a new one)

                Key userKey = KeyFactory.stringToKey(keyString);

                Entity existingStat = datastore.prepare(new Query("stat")
                        .setFilter(Query.CompositeFilterOperator.and(
                                Query.FilterOperator.EQUAL.of("user", userKey),
                                Query.FilterOperator.EQUAL.of("week", MiscUtils.getWeek())
                        ))).asSingleEntity();

                if (existingStat == null) { //if the user has no stats from this week, create one for them
                    addNewStatForUser(datastore.get(userKey), (Long) app.getProperty("coefficient"));
                } else {
                    incrementStatForUser(existingStat, (Long) app.getProperty("coefficient"));
                }
            }

            markCredited(input);
            LOG.info("Successfully credited all users for " + inboundStat.getKey());
            resp.setStatus(200);

        } catch (NullPointerException | EntityNotFoundException e) {
            LOG.info("Error processing stat " + inboundStat.getKey() + " couldn't locate entities");
            resp.setStatus(410);
        } catch (InputAlreadyCreditedException | PreparedQuery.TooManyResultsException e) {
            resp.setStatus(407);
        } catch (InputNotSatisfiedException e) {
            resp.setStatus(408);
        }




    }

    private void incrementStatForUser(Entity existingStat, Long appCoefficient) {

        existingStat.setProperty("credits", (Long) existingStat.getProperty("credits") + appCoefficient);

        datastore.put(existingStat);
    }

    private void addNewStatForUser(Entity user, Long appCoefficient) {

        Entity statToWrite = new Entity("stat");

        statToWrite.setProperty("week", MiscUtils.getWeek());
        statToWrite.setProperty("user", user.getKey());
        statToWrite.setProperty("credits", appCoefficient);

        datastore.put(statToWrite);
    }

    private void markCredited(Entity input) {
        input.setProperty("credited", true);
        datastore.put(input);
    }


    private Stat parseInboundResult(ServletInputStream inputStream) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(inputStream, Stat.class);

    }


}
