package com.chairbender.slackbot.resistance.game.model;

import java.util.HashSet;
import java.util.Set;

/**
 * encapsulates the current situation of the game. i.e. players, roles, turns, etc...
 *
 * Created by chairbender on 11/18/2015.
 */
public class Situation {

    private Set<PlayerCharacter> playerCharacters;
    private PlayerCharacter leader;
    //starts from 1 because that's how the game does it. Sorry :-(.
    private int currentRound;
    
    public Situation(Set<PlayerCharacter> playerCharacters, PlayerCharacter leader, int currentRound) {
        this.playerCharacters = playerCharacters;
        this.leader = leader;
        this.currentRound = currentRound;
    }

    public Set<PlayerCharacter> getPlayerCharacters() {
        return playerCharacters;
    }

    /**
     *
     * @return the set of player characters that are spies
     */
    public Set<PlayerCharacter> getSpies() {
        Set<PlayerCharacter> spies = new HashSet<>();
        for (PlayerCharacter playerCharacter : playerCharacters) {
            if (playerCharacter.isSpy()) {
                spies.add(playerCharacter);
            }
        }

        return spies;
    }

    /**
     *
     * @return the player who is the current team leader.
     */
    public PlayerCharacter getLeader() {
        return leader;
    }

    /**
     *
     * @return the number of players that need to go on the current team based on the current round number
     *      and number of players. -1 if there's too many or too few players
     */
    public int getRequiredTeamSize() {
        return RulesUtil.getRequiredTeamSize(playerCharacters.size(),currentRound);
    }
}
