package com.chairbender.slackbot.resistance.game.state;

import com.chairbender.slackbot.resistance.game.model.Player;
import com.chairbender.slackbot.resistance.game.model.PlayerCharacter;
import com.chairbender.slackbot.resistance.game.model.RulesUtil;
import com.chairbender.slackbot.resistance.game.model.Situation;

import java.util.*;

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
        //Determine the number of spies and resistance memebers
        int numSpies = RulesUtil.getSpiesNeeded(players.size());
        List<Player> playerList = new ArrayList<>(players.size());
        playerList.addAll(players);
        Collections.shuffle(playerList);

        //create player characters for all players and set the spies
        List<PlayerCharacter> playerCharacters = new ArrayList<>();
        int chosenSpies = 0;
        for (Player player : playerList) {
            playerCharacters.add(
                    new PlayerCharacter(
                            chosenSpies < numSpies ? PlayerCharacter.Role.SPY : PlayerCharacter.Role.RESISTANCE,
                            player));
            chosenSpies++;
        }

        Collections.shuffle(playerCharacters);
        PlayerCharacter leader = playerCharacters.get(0);

        Situation gameStartedSituation = new Situation(playerCharacters,leader,null,1);

        return new PickTeamState(gameStartedSituation);
    }
}
