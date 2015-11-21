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
 * Encapsulates all of the logic of the bot. Represents a single bot running a game in a single public channel. Not
 * intended to run multiple games. This bot does NOT directly create a listener with the websocket. It relies on
 * a parent class using getSlackMessagePostedListener() and invoking that when necessary.
 * <p>
 * <p>
 * <p>
 * Created by chairbender on 11/18/2015.
 */
//word in the sentence as the actual username
public class ResistanceBot {
    private final GameOverCallback gameOverCallback;
    private BotState botState;

    /**
     * @param session          session to use to connect to slack
     * @param botName          name of the bot configured in slack
     * @param publicChannel    channel to run the game in
     * @param isTestingMode    whether to enable testing mode, where one player can play as many other player. In testing
     *                         mode, the actual slack username is ignored and instead the first word is treated as the username.
     * @param testingUserName  username of user who is running the tes
     * @param gameOverCallback callback to execute when the game is over
     */
    public ResistanceBot(SlackSession session, String botName, SlackChannel publicChannel, boolean isTestingMode, String testingUserName, GameOverCallback gameOverCallback) {
        botState = new BotState(session, botName, publicChannel, isTestingMode, testingUserName);
        this.gameOverCallback = gameOverCallback;
        botState.startRegistration();
    }

    /**
     * Make sure this gets invoked when a slack message occurs. Causes this bot to listen and respond to messages
     */
    public void onMessagePosted(SlackMessagePosted postedEvent, SlackSession session) {
        ResistanceMessage resistanceMessage = ResistanceMessage.fromSlackMessagePosted(postedEvent, botState.isTestingMode());

        String message = resistanceMessage.getMessage();
        SlackChannel channel = resistanceMessage.getChannel();
        //ignore own messages, messages in other public channels (unless they are direct)
        if (postedEvent.getSender().getUserName().equals(botState.getBotName()) ||
                (!postedEvent.getChannel().isDirect() && !postedEvent.getChannel().getId().equals(botState.getPublicChannel().getId()))) {
            return;
        }
        //Report the current state and the last prompt if the message starts with the bot name and a game
        //is in progress
        if (message.startsWith(botState.getBotName()) &&
                !botState.getState().equals(BotState.State.WAITING_TO_START) &&
                !botState.getState().equals(BotState.State.REGISTRATION)) {
            botState.remind();
        }
        if (botState.getState().equals(BotState.State.REGISTRATION)) {
            if (message.contains("join")) {
                if (botState.getPlayerUserNames().size() == 10) {
                    botState.sendPublicMessageToPlayer(resistanceMessage.getSender(), "Sorry, the player" +
                            " limit of 10 has already been reached.");
                } else if (!botState.registerPlayer(resistanceMessage.getSender())) {
                    botState.sendPublicMessageToPlayer(resistanceMessage.getSender(), "You have already joined.");
                } else {
                    botState.sendPublicMessage(resistanceMessage.getSender() + " is playing. " +
                            botState.getPlayerUserNames().size() + " players in the game.");
                }
            } else if (message.contains("done")) {
                //check if there's enough players
                if (botState.getPlayerUserNames().size() >= 5) {
                    //start the game, send out all the player roles
                    startGame();
                } else {
                    botState.sendPublicMessage("Sorry, you need at least 5 players. Currently only " +
                            botState.getPlayerUserNames().size() + " people are playing.");
                }

            }
        } else if (botState.getState().equals(BotState.State.PICK_TEAM)) {
            //only listen to the leader right now
            if (resistanceMessage.getSender().equals(botState.getLeaderUserName())) {
                if (resistanceMessage.getMessage().startsWith("pick")) {
                    String chosenUsername = resistanceMessage.getMessage().replace("pick", "").trim();
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
                        botState.sendPublicMessage("Added " + chosenUsername + " to the team.\n");
                    }

                    reportTeamSelection();

                } else if (resistanceMessage.getMessage().startsWith("drop")) {
                    String chosenUsername = resistanceMessage.getMessage().replace("drop", "").trim();
                    if (chosenUsername.isEmpty()) {
                        botState.sendPublicMessage("Dropping all members of the current team.");
                        botState.removeAllTeamMembers();
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
                            botState.sendPublicMessage("Dropped " + chosenUsername + " from the team.");
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
                String vote = resistanceMessage.getMessage().trim();
                //check if they haven't already voted
                if (botState.hasPlayerVoted(senderUserName)) {
                    botState.sendPrivateMessageToPlayer(senderUserName,
                            "You have already voted.");
                } else if (!vote.equalsIgnoreCase("no") && !vote.equalsIgnoreCase("yes")) {
                    botState.sendPrivateMessageToPlayer(senderUserName,
                            "Please vote only 'yes' or 'no'.");
                } else {
                    //register the vote
                    botState.placeVote(senderUserName, vote.equalsIgnoreCase("yes"));
                    botState.sendPrivateMessageToPlayer(senderUserName, "Thank you. Your vote has been accepted.");
                    botState.sendPublicMessage(senderUserName + " has voted");
                    if (botState.allVotesSubmitted()) {
                        //done, lock in the votes
                        voteTeam();
                    }
                }

            }
        } else if (botState.getState().equals(BotState.State.DO_MISSION)) {
            //check if this is a direct message
            if (resistanceMessage.getChannel().isDirect()) {
                String senderUserName = resistanceMessage.getSender();
                String choice = resistanceMessage.getMessage().trim();
                //check if they are on the team
                if (!botState.isPlayerOnTeam(senderUserName)) {
                    botState.sendPrivateMessageToPlayer(senderUserName, "Nice try, but you're not on" +
                            " the current team.");
                } else if (botState.hasTeamMemberChosen(senderUserName)) {
                    //check that they haven't chosen
                    botState.sendPrivateMessageToPlayer(senderUserName,
                            "You have already chosen. Wait for the other team members to choose.");
                } else if (!choice.equalsIgnoreCase("pass") && !choice.equalsIgnoreCase("fail")) {
                    //check that they submitted a valid vote
                    botState.sendPrivateMessageToPlayer(senderUserName,
                            "Please vote only 'pass' or 'fail'.");
                } else {
                    //register the vote, ensure that a resistance player can't vote "fail"
                    boolean pass = choice.equalsIgnoreCase("pass");
                    if (!botState.isSpy(senderUserName) && !pass) {
                        botState.sendPrivateMessageToPlayer(senderUserName, "You can't make the mission fail " +
                                "when you are a resistance member! I've taken the liberty of marking your vote" +
                                " as 'pass'. You're welcome!");
                        pass = true;
                    }
                    botState.placeMissionChoice(senderUserName, pass);
                    botState.sendPrivateMessageToPlayer(senderUserName, "Thank you. Your choice has been accepted.");
                    botState.sendPublicMessage(senderUserName + " has chosen.");
                    if (botState.allMissionChoicesSubmitted()) {
                        //done, lock in the votes
                        try {
                            completeMission();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
    }

    /**
     * completes the current mission. Checks for a victory for the spies or resistance. Changes the leader
     * and moves to the next round.
     */
    private void completeMission() throws IOException {
        if (!botState.completeMission()) {
            //report the status
            botState.remindNoRepeat();
            announceLeader();
        } else {
            //stop running
            //invoke the game over callback
            gameOverCallback.gameIsOver();
        }
    }

    private synchronized BotState getBotState() {
        return botState;
    }

    /**
     * accept the current vote for the team. Change the leader or start the mission.
     */
    private void voteTeam() {
        boolean isTeamAccepted = botState.voteTeam();
        if (isTeamAccepted) {
            //Report the votes
            botState.reportVotes();
            reportTeamSelection();
            botState.sendPublicMessage("The team was accepted!");
            botState.sendPrompt("Team members, send me a direct message indicating whether you want the" +
                    " mission to 'pass' (succeed) or 'fail'.\n" +
                    "Only spies can choose 'fail'. \n" +
                    "If one person chooses 'fail', the mission will fail.\n" +
                    "Your choice will not be revealed.");
            for (String username : botState.getTeam()) {
                botState.sendPrivateMessageToPlayer(username, "Will you allow the mission to succeed or make it fail? Say 'pass' or 'fail'.");
            }
        } else {
            botState.reportVotes();
            reportTeamSelection();
            if (botState.getSuccessiveRejections() == 5) {
                botState.sendPublicMessage("The spies have won due to the team vote failing " +
                        "5 times in a row!");
                botState.announceSpies();
                botState.sendPublicMessage("Thanks for playing!");
                botState.reset();

            } else if (botState.getSuccessiveRejections() == 4) {
                botState.sendPublicMessage("The team was rejected!\n" +
                        "The spies will win if this next vote is rejected!");
            } else {
                botState.sendPublicMessage("The team was rejected!\n" +
                        "The spies will win if there are " + (5 - botState.getSuccessiveRejections()) +
                        " more rejections without a vote passing.");
            }

            announceLeader();
        }

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
                        GameMessageUtil.listOtherPeople(spies, gameCharacter.getUserName()) + ".\n" +
                        "If three missions fail, you win!");
            }
        }

        //announce the leader
        announceLeader();
    }

    private void announceLeader() {
        botState.sendPrompt("Attention " + botState.getLeaderUserName() + "! You are the current leader. \n" +
                "Pick " + botState.getRequiredTeamSize() + " people to be on the team for the next mission.\n" +
                "Use 'pick <username>' to add someone to the team. \nUse 'drop <username>' to remove them, " +
                "or just say 'drop' to remove everyone. \nSay 'done' to lock in your choices and let everyone vote on your choice." +
                "\nRemember, you " +
                "can pick yourself as a team member.");
    }

    /**
     * locks in the current team selection and moves to the next state
     */
    private void pickTeam() {
        botState.pickTeam();
        reportTeamSelection();
        botState.sendPrompt("The team has been set. Everybody, vote on whether you want this team to run the mission.\n" +
                "Send me a direct message of 'no' or 'yes' to vote.\n" +
                "If the majority votes yes, the mission will continue.\n" +
                "If it ties or a majority votes no, the team leader will be advanced to the next player in the rotation.\n" +
                "I will announce all votes once they are all submitted.");
        botState.sendPublicMessage("The leader rotation is " + GameMessageUtil.listOrder(botState.getPlayerCharacters()));
        for (String username : botState.getPlayerUserNames()) {
            botState.sendPrivateMessageToPlayer(username, "Do you accept this team? Say 'yes' or 'no'.");
        }
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

    /**
     * @return true if the game is running (i.e. not in the waiting to start state)
     */
    public boolean isGameRunning() {
        return !botState.getState().equals(BotState.State.WAITING_TO_START);
    }
}
