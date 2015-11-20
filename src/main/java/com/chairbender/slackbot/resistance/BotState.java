package com.chairbender.slackbot.resistance;

import com.chairbender.slackbot.resistance.game.model.Player;
import com.chairbender.slackbot.resistance.game.model.PlayerCharacter;
import com.chairbender.slackbot.resistance.game.state.PickTeamState;
import com.chairbender.slackbot.resistance.game.state.PreGameState;
import com.chairbender.slackbot.resistance.game.state.VoteTeamState;
import com.chairbender.slackbot.resistance.util.GameMessageUtil;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;

import java.io.IOException;
import java.util.*;
import java.util.stream.BaseStream;

/**
 * Tracks the state of the bot in a thread safe way.
 *
 * Created by chairbender on 11/19/2015.
 */
public class BotState {
    private boolean isTestingMode;
    //tracks the username of the player who is using testing mode
    private String testingModeUserName;
    private String botName;
    private SlackSession session;
    private State state;
    //tracks the last message
    private String lastMessage;
    //tracks the players in the game
    private Set<String> playerUsernames;
    //tracks the channel the game was started in
    private SlackChannel gameChannel;
    private Set<String> team;

    //All the various game states. Looks messy, but it works. Each state is its own, self-contained
    //game situation. They can have arbitrary action methods. We could create a single Object and then cast
    //it to the appropriate state when checking, but it's more readable to just have typed objects even
    //though it clutters up our field definitions
    private PickTeamState pickTeamState;
    //tracks the currently selected memebers of the team
    private Set<String> teamSelection;

    private VoteTeamState voteTeamState;
    //tracks the current votes of each player
    private Map<String,Boolean> playerVotes;

    public enum State {
        WAITING_TO_START,
        REGISTRATION,
        PICK_TEAM,
        VOTE_TEAM
    }

    public BotState(SlackSession session, String botName, boolean isTestingMode) {
        this.botName = botName;
        this.session = session;
        this.state = State.WAITING_TO_START;
        this.isTestingMode = isTestingMode;
    }

    public synchronized boolean isTestingMode() {
        return isTestingMode;
    }

    public synchronized String getBotName() {
        return botName;
    }

    public synchronized State getState() {
        return state;
    }

    public synchronized void setTestingUserName(String testingUserName) {
        this.testingModeUserName = testingUserName;
    }

    public synchronized int getTeamSize() {
        return teamSelection.size();
    }

    public synchronized int getRequiredTeamSize() {
        return pickTeamState.getSituation().getRequiredTeamSize();
    }

    /**
     *
     * @param playerUserName username to check
     * @return true if the player has already voted for the current vote.
     */
    public synchronized boolean hasPlayerVoted(String playerUserName) {
        return playerVotes.keySet().contains(playerUserName);
    }

    /**
     * Connects to slack and starts using the configured listeners
     * @throws IOException if error occurs connecting with slack
     */
    public void connectToSlack() throws IOException {
        session.connect();
    }

    /**
     *
     * @return the usernames of the players in the game. Do not modify the returned set or you'll be sorry.
     */
    public synchronized Set<String> getPlayerUserNames() {
        return Collections.unmodifiableSet(playerUsernames);
    }

    /**
     * starts the game using the usernames in playerUsernames.
     */
    public synchronized void startGame() {
        PreGameState preGameState = new PreGameState();
        pickTeamState = preGameState.startGame(Player.createFromUserNames(playerUsernames));
        state = State.PICK_TEAM;
        teamSelection = new HashSet<>();
    }

    /**
     * locks in the current team selection and moves to the next state
     */
    public synchronized void pickTeam() {
        voteTeamState = pickTeamState.pickTeam(teamSelection);
        state = State.VOTE_TEAM;
        playerVotes = new HashMap<>();
    }

    /**
     *
     * @return the list of player characters who are spies.
     */
    public synchronized Set<PlayerCharacter> getSpies() {
        return pickTeamState.getSituation().getSpies();
    }

    /**
     *
     * @return a list of all player characters in the game
     */
    public synchronized List<PlayerCharacter> getPlayerCharacters() {
        return pickTeamState.getSituation().getPlayerCharacters();
    }

    /**
     *
     * @return the current usernames of players on the team. Don't modify the returned set.
     */
    public synchronized Set<String> getTeam() {
        return Collections.unmodifiableSet(teamSelection);
    }

    public synchronized void setMessagePostedListener(SlackMessagePostedListener listener) {
        session.addMessagePostedListener(listener);
    }

    /**
     *
     * @return the username of the leader. null if none yet.
     */
    public synchronized String getLeaderUserName() {
        if (pickTeamState != null) {
            return pickTeamState.getSituation().getLeader().getUserName();
        } else {
            return null;
        }
    }

    /**
     *
     * @param username username to check
     * @return true if the username is a user in the game. otherwise false.
     */
    public synchronized boolean isPlayer(String username) {
        return playerUsernames.contains(username);
    }


    /**
     *
     * @param username username to check
     * @return true if the player is on the current mission team
     */
    public synchronized boolean isPlayerOnTeam(String username) {
        return teamSelection.contains(username);
    }

    /**
     *
     * @return true if the mission team is full
     */
    public synchronized boolean isTeamFull() {
        return teamSelection.size() == pickTeamState.getSituation().getRequiredTeamSize();
    }

    /**
     *
     * @param chosenUsername username to add to the mission team
     */
    public synchronized void addTeamMember(String chosenUsername) {
        teamSelection.add(chosenUsername);
    }

    /**
     *
     * @param chosenUsername team member username to remove from the mission team
     */
    public synchronized void removeTeamMember(String chosenUsername) {
        teamSelection.remove(chosenUsername);
    }


    /**
     *
     * @param userName username to vote
     * @param vote whether to accept the team or reject it
     */
    public synchronized void placeVote(String userName, boolean vote) {
        playerVotes.put(userName, vote);
    }

    /**
     *
     * @return true if all votes have been submitted for all players for the current turn
     */
    public synchronized boolean allVotesSubmitted() {
        return playerVotes.size() == playerUsernames.size();
    }

    /**
     *
     * @param message message to send to all players in the public channel.
     */
    public synchronized void sendPublicMessage(String message) {
        session.sendMessage(gameChannel, message, null);
    }


    /**
     * Sends a direct message to the user.
     *
     * @param userName user to send the direct message to
     * @param message to send
     */
    public synchronized void sendPrivateMessageToPlayer(String userName, String message) {
        SlackUser user;
        if (isTestingMode) {
            user = session.findUserByUserName(testingModeUserName);
        } else {
            user = session.findUserByUserName(userName);
        }
        SlackMessageHandle<SlackChannelReply> openDirectHandle = session.openDirectMessageChannel(user);
        SlackChannel directChannel = openDirectHandle.getReply().getSlackChannel();
        session.sendMessage(directChannel, message, null);
    }

    /**
     * Requires that the gameChannel has already been set.
     * Sends a message in the game channel, @replying to the specified sender.
     *
     * @param recipient the person to @ reply
     * @param message the message to send them
     */
    public synchronized void sendPublicMessageToPlayer(String recipient, String message) {
        session.sendMessage(gameChannel, "@" + recipient + " " + message,null);
    }


    /**
     * Sends the message to all players. Intended to be use for prompts that need to be repeated
     * if people aren't sure what to do. Tracks 'message' as the last message sent. Sends the
     * message out via the gameChannel (the channel that the game was started in)
     * Requires that the gameChannel has already been set.
     * @param message message to send.
     */
    public synchronized void sendPrompt(String message) {
        session.sendMessage(gameChannel,message,null);
        lastMessage = message;
    }

    /**
     *
     * @param username slack username to register in the game (if testing mode, this can be a made up username)
     * @return true if the user isn't already registered using this method. false otherwise
     */
    public synchronized boolean registerPlayer(String username) {
        if (playerUsernames.contains(username)) {
            return false;
        } else {
            playerUsernames.add(username);
            return true;
        }
    }

    /**
     * Switches to a state in which it will listen for player registration
     * @param gameChannel channel the game was started in which should be used for all public
     *                    communication.
     */
    public synchronized void startRegistration(SlackChannel gameChannel) {
        state = State.REGISTRATION;
        playerUsernames = new HashSet<>();
        this.gameChannel = gameChannel;
    }




}
