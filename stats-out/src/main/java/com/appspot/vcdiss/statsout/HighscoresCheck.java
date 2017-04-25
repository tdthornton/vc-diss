package com.appspot.vcdiss.statsout;

import com.google.appengine.api.datastore.*;


import com.appspot.vcdiss.utils.MiscUtils;

import java.util.List;
import java.util.logging.Logger;

/**
 * Utility class that can tell you if a user is in the top 100, 50 or 10 contributors this week OR all time.
 * Accuracy is key as this is how badges are awarded.
 */
public class HighscoresCheck {

    private static final Logger LOG = Logger.getLogger(HighscoresCheck.class.getName());


    private Entity user;

    public HighscoresCheck(Entity user) {
        this.user = user;
    }

    public String getBadgeLevelAllTime() {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

        List<Entity> statsInDb = ds.prepare(new Query("user") //get top 100 contributors
                .addSort("life_time_credits", Query.SortDirection.DESCENDING)
                .addProjection(new PropertyProjection("name", String.class))
        ).asList(FetchOptions.Builder.withLimit(100));

        int rank = 100+1;

        for (int i = 0; i < statsInDb.size(); i++) {
            if (statsInDb.get(i).getProperty("name").equals(user.getProperty("name"))) {
                rank=i;
                break;
            }
        }

        return getBadgeLevelText(rank);
    }

    private String getBadgeLevelText(int rank) {
        if (rank>100) { //filter down which section the user falls in, but don't reveal their exact rank
            return "STANDARD";
        } else if (rank >50) {
            return "BRONZE";
        } else if (rank>10) {
            return "SILVER";
        } else {
            return "GOLD";
        }
    }

    public String getBadgeLevelThisWeek() throws EntityNotFoundException {
        DatastoreService ds = DatastoreServiceFactory.getDatastoreService();


        List<Entity> statsInDb = ds.prepare(new Query("stat") //get top 100 contributors of the week
                .setFilter(new Query.FilterPredicate("week", Query.FilterOperator.EQUAL, MiscUtils.getWeek()))
                .addSort("credits", Query.SortDirection.DESCENDING)
                .addProjection(new PropertyProjection("user", Key.class))
        ).asList(FetchOptions.Builder.withLimit(100));

        int rank = 100+1;

        for (int i = 0; i < statsInDb.size(); i++) {
            Entity statUser = ds.get((Key) statsInDb.get(i).getProperty("user"));

            if (statUser.getProperty("name").equals(user.getProperty("name"))) {
                rank=i;
                break;
            }
        }

        return getBadgeLevelText(rank);
    }


}
