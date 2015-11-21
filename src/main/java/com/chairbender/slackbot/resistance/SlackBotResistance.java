package com.chairbender.slackbot.resistance;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

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
        //add a listener to pick up when to start a game
        session.addMessagePostedListener(new SlackMessagePostedListener() {
            @Override
            public void onEvent(SlackMessagePosted event, SlackSession session) {
                //start the game when instructed
                if (event.getMessageContent().startsWith(botName)) {
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
                        session.sendMessage(event.getChannel(), "Say '" + botName + " start' to start a game of The Resistance.\n" +
                                "Say 'resistbot help' for a list of commands.", null);
                    }
                } else {
                    //allow all running bots to handle the message
                    for (ResistanceBot bot : channelIDsToResistanceBots.values()) {
                        bot.onMessagePosted(event, session);
                    }
                }
            }
        });
        session.connect();
    }

}
