package com.meekdev.meekueue;

import com.velocitypowered.api.proxy.Player;

public class PlayerInfo {
    private final Player player;
    private volatile boolean isWhitelisted;
    private volatile boolean hasSpammed;
    private volatile boolean hasUsedCommands;

    public PlayerInfo(Player player) {
        this.player = player;
        this.isWhitelisted = false;
        this.hasSpammed = false;
        this.hasUsedCommands = false;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isWhitelisted() {
        return isWhitelisted;
    }

    public void setWhitelisted(boolean whitelisted) {
        isWhitelisted = whitelisted;
    }

    public boolean hasSpammed() {
        return hasSpammed;
    }

    public void setHasSpammed(boolean hasSpammed) {
        this.hasSpammed = hasSpammed;
    }

    public boolean hasUsedCommands() {
        return hasUsedCommands;
    }

    public void setHasUsedCommands(boolean hasUsedCommands) {
        this.hasUsedCommands = hasUsedCommands;
    }
}