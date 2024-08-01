package com.meekdev.meekueue;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class QueueListener {

    private final QueueManager queueManager;
    private final Meekueue plugin;

    public QueueListener(QueueManager queueManager, Meekueue plugin) {
        this.queueManager = queueManager;
        this.plugin = plugin;
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer targetServer = event.getResult().getServer().orElse(null);

        if (targetServer != null && !isMainServer(targetServer)) {
            queueManager.addToQueue(player);
        }
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        // Get the RegisteredServer from the ServerConnection
        RegisteredServer currentServer = player.getCurrentServer().map(connection -> connection.getServer()).orElse(null);

        if (isMainServer(currentServer)) {
            queueManager.removeFromQueue(player);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        queueManager.removeFromQueue(event.getPlayer());
    }

    private boolean isMainServer(RegisteredServer server) {
        return server != null && server.getServerInfo().getName().equalsIgnoreCase("main");
    }
}
