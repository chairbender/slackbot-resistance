package com.chairbender.slackbot.resistance.game.state;

import com.chairbender.slackbot.resistance.game.model.Situation;

/**
 * A state in which the mission has started and the team must privately
 * select their pass / fail cards.
 *
 * Created by chairbender on 11/20/2015.
 */
public class DoMissionState {
    private Situation situation;


    public DoMissionState(Situation situation) {
        this.situation = situation;
    }

    /**
     *
     * @param success whether mission succeeds or fails
     * @return state in which the success or failure has occurred and been tracked
     * and the leader has been advanced. Or, a victory has occurred.
     *
     */
    public CompleteMissionState completeMission(boolean success) {
        //track the success or failure
        situation.completeMission(success);
        //advance the leader
        situation.advanceLeader();

        return new CompleteMissionState(situation);
    }
}
