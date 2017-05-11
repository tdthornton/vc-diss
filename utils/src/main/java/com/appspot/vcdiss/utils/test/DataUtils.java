package com.appspot.vcdiss.utils.test;

import com.appspot.vcdiss.utils.MiscUtils;
import com.appspot.vcdiss.utils.security.SecurityUtils;
import com.google.appengine.api.datastore.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Used only by unit tests to inject dummy data into a test datastore environment.
 */
public class DataUtils {

    private List<Key> users = new ArrayList<>();
    private List<Key> inputs = new ArrayList<>();
    private List<Key> apps = new ArrayList<>();
    private List<Key> stats = new ArrayList<>();

    public void makeSampleData() {


        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Iterable<Entity> inputs_iterable = datastore.prepare(new Query("input")).asIterable();
        for (Entity input : inputs_iterable) {
            datastore.delete(input.getKey());
        }


        clearUsers();


        Iterable<Entity> tokenlinks = datastore.prepare(new Query("token_link")).asIterable();
        for (Entity input : tokenlinks) {
            datastore.delete(input.getKey());
        }


        Iterable<Entity> works = datastore.prepare(new Query("work")).asIterable();
        for (Entity input : works) {
            datastore.delete(input.getKey());
        }


        Iterable<Entity> stats = datastore.prepare(new Query("stat")).asIterable();
        for (Entity input : stats) {
            datastore.delete(input.getKey());
        }


        Iterable<Entity> resetCodes = datastore.prepare(new Query("reset_code")).asIterable();
        for (Entity input : resetCodes) {
            datastore.delete(input.getKey());
        }


        Iterable<Entity> verificationCodes = datastore.prepare(new Query("verification_code")).asIterable();
        for (Entity input : verificationCodes) {
            datastore.delete(input.getKey());
        }


        apps.add(datastore.put(makework()));



        makeStats(datastore, makeUsers(datastore));
        inputs.add(makeSampleInput(3, 0));
        inputs.add(makeSampleInput(3, 3));
        inputs.add(makeSampleInput(2, 2));
        inputs.add(makeSampleInput(0, 1));
        inputs.add(makeSampleInput(0, 0));
        inputs.add(makeSampleInput(3, 3));
        inputs.add(makeSampleInput(3, 0));
        inputs.add(makeSampleInput(3, 0));
        inputs.add(makeSampleInput(3, 0));
        inputs.add(makeSampleInput(3, 0));
        inputs.add(makeSampleInput(3, 0));

        for (int i = 0; i <300; i++) {
            inputs.add(makeSampleInput(0, 0));
        }


    }

    private void makeStats(DatastoreService datastore, Key user) {
        Entity stat = new Entity("stat");
        stat.setProperty("week", MiscUtils.getWeek());
        stat.setProperty("user", user);
        stat.setProperty("credits", 1000);

        stats.add(datastore.put(stat));
    }

    private Entity makework() {
        Entity work = new Entity("work");
        work.setProperty("app", "Super Prime Checker 1.0");
        work.setProperty("code", "import sys\n" +
                "import decimal\n" +
                "\n" +
                "D = decimal.Decimal\n" +
                "n = D(sys.argv[1])\n" +
                "\n" +
                "def mrange(start, stop, step):\n" +
                "    while start < stop:\n" +
                "        yield start\n" +
                "        start += step\n" +
                "\n" +
                "\n" +
                "with decimal.localcontext() as ctx:\n" +
                "    ctx.prec = 12\n" +
                "    x = long(n.sqrt())+1\n" +
                "\n" +
                "\n" +
                "if n == 2:\n" +
                "    print True\n" +
                "if n % 2 == 0 or n <= 1:\n" +
                "    print False\n" +
                "\n" +
                "for divisor in mrange(3, x, 2):\n" +
                "    if n % divisor == 0:\n" +
                "        print False\n" +
                "print True");
        work.setProperty("version", "1");
        work.setProperty("md5", "9346fe35b5518e9a90c633a84dc6d736");
        work.setProperty("coefficient", 1000);


        return work;
    }

    private Key makeUsers(DatastoreService datastore) {
        Key appKey = datastore.prepare(new Query("work")).asSingleEntity().getKey();


        Entity user = new Entity("user");
        user.setProperty("name", "test1");
        user.setProperty("app", appKey);
        user.setProperty("admin", false);
        user.setProperty("status", "start");
        user.setProperty("email_verified", false);
        String salt = SecurityUtils.getNewSalt();
        user.setProperty("password", SecurityUtils.hash("t3heqaNa", salt));
        user.setProperty("salt", salt);
        user.setProperty("email", "test@localtest.org");
        user.setProperty("failed_login_attempts_today", 0);
        user.setProperty("life_time_credits", 1000);


        users.add(datastore.put(user));

        user = new Entity("user");
        user.setProperty("name", "test2");
        user.setProperty("app", appKey);
        user.setProperty("admin", false);
        user.setProperty("status", "start");
        salt = SecurityUtils.getNewSalt();
        user.setProperty("password", SecurityUtils.hash("t3heqaNa", salt));
        user.setProperty("salt", salt);
        user.setProperty("life_time_credits", 1000);
        user.setProperty("failed_login_attempts_today", 0);


        users.add(datastore.put(user));

        user = new Entity("user");
        user.setProperty("name", "test3");
        user.setProperty("app", appKey);
        user.setProperty("admin", false);
        user.setProperty("status", "start");
        salt = SecurityUtils.getNewSalt();
        user.setProperty("password", SecurityUtils.hash("t3heqaNa", salt));
        user.setProperty("salt", salt);
        user.setProperty("life_time_credits", 1000);
        user.setProperty("failed_login_attempts_today", 0);


        users.add(datastore.put(user));

        user = new Entity("user");
        user.setProperty("name", "test4");
        user.setProperty("app", appKey);
        user.setProperty("admin", false);
        user.setProperty("status", "start");
        salt = SecurityUtils.getNewSalt();
        user.setProperty("password", SecurityUtils.hash("t3heqaNa", salt));
        user.setProperty("salt", salt);
        user.setProperty("life_time_credits", 1000);
        user.setProperty("failed_login_attempts_today", 0);


        users.add(datastore.put(user));

        user = new Entity("user");
        user.setProperty("name", "admin1");
        user.setProperty("email", "vcdissowner@gmail.com");
        user.setProperty("email_verified", true);
        user.setProperty("app", appKey);
        user.setProperty("admin", true);
        user.setProperty("status", "start");
        salt = SecurityUtils.getNewSalt();
        user.setProperty("password", SecurityUtils.hash("t3heqaNa", salt));
        user.setProperty("salt", salt);
        user.setProperty("life_time_credits", 1000);
        user.setProperty("failed_login_attempts_today", 0);


        users.add(datastore.put(user));

        user = new Entity("user");
        user.setProperty("name", "test5");
        user.setProperty("app", appKey);
        user.setProperty("admin", false);
        user.setProperty("status", "start");
        salt = SecurityUtils.getNewSalt();
        user.setProperty("password", SecurityUtils.hash("t3heqaNa", salt));
        user.setProperty("salt", salt);
        user.setProperty("life_time_credits", 1000);
        user.setProperty("failed_login_attempts_today", 0);


        Key test5key = datastore.put(user);
        users.add(test5key);

        return test5key;


    }

    public Key makeSampleInput(int distributedToCount, int resultsFromCount) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Key appKey = datastore.prepare(new Query("work")).asSingleEntity().getKey();


        BigInteger newPrime = new BigInteger(24, 1, new Random());


        Entity work = new Entity("input");
        work.setProperty("app", appKey);

        List<String> results = new ArrayList<>();
        List<String> resultsFrom = new ArrayList<>();
        List<String> distributedTo = new ArrayList<>();

        for (int i = 0; i < distributedToCount; i++) {
            distributedTo.add(KeyFactory.keyToString(users.get(i)));
        }

        for (int i = 0; i < resultsFromCount; i++) {
            resultsFrom.add(KeyFactory.keyToString(users.get(i)));
            results.add("true");
        }

        if (resultsFromCount==3) {
            work.setProperty("canonical", true);
        } else {
            work.setProperty("canonical", false);
        }

        for (int i = 0; i < (3-resultsFromCount); i++) {
            resultsFrom.add(null);
            results.add(null);
        }

        for (int i = 0; i < (3-distributedToCount); i++) {
            distributedTo.add(null);
        }



        work.setProperty("input", newPrime.toString());
        work.setProperty("results", results);
        work.setProperty("resultsFrom", resultsFrom);
        work.setProperty("distributedTo", distributedTo);

        work.setProperty("credited", false);

        return datastore.put(work);

    }

    public static void clearUsers() {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Iterable<Entity> users = datastore.prepare(new Query("user")).asIterable();

        for (Entity input : users) {
            datastore.delete(input.getKey());
        }

    }

    public static void clearApps() {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Iterable<Entity> apps = datastore.prepare(new Query("work")).asIterable();

        for (Entity app : apps) {
            datastore.delete(app.getKey());
        }
    }

    public List<Key> getUsers() {
        return users;
    }

    public List<Key> getInputs() {
        return inputs;
    }

    public List<Key> getApps() {
        return apps;
    }

    public List<Key> getStats() {
        return stats;
    }
}
