package com.chairbender.slackbot.resistance.game.model;

import com.ullink.slack.simpleslackapi.SlackUser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates an individual player of the game. The person playing, not their role in the game.
 * Does not represent their state in the game. Immutable.
 *
 * Created by chairbender on 11/18/2015.
 */
public class Player {
    private String name;
    private String id;
    //map from user ids to players. testing users are stored with their username as the ID
    private static Map<String,Player> players = new HashMap<>();

    /**
     * @param id user id
     * @param name username
     */
    private Player(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public String getUserName() {
        return name;
    }

    /**
     *
     * @param slackUser slack user to get the corresponding player for
     * @return a player. Any player returned by this will be equivalent for a given slack user.
     */
    public static Player fromSlackUser(SlackUser slackUser) {
        if (!players.containsKey(slackUser.getId())) {
            players.put(slackUser.getId(),new Player(slackUser.getUserName(),slackUser.getId()));
        }
        return players.get(slackUser.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (!name.equals(player.name)) return false;
        return id.equals(player.id);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }


    /**
     *
     * @param username username to create a test player from
     * @return a player with an id and username equal to username
     */
    public static Player fromTestUserName(String username) {
        if (!players.containsKey(username)) {
            players.put(username,new Player(username,username));
        }
        return players.get(username);
    }

    @Override
    public String toString() {
        return getUserName();
    }

    public String getUserID() {
        return id;
    }
}
