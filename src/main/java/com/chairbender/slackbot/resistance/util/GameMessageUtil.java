package com.chairbender.slackbot.resistance.util;

import com.chairbender.slackbot.resistance.game.model.PlayerCharacter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        Set<String> otherNames = people.stream().filter(character -> !character.getUserName().equals(exclude)).map(PlayerCharacter::getUserName).collect(Collectors.toSet());
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
     * @param playerUsernames usernames to list with a comma and and
     * @return string listing of the usernames in a grammatically correct format that can be inserted into
     *      a sentence.
     */
    public static String listPeople(Set<String> playerUsernames) {
        StringBuilder result = new StringBuilder("");

        if (playerUsernames.size() == 1) {
            return playerUsernames.iterator().next();
        } else if (playerUsernames.size() == 2) {
            return playerUsernames.iterator().next() + " and " + playerUsernames.iterator().next();
        }

        int i = 0;
        for (String name : playerUsernames) {
            if (i == playerUsernames.size() - 1) {
                result.append(" and " + name);
            } else {
                result.append(name + ", ");
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
}
