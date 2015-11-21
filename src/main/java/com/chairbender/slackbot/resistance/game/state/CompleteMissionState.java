package com.chairbender.slackbot.resistance.game.state;

import com.chairbender.slackbot.resistance.game.model.Situation;

/**
 * A state in which either the game is back to picking teams or the game has been completed.
 *
 * Created by chairbender on 11/20/2015.
 */
public class CompleteMissionState {
    private Situation situation;

    /**
     *
     * @param situation current situation, immediately after determining if the mission was a fail or success
     */
    public CompleteMissionState(Situation situation) {
        this.situation = situation;
    }

    /**
     *
     * @return true if the game is now over
     */
    public boolean isGameOver() {
        return situation.getMissionSuccess() == 3 || situation.getMissionFails() == 3;
    }

    /**
     *
     * @return true if the spies have won
     */
    public boolean didSpiesWin() {
        return situation.getMissionFails() == 3;
    }

    /**
     *
     * @return the pick team state after the mission has been completed with no game over. Null if
     *      the game is over.
     */
    public PickTeamState getPickTeamState() {
        if (isGameOver()) {
            return null;
        } else {
            return new PickTeamState(situation);
        }
    }
}
