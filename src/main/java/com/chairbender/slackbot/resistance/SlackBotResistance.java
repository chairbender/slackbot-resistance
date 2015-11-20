package com.chairbender.slackbot.resistance;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import java.io.IOException;

/**
 * Created by kwhip_000 on 11/18/2015.
 */
public class SlackBotResistance {

    /**
     *
     * @param args
     * 0 - slack team name (yourteam.slack.com)
     * 1 - slack api token
     * 2 - name you want this bot to respond to (i.e. "resistbot" or whatever you chose when creating the bot)
     * 3 - 'true' or 'false' - whether you want to set it to testing mode, where one player controls multiple characters
     */
    public static void main(String[] args) throws IOException {
        String apiToken = args[1];
        String botName = args[2];
        boolean testingMode = args[3].equalsIgnoreCase("true");

        SlackSession session = SlackSessionFactory.createWebSocketSlackSession(apiToken);
        ResistanceBot resistanceBot = new ResistanceBot(session,botName,testingMode);
        resistanceBot.run();
    }

}
