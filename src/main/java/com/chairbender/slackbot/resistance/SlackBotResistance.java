package com.chairbender.slackbot.resistance;

import com.chairbender.slackbot.resistance.server.handler.InfoPageHandler;
import com.chairbender.slackbot.resistance.util.GameMessageUtil;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.eclipse.jetty.server.Server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kwhip_000 on 11/18/2015.
 */
public class SlackBotResistance {

    //list of bots running games in each channel
    private static Map<String,ResistanceBot> channelIDsToResistanceBots = new HashMap<>();

    /**
     *
     * @param args
     * 0 - slack api token
     * 1 - name you want this bot to respond to (i.e. "resistbot" or whatever you chose when creating the bot)
     * 2 - 'true' or 'false' - whether you want to set it to testing mode, where one player controls multiple characters. False if omitted.
     * 3 - port to run bind to for serving up the info HTML page. won't run a server if this is omitted
     */
    public static void main(String[] args) throws Exception {
        String apiToken = args[0];
        final String botName = args[1];
        boolean isTesting = false;
        if (args.length >= 3) {
            isTesting = args[2].equalsIgnoreCase("true");
        }
        final boolean testingMode = isTesting;
        SlackSession session = SlackSessionFactory.createWebSocketSlackSession(apiToken);
        //add a listener to pick up when to start a game
        session.addMessagePostedListener(new SlackMessagePostedListener() {
            @Override
            public void onEvent(final SlackMessagePosted event, SlackSession session) {
                SlackUser botUser = session.findUserByUserName(botName);
                //start the game when instructed
                if (event.getMessageContent().startsWith(botName) ||
                        (botUser != null && GameMessageUtil.atMessageToUID(event.getMessageContent()).startsWith(botUser.getId()))) {
                    //check for the help command
                    if (event.getMessageContent().contains("help")) {
                        session.sendMessage(event.getChannel(),
                                "'" + botName + " stop all games' will stop all games in progress.\n" +
                                        "'" + botName + " stop this game' will stop the game in the current channel.",null);
                    } else if (event.getMessageContent().contains("stop all games")) {
                        //stop all games in progress.
                        channelIDsToResistanceBots.clear();

                    } else if (event.getMessageContent().contains("stop this game")) {
                        //stop the game in this channel if one is running
                        if (channelIDsToResistanceBots.containsKey(event.getChannel().getId())) {
                            channelIDsToResistanceBots.remove(event.getChannel().getId());
                        } else {
                            session.sendMessage(event.getChannel(),"There is no game running in this channel.",null);
                        }
                    } else if (event.getMessageContent().contains("start")) {
                        //determine if a game is running right now in this channel
                        if (channelIDsToResistanceBots.containsKey(event.getChannel().getId()) &&
                                channelIDsToResistanceBots.get(event.getChannel().getId()).isGameRunning()) {
                            session.sendMessage(event.getChannel(), "A game is already running here.", null);
                        } else {
                            //no game is running, start a game
                            ResistanceBot resistanceBot = new ResistanceBot(session, botName, event.getChannel(), testingMode, event.getSender().getUserName(), new GameOverCallback() {
                                @Override
                                public void gameIsOver() {
                                    //remove from the bot map
                                    channelIDsToResistanceBots.remove(event.getChannel().getId());
                                }
                            });
                            session.sendMessage(event.getChannel(), "A game of The Resistance is starting. It's for 5 to 10 players.\nType 'join' to join.\n" +
                                    "Type 'done' when all players are done joining.", null);
                            channelIDsToResistanceBots.put(event.getChannel().getId(), resistanceBot);
                        }
                    } else {
                        //let the bot handle it if a game is in progress, otherwise explain how to start
                        if (channelIDsToResistanceBots.containsKey(event.getChannel().getId()) &&
                                channelIDsToResistanceBots.get(event.getChannel().getId()).isGameRunning()) {
                            //allow all running bots to handle the message
                            for (ResistanceBot bot : channelIDsToResistanceBots.values()) {
                                bot.onMessagePosted(event);
                            }
                        } else {
                            session.sendMessage(event.getChannel(), "Say '" + botName + " start' to start a game of The Resistance.\n" +
                                    "Say '" + botName + " help' for a list of commands.", null);
                        }
                    }
                } else {
                    //allow all running bots to handle the message
                    for (ResistanceBot bot : channelIDsToResistanceBots.values()) {
                        bot.onMessagePosted(event);
                    }
                }
            }
        });
        session.connect();

        //start up a server that serves up a small information page if a port is specified
        if (args.length == 4) {
            String port = args[3];
            Server server = new Server(Integer.parseInt(port));
            server.setHandler(new InfoPageHandler());
            server.start();
            server.join();
        }
    }

}
