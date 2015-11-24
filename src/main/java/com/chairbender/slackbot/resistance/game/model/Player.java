package com.chairbender.slackbot.resistance.game.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates an individual player of the game. The person playing, not their role in the game.
 * Does not represent their state in the game. Immutable.
 *
 * Created by chairbender on 11/18/2015.
 */
public class Player {
    private String name;

    public Player(String name) {
        this.name = name;
    }

    /**
     *
     * @param playerUsernames usernames to create players for
     * @return a set of players with the names set to the given usernames
     */
    public static Set<Player> createFromUserNames(Set<String> playerUsernames) {
        Set<Player> result = new HashSet<>();
        for (String playerUsername : playerUsernames) {
            result.add(new Player(playerUsername));
        }
        return result;
    }

    public String getUserName() {
        return name;
    }
}
