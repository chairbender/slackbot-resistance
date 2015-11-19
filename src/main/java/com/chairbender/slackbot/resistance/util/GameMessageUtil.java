package com.chairbender.slackbot.resistance.util;

import com.chairbender.slackbot.resistance.game.model.PlayerCharacter;

import java.util.HashSet;
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

        int i = 0;
        for (String name : otherNames) {
            if (i == otherNames.size()) {
                result.append(" and " + name);
            }
            result.append(name + ", ");
            i++;
        }
        String resultString = result.toString();
        if (otherNames.size() == 2) {
            resultString.replace(",","");
        }
        if (otherNames.size() == 1) {
            resultString.replace(", ","");
        }
        return resultString;
    }
}
