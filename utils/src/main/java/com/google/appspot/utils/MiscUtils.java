package com.google.appspot.utils;


import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Utility class to handle various things that multiple modules need, to avoid code repetition.
 */
public class MiscUtils {

    public static String getWeek() {
        GregorianCalendar calendar = new GregorianCalendar();
        return calendar.get(Calendar.WEEK_OF_YEAR) + "/" + calendar.get(Calendar.YEAR);
    }


    public static boolean tokenExpired(Date date) {
        Date expiryThreshold = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1));
        return date.before(expiryThreshold);
    }






}
