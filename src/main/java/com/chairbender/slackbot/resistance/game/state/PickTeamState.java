package com.chairbender.slackbot.resistance.game.state;

import com.chairbender.slackbot.resistance.game.model.Situation;

/**
 *
 * Created by chairbender on 11/18/2015.
 */
public class PickTeamState {
    private Situation situation;

    public PickTeamState(Situation situation) {
        this.situation = situation;
    }

    public Situation getSituation() {
        return situation;
    }
}
