package com.chairbender.slackbot.resistance.game.model;

/**
 * Encapsulates rules logic like how many players are needed on missions, how many spies, etc...
 *
 * Created by chairbender on 11/19/2015.
 */
public abstract class RulesUtil {

    /**
     *
     * @param numPlayers number of players in the game. Must be from 5 to 10
     * @param round current round (1 - 5)
     * @return the number of players needed for the mission team for the indicated round.
     */
    public static int getRequiredTeamSize(int numPlayers, int round) {
        if (round == 1) {
            switch (numPlayers) {
                case 5:
                    return 2;
                case 6:
                    return 2;
                case 7:
                    return 2;

                case 8:
                    return 3;

                case 9:
                    return 3;

                case 10:
                    return 3;

                default:
                    return -1;

            }
        } else if (round == 2) {
            switch (numPlayers) {
                case 5:
                    return 3;

                case 6:
                    return 3;

                case 7:
                    return 3;

                case 8:
                    return 4;

                case 9:
                    return 4;

                case 10:
                    return 4;

                default:
                    return -1;

            }
        } else if (round == 3) {
            switch (numPlayers) {
                case 5:
                    return 2;

                case 6:
                    return 4;

                case 7:
                    return 3;

                case 8:
                    return 4;

                case 9:
                    return 4;

                case 10:
                    return 4;

                default:
                    return -1;

            }
        }  else if (round == 4) {
            switch (numPlayers) {
                case 5:
                    return 3;

                case 6:
                    return 3;

                case 7:
                    return 4;

                case 8:
                    return 5;

                case 9:
                    return 5;

                case 10:
                    return 5;

                default:
                    return -1;

            }
        } else if (round == 5) {
            switch (numPlayers) {
                case 5:
                    return 3;

                case 6:
                    return 4;

                case 7:
                    return 4;

                case 8:
                    return 5;

                case 9:
                    return 5;

                case 10:
                    return 5;

                default:
                    return -1;

            }
        }

        return -1;
    }

    /**
     *
     * @param playerCount number of players in the game (5 - 10)
     * @return the number of spies needed in the game.
     */
    public static int getSpiesNeeded(int playerCount) {
        if (playerCount == 5 || playerCount == 6) {
            return 2;
        } else if (playerCount == 10) {
            return 4;
        } else {
            return 3;
        }
    }
}
