package com.appspot.vcdiss.utils.servlets;

import com.appspot.vcdiss.utils.LiveUrlCreator;
import com.appspot.vcdiss.utils.MiscUtils;
import com.appspot.vcdiss.utils.domain.Stat;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.modules.ModulesService;
import com.google.appengine.api.modules.ModulesServiceFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Servlet that provides HTML for the homepage, as stats-out is the default module.
 * This is because the homepage primarily shows statistics, so stats-out handling it makes the most sense for the data access scaling patterns.
 */

public class StatsOutServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(StatsOutServlet.class.getName());

    private static final int STATS_ON_FRONT_PAGE = 10;

    ModulesService modules = ModulesServiceFactory.getModulesService();


    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        if (modules !=null && modules.getModules().size()>1) {
            URL workouturl = new URL("http://" + modules.getVersionHostname("work-out", null) + "/");
            URL resultsinurl = new URL("http://" + modules.getVersionHostname("results-in", null) + "/");
            URL statsinurl = new URL("http://" + modules.getVersionHostname("stats-in", null) + "/");
            URL statsouturl = new URL("http://" + modules.getVersionHostname("default", null) + "/");
            URL opsurl = new URL("http://" + modules.getVersionHostname("ops", null) + "/");

            resp.getWriter().println("<html><body><b>STATS-OUT!</b><br><br> Modules: " +
                    "<li><a href=\"" + workouturl.toExternalForm() + "\"> WORK-OUT: </a>" + workouturl.toExternalForm() + "</li>" +
                    "<li><a href=\"" + resultsinurl.toExternalForm() + "\"> RESULTS-IN: </a>" + resultsinurl.toExternalForm() + "</li>" +
                    "<li><a href=\"" + opsurl.toExternalForm() + "\"> OPS</a> - <a href=\"" + opsurl.toExternalForm() + "clearinputs\"> MAKE INPUTS</a> - : <a href=\"" + opsurl.toExternalForm() + "togglestatus\"> TOGGLESTATUS</a> - : <a href=\"" + opsurl.toExternalForm() + "makeuser\"> MAKE USERS</a>- : <a href=\"" + opsurl.toExternalForm() + "clearinputs\"> RESET ALL</a></li>" +
                    "<li><a href=\"" + statsinurl.toExternalForm() + "\"> STATS-IN: </a>" + statsinurl.toExternalForm() + "</li>" +
                    "<li><a href=\"" + statsouturl.toExternalForm() + "\"> STATSOUT</a></li>" +
                    "</html></body>");

        }



        List<Stat> stats = new ArrayList<>();

        List<Entity> statsInDb = datastore.prepare(new Query("stat")
                .setFilter(new Query.FilterPredicate("week", Query.FilterOperator.EQUAL, MiscUtils.getWeek()))
                .addSort("credits", Query.SortDirection.DESCENDING)
        ).asList(FetchOptions.Builder.withLimit(STATS_ON_FRONT_PAGE));


        LOG.info("Homepage requested: loaded " + statsInDb.size() + " top contributors");

        for (Entity entity : statsInDb) {
            try {
                Entity user = datastore.get((Key) entity.getProperty("user"));

                stats.add(new Stat((String) user.getProperty("name"), (String) entity.getProperty("week"), (Long) entity.getProperty("credits")));
            } catch (EntityNotFoundException e) {
                e.printStackTrace();
            }

        }

        req.setAttribute("stats", stats);
        req.setAttribute("statscount", stats.size());
        req.setAttribute("registerurl", LiveUrlCreator.getLiveUrl("ops", "register"));
        req.setAttribute("reseturl", LiveUrlCreator.getLiveUrl("ops", "resetpassword"));
        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());

        resp.setContentType("text/html");

        RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/Homepage.jsp");
        jsp.forward(req, resp);

    }


}
