package com.appspot.vcdiss.utils.servlets;

import com.appspot.vcdiss.utils.LiveUrlCreator;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that provides HTML for the "about vc-diss" page, as stats-out is the default module.
 */

public class AboutServlet extends HttpServlet {





    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {

        resp.setContentType("text/html");
        req.setAttribute("footerUrls", LiveUrlCreator.getFooterUrls());

        RequestDispatcher jsp = req.getRequestDispatcher("/WEB-INF/AboutPage.jsp");
        jsp.forward(req, resp);

    }



}
