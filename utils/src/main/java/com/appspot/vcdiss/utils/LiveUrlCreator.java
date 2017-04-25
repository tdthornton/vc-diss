package com.appspot.vcdiss.utils;

import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility class that returns inter-module URLs that are safe for live operation (-dot- instead of first two '.', and https).
 * Curiously there is nothing built-in to the GAE SDK for this.
 */
public class LiveUrlCreator {
    public static String getLiveUrl(String module, String subPage) throws MalformedURLException {
        String opsUrl = "";

        ModulesService modules = ModulesServiceFactory.getModulesService();

        if (modules.getModules().size() > 1) {
            URL ops = new URL("https://" + modules.getVersionHostname(module, null) + "/"); //TODO FINALISE HTTPS

            opsUrl = ops.toExternalForm();

            opsUrl = opsUrl.replaceFirst("(\\.)", "-dot-");
            opsUrl = opsUrl.replaceFirst("(\\.)", "-dot-");
            opsUrl = opsUrl + subPage;
        }

        return opsUrl;

    }
}
