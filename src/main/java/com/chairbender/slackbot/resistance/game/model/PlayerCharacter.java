package com.chairbender.slackbot.resistance.game.model;

/**
 * Represents a player's character in the game, with an assigned role.
 *
 * Created by chairbender on 11/18/2015.
 */
public class PlayerCharacter {
    private Role role;
    private Player player;

    public boolean isSpy() {
        return !isResistance();
    }

    public Player getPlayer() {
        return player;
    }

    public enum Role {
        RESISTANCE,
        SPY
    }

    public String getUserName() {
        return player.getUserName();
    }


    public PlayerCharacter(Role role, Player player) {
        this.role = role;
        this.player = player;
    }

    public boolean isResistance() {
        return role.equals(Role.RESISTANCE);
    }
}
