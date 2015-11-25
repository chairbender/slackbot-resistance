package com.chairbender.slackbot.resistance.game.state;

import com.chairbender.slackbot.resistance.game.model.Player;
import com.chairbender.slackbot.resistance.game.model.PlayerCharacter;
import com.chairbender.slackbot.resistance.game.model.Situation;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * Created by chairbender on 11/18/2015.
 */
public class PickTeamState {
    private Situation situation;

    public PickTeamState(Situation situation) {
        this.situation = situation;
    }

    public Situation getSituation() {
        return situation;
    }

    /**
     *
     * @param team list of usernames of players on the team
     * @return a state where the players need to vote on the team selection
     */
    public VoteTeamState pickTeam(Set<Player> team) {
        //find the player characters for each username and assign them to the team, then create
        //the next state
        Set<PlayerCharacter> teamPlayers = new HashSet<>();
        for (Player player : team) {
            teamPlayers.add(situation.getPlayerCharacter(player));
        }
        situation.setCurrentTeam(teamPlayers);

        return new VoteTeamState(situation);
    }
}
