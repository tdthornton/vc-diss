package com.appspot.vcdiss.ops.adminhelpers;

import com.appspot.vcdiss.utils.test.DataUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet provides the HTML for the counter web page.
 */
public class ClearInputs extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.getWriter().println("YOU HAVE REACHED OPS/clearinputs");

        new DataUtils().makeSampleData();

    }

}
