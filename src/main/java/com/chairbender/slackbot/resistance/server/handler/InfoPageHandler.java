package com.chairbender.slackbot.resistance.server.handler;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Given any request, simply returns an info page
 *
 * Created by chairbender on 11/21/2015.
 */
public class InfoPageHandler extends AbstractHandler{
    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        response.getWriter().println("<p>The Resistance Bot is running. " +
                "Visit <a href=\"https://github.com/chairbender/slackbot-resistance\">https://github.com/chairbender/slackbot-resistance</a> for more information. </p>");
    }
}
