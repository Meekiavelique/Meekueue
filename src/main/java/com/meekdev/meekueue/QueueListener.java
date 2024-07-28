package com.meekdev.meekueue;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;

public class QueueListener {

    private final QueueManager queueManager;

    public QueueListener(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        queueManager.addToQueue(event.getPlayer());
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        queueManager.removeFromQueue(event.getPlayer());
    }
}