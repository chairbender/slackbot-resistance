package com.chairbender.slackbot.resistance.util;

import com.chairbender.slackbot.resistance.game.model.Player;
import com.chairbender.slackbot.resistance.game.model.PlayerCharacter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Encapsulates functionality for generating gameplay messages.
 *
 * Created by chairbender on 11/18/2015.
 */
public abstract class GameMessageUtil {

    /**
     *
     * @param people
     * @param exclude
     * @return a string, of comma separated usernames from people, with a final and, excluding "exclude".
     *      Used to print out a list other people's names in a grammatically correct way
     */
    public static String listOtherPeople(Set<PlayerCharacter> people, String exclude) {
        StringBuilder result = new StringBuilder("");

        Set<String> otherNames = new HashSet<>();
        for (PlayerCharacter person : people) {
            if (!person.getUserName().equals(exclude)) {
                otherNames.add(person.getUserName());
            }
        }
        if (otherNames.size() == 1) {
            return otherNames.iterator().next();
        } else if (otherNames.size() == 2) {
            return otherNames.iterator().next() + " and " + otherNames.iterator().next();
        }

        int i = 0;
        for (String name : otherNames) {
            if (i == otherNames.size() - 1) {
                result.append(" and " + name);
            } else {
                result.append(name + ", ");
                i++;
            }
        }

        return  result.toString();
    }

    /**
     *
     * @param players usernames to list with a comma and and
     * @return string listing of the usernames in a grammatically correct format that can be inserted into
     *      a sentence.
     */
    public static String listPeople(Set<Player> players) {
        StringBuilder result = new StringBuilder("");

        if (players.size() == 1) {
            return players.iterator().next().getUserName();
        } else if (players.size() == 2) {
            Iterator<Player> playersIterator = players.iterator();
            return playersIterator.next().getUserName() + " and " + playersIterator.next().getUserName();
        }

        int i = 0;
        for (Player player : players) {
            if (i == players.size() - 1) {
                result.append(" and " + player.getUserName());
            } else {
                result.append(player.getUserName() + ", ");
                i++;
            }
        }

        return result.toString();
    }

    /**
     *
     * @param playerCharacters people to list in the order specified
     * @return a string listing the order of the specified players like
     *      person1 -> person2 -> person3
     */
    public static String listOrder(List<PlayerCharacter> playerCharacters) {
        StringBuilder result = new StringBuilder("");

        if (playerCharacters.size() == 1) {
            return playerCharacters.iterator().next().getUserName();
        }

        int i = 0;
        for (PlayerCharacter playerCharacter : playerCharacters) {
            if (i == playerCharacters.size() - 1) {
                result.append(playerCharacter.getUserName());
            } else {
                result.append(playerCharacter.getUserName() + " -> ");
                i++;
            }
        }

        return result.toString();
    }

    /**
     *
     * @param playerCharacters players to list with a comma and 'and'
     * @return string listing of the usernames in a grammatically correct format that can be inserted into
     *      a sentence.
     */
    public static String listPeoplePlayerCharacters(Set<PlayerCharacter> playerCharacters) {
        StringBuilder result = new StringBuilder("");

        if (playerCharacters.size() == 1) {
            return playerCharacters.iterator().next().getUserName();
        } else if (playerCharacters.size() == 2) {
            Iterator<PlayerCharacter> playersIterator = playerCharacters.iterator();
            return playersIterator.next().getUserName() + " and " + playersIterator.next().getUserName();
        }

        int i = 0;
        for (PlayerCharacter player : playerCharacters) {
            if (i == playerCharacters.size() - 1) {
                result.append(" and " + player.getUserName());
            } else {
                result.append(player.getUserName() + ", ");
                i++;
            }
        }

        return result.toString();
    }

    /**
     *
     * @param atMessage string containing an @ message like &lt;@U1232&gt;
     * @return the same string but with the stuff around the UID in the @ message removed, like just U1232
     */
    public static String atMessageToUID(String atMessage) {
        return atMessage.replace("<@","").replace(">","");

    }
}
