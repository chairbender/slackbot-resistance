package com.chairbender.slackbot.resistance.game.state;

import com.chairbender.slackbot.resistance.game.model.Player;

import java.util.Set;

/**
 * Represents the state of a game before it has started. Only action is to start the game with the specified
 * number of players.
 *
 * Created by chairbender on 11/18/2015.
 */
public class PreGameState {

    /**
     * Assigns all players their roles.
     * @param players set of players who will play the game
     * @return a state in which all players have been assigned their roles, including picking the leader,
     *      and now the leader must decide who will be on the team.
     */
    public PickTeamState startGame(Set<Player> players) {
        //TODO: Implement

    }
}
