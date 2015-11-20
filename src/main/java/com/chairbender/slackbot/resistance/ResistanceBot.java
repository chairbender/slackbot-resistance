package com.chairbender.slackbot.resistance;

import com.chairbender.slackbot.resistance.game.model.Player;
import com.chairbender.slackbot.resistance.game.model.PlayerCharacter;
import com.chairbender.slackbot.resistance.game.state.PreGameState;
import com.chairbender.slackbot.resistance.model.ResistanceMessage;
import com.chairbender.slackbot.resistance.util.GameMessageUtil;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates all of the logic of the bot.
 *
 *
 * Created by chairbender on 11/18/2015.
 */
//word in the sentence as the actual username
public class ResistanceBot {
    private BotState botState;

    /**
     *
     * @param session session to use to connect to slack
     * @param botName name of the bot configured in slack
     * @param isTestingMode whether to enable testing mode, where one player can play as many other player. In testing
     *                      mode, the actual slack username is ignored and instead the first word is treated as the username.
     */
    public ResistanceBot(SlackSession session, String botName, boolean isTestingMode) {
        botState = new BotState(session,botName,isTestingMode);
    }

    /**
     * Starts running and listening for messages
     */
    public void run() throws IOException {
        botState.setMessagePostedListener(new SlackMessagePostedListener() {
            @Override
            public void onEvent(SlackMessagePosted postedEvent, SlackSession session) {
                ResistanceMessage resistanceMessage = ResistanceMessage.fromSlackMessagePosted(postedEvent, botState.isTestingMode());

                String message = resistanceMessage.getMessage();
                SlackChannel channel = resistanceMessage.getChannel();
                //ignore own messages
                if (postedEvent.getSender().getUserName().equals(botState.getBotName())) {
                    return;
                }
                if ( botState.getState().equals(BotState.State.WAITING_TO_START)){
                    //start the game when instructed
                    if (postedEvent.getMessageContent().startsWith(botState.getBotName())) {
                        if (message.contains("start")) {
                            botState.startRegistration(channel);
                            botState.setTestingUserName(postedEvent.getSender().getUserName());
                            botState.sendPrompt("A game of The Resistance is starting. Type 'join' to join in. " +
                                    "Type 'done' when everyone who wants to play has joined.");
                        } else {
                            session.sendMessage(channel, "Say '" + botState.getBotName() + " start' to start a game of The Resistance.", null);
                        }
                    }

                }else if (botState.getState().equals(BotState.State.REGISTRATION)) {
                    if (message.contains("join")) {
                        if (!botState.registerPlayer(resistanceMessage.getSender())) {
                            botState.sendPublicMessageToPlayer(resistanceMessage.getSender(), "You have already joined.");
                        } else {
                            botState.sendPublicMessageToPlayer(resistanceMessage.getSender(), " is playing.");
                        }
                    } else if (message.contains("done")) {
                        //start the game, send out all the player roles
                        startGame();

                    }
                } else if (botState.getState().equals(BotState.State.PICK_TEAM)) {
                    //only listen to the leader right now
                    if (resistanceMessage.getSender().equals(botState.getLeaderUserName())) {
                        if (resistanceMessage.getMessage().startsWith("pick")) {
                            String chosenUsername = resistanceMessage.getMessage().replace("pick ", "").trim();
                            //confirm it is a player in the game
                            if (!botState.isPlayer(chosenUsername)) {
                                botState.sendPublicMessageToPlayer(resistanceMessage.getSender(),
                                        "I don't recognize the player called " + chosenUsername + ".");
                            } else if (botState.isPlayerOnTeam(chosenUsername)) {
                                //confirm the player isn't already chosen
                                botState.sendPublicMessageToPlayer(resistanceMessage.getSender(),
                                        "That player, " + chosenUsername + ", is already on the team.");
                            } else if (botState.isTeamFull()) {
                                //confirm there aren't too many team members
                                botState.sendPublicMessageToPlayer(resistanceMessage.getSender(),
                                        "The team is already full. Please 'drop' somebody first.");
                            } else {
                                //add the player and report the current team
                                botState.addTeamMember(chosenUsername);
                            }

                            botState.sendPublicMessage("Added " + chosenUsername + " to the team.\n");
                            reportTeamSelection();
                        } else if (resistanceMessage.getMessage().startsWith("drop")) {
                            String chosenUsername = resistanceMessage.getMessage().replace("drop ", "").trim();
                            if (chosenUsername.isEmpty()) {
                                botState.sendPublicMessage("Dropping all members of the current team.");
                            } else {
                                if (!botState.isPlayer(chosenUsername)) {
                                    //confirm it is a player in the game
                                    botState.sendPublicMessageToPlayer(resistanceMessage.getSender(),
                                            "I don't recognize the player called " + chosenUsername + ".");
                                } else if (!botState.isPlayerOnTeam(chosenUsername)) {
                                    //confirm the player is on the team
                                    botState.sendPublicMessageToPlayer(resistanceMessage.getSender(),
                                            "That player, " + chosenUsername + ", is not on the team.");

                                } else {
                                    //drop the player and report the current team./
                                    botState.removeTeamMember(chosenUsername);
                                }
                            }
                            reportTeamSelection();
                        } else if (resistanceMessage.getMessage().startsWith("done")) {
                            //check that the team is valid
                            if (botState.isTeamFull()) {
                                //advance to the next state with the team locked in
                                pickTeam();
                            } else {
                                botState.sendPublicMessageToPlayer(resistanceMessage.getSender(),
                                        "The team has only " + botState.getTeamSize() + " members." +
                                                " You need " + botState.getRequiredTeamSize());
                            }
                        }
                    }
                } else if (botState.getState().equals(BotState.State.VOTE_TEAM)) {
                    //check if this is a direct message
                    if (resistanceMessage.getChannel().isDirect()) {
                        String senderUserName = resistanceMessage.getSender();
                        String vote = resistanceMessage.getMessage().replace(senderUserName, "").trim();
                        //check if they haven't already voted
                        if (botState.hasPlayerVoted(senderUserName)) {
                            botState.sendPrivateMessageToPlayer(senderUserName,
                                    "You have already voted.");
                        } else if (vote.equalsIgnoreCase("no") || vote.equalsIgnoreCase("yes")) {
                            botState.sendPrivateMessageToPlayer(senderUserName,
                                    "Please vote only 'yes' or 'no'.");
                        } else {
                            //register the vote
                            botState.placeVote(senderUserName,vote.equalsIgnoreCase("yes"));
                            botState.sendPrivateMessageToPlayer(senderUserName, "Thank you. Your vote has been accepted.");
                            if (botState.allVotesSubmitted()) {
                                //done, lock in the votes
                                voteTeam();
                            }
                        }

                    }
                }

            }
        });
        botState.connectToSlack();

    }

    private synchronized BotState getBotState() {
        return botState;
    }

    /**
     * accept the current vote for the team. Change the leader or start the mission.
     */
    private void voteTeam() {

    }

    /**
     * starts the game using the usernames in playerUsernames. Let everyone know their roles. Let the
     * current leader know who they are
     */
    private void startGame() {
        botState.startGame();

        botState.sendPrompt("The game has begun. The players are " + GameMessageUtil.listPeople(botState.getPlayerUserNames()));

        Set<PlayerCharacter> spies = botState.getSpies();
        for (PlayerCharacter gameCharacter : botState.getPlayerCharacters()) {
            //tell the roles
            if (gameCharacter.isResistance()) {
                botState.sendPrivateMessageToPlayer(gameCharacter.getUserName(), "You are a member of the resistance. If " +
                        " three missions succeed, you win! You're" +
                        " on the side of good. Hooray for you!");
            } else {
                botState.sendPrivateMessageToPlayer(gameCharacter.getUserName(), "You are a spy along with " +
                        GameMessageUtil.listOtherPeople(spies, gameCharacter.getUserName()) + ".");
            }
        }

        //announce the leader
        botState.sendPrompt("Attention " + botState.getLeaderUserName() + "! You are the current leader. \n" +
                "Pick " + botState.getRequiredTeamSize() + " people to be on the team for the next mission.\n" +
                "Use 'pick <username>' to add someone to the team. \nUse 'drop <username>' to remove them, " +
                "or just say 'drop' to remove everyone. Say 'done' to lock in your choices and let everyone vote on your choice." +
                "\nRemember, you " +
                "can pick yourself as a team member.");
    }

    /**
     * locks in the current team selection and moves to the next state
     */
    private void pickTeam() {
        reportTeamSelection();
        botState.sendPrompt("The team has been set. Everybody, vote on whether you want this team to run the mission.\n" +
                "Send me a direct message of 'no' or 'yes' to vote.\n" +
                "If the majority votes yes, the mission will continue.\n" +
                "If it ties or a majority votes no, the team leader will be advanced to the next player in the rotation.\n" +
                "I will announce all votes once they are all submitted.");
        botState.sendPublicMessage("The leader rotation is " + GameMessageUtil.listOrder(botState.getPlayerCharacters()));
    }

    /**
     * reports the currently selected team members in the public channel
     */
    private void reportTeamSelection() {
        if (botState.getTeamSize() == 0) {
            botState.sendPublicMessage("The team is empty.");
        } else if (botState.getTeamSize() == 1) {
            botState.sendPublicMessage("Only " + botState.getTeam().iterator().next() + " is on the team.");
        } else {
            botState.sendPublicMessage("The current team is " + GameMessageUtil.listPeople(botState.getTeam()));
        }
    }

}
