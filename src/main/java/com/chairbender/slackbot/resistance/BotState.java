package com.chairbender.slackbot.resistance;

import com.chairbender.slackbot.resistance.game.model.Player;
import com.chairbender.slackbot.resistance.game.model.PlayerCharacter;
import com.chairbender.slackbot.resistance.game.state.*;
import com.chairbender.slackbot.resistance.util.GameMessageUtil;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.replies.SlackChannelReply;

import java.util.*;

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
    private Set<Player> players;
    //tracks the channel the game was started in
    private SlackChannel gameChannel;
    private Set<Player> team;

    //All the various game states. Looks messy, but it works. Each state is its own, self-contained
    //game situation. They can have arbitrary action methods. We could create a single Object and then cast
    //it to the appropriate state when checking, but it's more readable to just have typed objects even
    //though it clutters up our field definitions
    private PickTeamState pickTeamState;
    //tracks the currently selected memebers of the team
    private Set<Player> teamSelection;

    private VoteTeamState voteTeamState;
    //tracks the current votes of each player
    private Map<Player,Boolean> playerVotes;
    //tracks the number of successive rejections without a team being accepted
    private int successiveRejections = 0;

    private DoMissionState doMissionState;
    //holds the choices of the team members on a given mission (pass or fail)
    private Map<Player,Boolean> teamMemberChoices;

    /**
     *
     * @param teamMember user who is choosing whether to pass / fail
     * @param pass whether they vote to pass or fail the mission
     */
    public synchronized void placeMissionChoice(Player teamMember, boolean pass) {
        teamMemberChoices.put(teamMember,pass);
    }

    /**
     * @param player player to check
     * @return true if the player with that username is a spy
     */
    public synchronized boolean isSpy(Player player) {
        for (PlayerCharacter playerCharacter : getSpies()) {
            if (playerCharacter.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return true if all mission choices have been submitted via placeMissionChoice
     */
    public synchronized boolean allMissionChoicesSubmitted() {
        return teamMemberChoices.size() == teamSelection.size();
    }

    /**
     * Remind the players of the current state and the last message. Only works when not waiting to start
     * and not in the registration state.
     */
    public synchronized void remind() {
        if (state != State.REGISTRATION && state != State.WAITING_TO_START) {
            sendPublicMessage("Round " + pickTeamState.getSituation().getRoundNumber() +
                    " of 5\n" +
                    pickTeamState.getSituation().getMissionSuccess() + " successes\n" +
                    pickTeamState.getSituation().getMissionFails() + " fails\n" +
                    "The leader rotation is " + GameMessageUtil.listOrder(getPlayerCharacters()) + "\n" +
                    pickTeamState.getSituation().getLeader().getUserName() + " is the leader.\n\n" +
                    lastMessage);
        }
    }

    /**
     * removes all members from the current team
     */
    public synchronized void removeAllTeamMembers() {
        teamSelection = new HashSet<>();
    }

    /**
     * remind the status without repeating the last prompt. only if the bot is not in the registration or waiting
     * to start state.
     */
    public synchronized void remindNoRepeat() {
        if (state != State.REGISTRATION && state != State.WAITING_TO_START) {
            sendPublicMessage("Round " + pickTeamState.getSituation().getRoundNumber() +
                    " of 5\n" +
                    pickTeamState.getSituation().getMissionSuccess() + " successes\n" +
                    pickTeamState.getSituation().getMissionFails() + " fails\n" +
                    "The leader rotation is " + GameMessageUtil.listOrder(getPlayerCharacters()) + "\n" +
                    pickTeamState.getSituation().getLeader().getUserName() + " is the leader.");
        }
    }

    /**
     *
     * return the public channel for this game
     */
    public SlackChannel getPublicChannel() {
        return gameChannel;
    }

    public enum State {
        WAITING_TO_START,
        REGISTRATION,
        PICK_TEAM,
        VOTE_TEAM,
        DO_MISSION
    }

    /**
     * broadcast all the votes on the current team to the public channel
     */
    public synchronized void reportVotes() {
        StringBuilder voteReport = new StringBuilder("");
        for (Player player : playerVotes.keySet()) {
            voteReport.append(player.getUserName()).append(": ").append(
                    playerVotes.get(player) ? "yes\n" : "no\n");
        }
        sendPublicMessage("The votes were:\n" + voteReport.toString());
    }

    /**
     * broadcast to the public who the spies were
     */
    public synchronized void announceSpies() {
        sendPublicMessage("The spies were " + GameMessageUtil.listPeoplePlayerCharacters(getSpies()) +
        ".");
    }

    /**
     * reset to a state where the bot is waiting for a game to start
     */
    public synchronized void reset() {
        state = State.WAITING_TO_START;
    }

    /**
     *
     * @param player player to check
     * @return true if that player has already chosen pass or fail
     */
    public synchronized boolean hasTeamMemberChosen(Player player) {
        return teamMemberChoices.containsKey(player);
    }


    public BotState(SlackSession session, String botName, SlackChannel publicChannel, boolean isTestingMode, String testingModeUserName) {
        this.botName = botName;
        this.session = session;
        this.state = State.WAITING_TO_START;
        this.isTestingMode = isTestingMode;
        this.gameChannel = publicChannel;
        this.testingModeUserName = testingModeUserName;
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
     * @param player player to check
     * @return true if the player has already voted for the current vote.
     */
    public synchronized boolean hasPlayerVoted(Player player) {
        return playerVotes.keySet().contains(player);
    }

    /**
     *
     * @return the players of the players in the game. Do not modify the returned set or you'll be sorry.
     */
    public synchronized Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    /**
     * starts the game using the usernames in players.
     */
    public synchronized void startGame() {
        PreGameState preGameState = new PreGameState();
        pickTeamState = preGameState.startGame(players);
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
     * accept the current vote for the team. Change the leader or start the mission.
     * @return true if the team was accepted and the mission should start. false otherwise
     */
    public synchronized boolean voteTeam() {
        //tally the votes
        int yes = 0, no = 0;
        for (boolean vote : playerVotes.values()) {
            if (vote) {
                yes++;
            } else {
                no++;
            }
        }

        if (yes > no) {
            doMissionState = voteTeamState.acceptTeam();
            state = State.DO_MISSION;
            successiveRejections = 0;
            teamMemberChoices = new HashMap<>();
            return true;
        } else {
            pickTeamState = voteTeamState.rejectTeam();
            state = State.PICK_TEAM;
            successiveRejections++;
            teamSelection = new HashSet<>();
            return false;
        }
    }

    /**
     * completes the current mission. Checks for a victory for the spies or resistance. Changes the leader
     * and moves to the next round.
     * @return true if the game has ended
     */
    public synchronized boolean completeMission() {
        //determine mission success / fail
        int numFails = 0;
        for (boolean choice : teamMemberChoices.values()) {
            if (!choice) {
                numFails++;
            }
        }
        if (numFails == 1) {
            sendPublicMessage("The mission was a failure! There was 1 fail.");
        } else if (numFails > 1) {
            sendPublicMessage("The mission was a failure! There were " + numFails + " fails.");
        } else {
            sendPublicMessage("The mission was a success!");
        }

        CompleteMissionState completeMissionState = doMissionState.completeMission(numFails == 0);
        if (completeMissionState.isGameOver()) {
            //report the winner
            if (completeMissionState.didSpiesWin()) {
                sendPublicMessage("3 missions have failed! Spies win!");
                announceSpies();
                sendPublicMessage("Thank you for playing!");
            } else {
                sendPublicMessage("3 missions have succeeded! The Resistance wins!");
                announceSpies();
                sendPublicMessage("Thank you for playing!");
            }
            reset();
            return true;
        } else {
            pickTeamState = completeMissionState.getPickTeamState();
            state = State.PICK_TEAM;
            teamSelection = new HashSet<>();
            return false;
        }
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
     * @return the current players on the team. Don't modify the returned set.
     */
    public synchronized Set<Player> getTeam() {
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
     * @param player player to check
     * @return true if the username is a user in the game. otherwise false.
     */
    public synchronized boolean isPlayer(Player player) {
        return players.contains(player);
    }


    /**
     *
     * @param chosenUserNameOrAtMention a username or a special "at mention string" which looks something like
     *                                 &lt;u12342&gt;
     * @return the player corresponding to that username or at mention. null if not found
     */
    public Player getPlayerFromNameOrAtMention(String chosenUserNameOrAtMention) {
        for (Player player : players) {
            if (player.getUserName().equals(chosenUserNameOrAtMention) ||
                    player.getUserID().equalsIgnoreCase(chosenUserNameOrAtMention.replace("<@","").replace(">",""))) {
                return player;
            }
        }
        return null;
    }

    /**
     *
     * @param player player to check
     * @return true if the player is on the current mission team
     */
    public synchronized boolean isPlayerOnTeam(Player player) {
        return teamSelection.contains(player);
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
     * @param chosenPlayer username to add to the mission team
     */
    public synchronized void addTeamMember(Player chosenPlayer) {
        teamSelection.add(chosenPlayer);
    }

    /**
     *
     * @param chosenPlayer team member username to remove from the mission team
     */
    public synchronized void removeTeamMember(Player chosenPlayer) {
        teamSelection.remove(chosenPlayer);
    }


    /**
     *
     * @param player player to vote
     * @param vote whether to accept the team or reject it
     */
    public synchronized void placeVote(Player player, boolean vote) {
        playerVotes.put(player, vote);
    }

    /**
     *
     * @return true if all votes have been submitted for all players for the current turn
     */
    public synchronized boolean allVotesSubmitted() {
        return playerVotes.size() == players.size();
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
     * @param player user to send the direct message to
     * @param message to send
     */
    public synchronized void sendPrivateMessageToPlayer(Player player, String message) {
        SlackUser user;
        if (isTestingMode) {
            user = session.findUserByUserName(testingModeUserName);
        } else {
            user = session.findUserByUserName(player.getUserName());
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
    public synchronized void sendPublicMessageToPlayer(Player recipient, String message) {
        session.sendMessage(gameChannel, "@" + recipient.getUserName() + " " + message,null);
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
     * @param player slack user to register in the game (if testing mode, this can be a made up username)
     * @return true if the user isn't already registered using this method. false otherwise
     */
    public synchronized boolean registerPlayer(Player player) {
        if (players.contains(player)) {
            return false;
        } else {
            players.add(player);
            return true;
        }
    }

    /**
     * Switches to a state in which it will listen for player registration
     */
    public synchronized void startRegistration() {
        state = State.REGISTRATION;
        players = new HashSet<>();
    }

    /**
     *
     * @return the number of team vote rejections this round
     */
    public synchronized int getSuccessiveRejections() {
        return successiveRejections;
    }
}
