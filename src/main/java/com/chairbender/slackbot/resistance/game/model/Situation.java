package com.chairbender.slackbot.resistance.game.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * encapsulates the current situation of the game. i.e. players, roles, turns, etc...
 *
 * Created by chairbender on 11/18/2015.
 */
public class Situation {

    private List<PlayerCharacter> playerCharacters;
    private PlayerCharacter leader;
    private Set<PlayerCharacter> teamMembers;
    //starts from 1 because that's how the game does it. Sorry :-(.
    private int currentRound;

    /**
     *
     * @param playerCharacters players, their state, and the order they are sitting around the table.
     * @param leader the current leader
     * @param teamMembers the players on the currently selected team
     * @param currentRound the current round.
     */

    public Situation(List<PlayerCharacter> playerCharacters, PlayerCharacter leader, Set<PlayerCharacter> teamMembers, int currentRound) {
        this.playerCharacters = playerCharacters;
        this.leader = leader;
        this.teamMembers = teamMembers;
        this.currentRound = currentRound;
    }



    public List<PlayerCharacter> getPlayerCharacters() {
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

    /**
     *
     * @param username username to find
     * @return the player character with the given username. null if not found
     */
    public PlayerCharacter getPlayerByUserName(String username) {
        for (PlayerCharacter playerCharacter : playerCharacters) {
            if (playerCharacter.getUserName().equals(username)) {
                return playerCharacter;
            }
        }

        return null;
    }

    /**
     *
     * @param currentTeam the players to put on the mission team
     */
    public void setCurrentTeam(Set<PlayerCharacter> currentTeam) {
        this.teamMembers = currentTeam;
    }
}
