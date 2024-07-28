package com.meekdev.meekueue;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueueManager {

    private final Meekueue plugin;
    private final Queue<Player> queue = new ConcurrentLinkedQueue<>();
    private final Set<UUID> whitelist = new HashSet<>();
    private boolean enabled = true;

    public QueueManager(Meekueue plugin) {
        this.plugin = plugin;
    }

    public void addToQueue(Player player) {
        if (enabled && !whitelist.contains(player.getUniqueId())) {
            queue.offer(player);
            updatePlayerPosition(player);
            player.sendMessage(Component.text("You have been added to the queue."));
            plugin.getLogger().info(player.getUsername() + " added to queue.");
        } else {
            moveToMainServer(player);
        }
    }

    public void removeFromQueue(Player player) {
        queue.remove(player);
        plugin.getLogger().info(player.getUsername() + " removed from queue.");
    }

    public void processQueue() {
        Player player;
        while ((player = queue.poll()) != null) {
            plugin.getLogger().info(player.getUsername() + " is being moved to the main server.");
            moveToMainServer(player);
        }
    }

    private void moveToMainServer(Player player) {
        Optional<RegisteredServer> mainServer = plugin.getServer().getServer("main");
        mainServer.ifPresent(server -> {
            player.createConnectionRequest(server).fireAndForget();
            plugin.getLogger().info(player.getUsername() + " moved to main server.");
        });
    }

    private void updatePlayerPosition(Player player) {
        int position = new ArrayList<>(queue).indexOf(player) + 1;
        player.sendMessage(Component.text("You are in position " + position + " in the queue."));
    }

    public void toggleQueue(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            processQueue();
        }
    }

    public void addToWhitelist(UUID uuid) {
        whitelist.add(uuid);
    }

    public void removeFromWhitelist(UUID uuid) {
        whitelist.remove(uuid);
    }

    public boolean isWhitelisted(UUID uuid) {
        return whitelist.contains(uuid);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
