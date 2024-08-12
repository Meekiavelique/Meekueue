package com.meekdev.meekueue;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import net.kyori.adventure.text.Component;
import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.*;

public class QueueManager {
    private final Meekueue plugin;
    private final Queue<Player> queue = new ConcurrentLinkedQueue<>();
    private final Set<UUID> whitelist = new HashSet<>();
    private final Set<UUID> connecting = new HashSet<>();
    private boolean enabled = true;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService positionUpdater = Executors.newScheduledThreadPool(1);

    private static final long RETRY_DELAY = 4;
    private static final long UPDATE_INTERVAL = 4;

    public QueueManager(Meekueue plugin) {
        this.plugin = plugin;
        startQueueProcessing();
        startPositionUpdates();
    }

    private void startQueueProcessing() {
        scheduler.scheduleAtFixedRate(this::processQueue, 0, 5, TimeUnit.SECONDS);
    }

    private void startPositionUpdates() {
        positionUpdater.scheduleAtFixedRate(this::updateAllPlayerPositions, 0, UPDATE_INTERVAL, TimeUnit.SECONDS);
    }

    public void addToQueue(Player player) {
        UUID playerId = player.getUniqueId();
        if (enabled && !queue.contains(player) && !connecting.contains(playerId) && !isPlayerOnMainServer(player)) {
            queue.offer(player);
            updatePlayerPosition(player);
            plugin.getLogger().info(player.getUsername() + " ajouté à la file d'attente.");
        } else if (isPlayerOnMainServer(player)) {
            removeFromQueue(player);
        }
    }

    private boolean isPlayerOnMainServer(Player player) {
        return player.getCurrentServer()
                .map(server -> server.getServerInfo().getName().equalsIgnoreCase("main"))
                .orElse(false);
    }

    public void removeFromQueue(Player player) {
        boolean removed = queue.remove(player);
        if (removed) {
            plugin.getLogger().info(player.getUsername() + " retiré de la file d'attente.");
            updateAllPlayerPositions();
        }
        connecting.remove(player.getUniqueId());
    }

    private void processQueue() {
        if (!enabled || queue.isEmpty()) {
            return;
        }

        Player player = queue.peek();
        if (player != null) {
            if (isPlayerOnMainServer(player)) {
                removeFromQueue(player);
                processQueue();
            } else if (!connecting.contains(player.getUniqueId()) && player.isActive()) {
                moveToMainServer(player);
            } else if (!player.isActive()) {
                removeFromQueue(player);
                processQueue();
            }
        }
    }

    private void moveToMainServer(Player player) {
        UUID playerId = player.getUniqueId();
        connecting.add(playerId);

        if (whitelist.contains(playerId)) {
            plugin.getLogger().info(player.getUsername() + " est sur la liste blanche et tente de rejoindre le serveur principal.");
            moveToServer(player, "main");
        } else {
            Optional<RegisteredServer> mainServer = plugin.getServer().getServer("main");
            if (mainServer.isPresent()) {
                plugin.getLogger().info(player.getUsername() + " tente de rejoindre le serveur principal.");
                attemptConnection(player, mainServer.get());
            } else {
                plugin.getLogger().error("Le serveur principal n'est pas disponible.");
                connecting.remove(playerId);
                scheduler.schedule(() -> processQueue(), RETRY_DELAY, TimeUnit.SECONDS);
            }
        }
    }

    private void moveToServer(Player player, String serverName) {
        Optional<RegisteredServer> server = plugin.getServer().getServer(serverName);
        if (server.isPresent()) {
            attemptConnection(player, server.get());
        } else {
            plugin.getLogger().error("Le serveur " + serverName + " n'est pas disponible.");
            connecting.remove(player.getUniqueId());
            scheduler.schedule(() -> processQueue(), RETRY_DELAY, TimeUnit.SECONDS);
        }
    }

    private void attemptConnection(Player player, RegisteredServer server) {
        UUID playerId = player.getUniqueId();
        player.createConnectionRequest(server).connectWithIndication().thenAccept(success -> {
            if (success) {
                plugin.getLogger().info(player.getUsername() + " déplacé vers le serveur " + server.getServerInfo().getName() + ".");
                queue.remove(player);
                connecting.remove(playerId);
                updateAllPlayerPositions();
            } else {
                plugin.getLogger().warn(player.getUsername() + " n'a pas pu rejoindre le serveur " + server.getServerInfo().getName() + ". Nouvelle tentative dans " + RETRY_DELAY + " secondes.");
                scheduler.schedule(() -> {
                    if (player.isActive() && queue.contains(player)) {
                        attemptConnection(player, server);
                    } else {
                        connecting.remove(playerId);
                        queue.remove(player);
                        processQueue();
                    }
                }, RETRY_DELAY, TimeUnit.SECONDS);
            }
        });
    }

    private void updatePlayerPosition(Player player) {
        int position = new ArrayList<>(queue).indexOf(player) + 1;
        player.sendMessage(Component.text("§6ᴠᴏᴜѕ ᴇᴛᴇѕ ᴇɴ ᴘᴏѕɪᴛɪᴏɴ §r§f" + position + "§6 ᴅᴀɴѕ ʟᴀ ꜰɪʟᴇ ᴅ'ᴀᴛᴛᴇɴᴛᴇ"));
    }

    private void updateAllPlayerPositions() {
        int position = 1;
        for (Player player : queue) {
            player.sendMessage(Component.text("§6ᴠᴏᴜѕ ᴇᴛᴇѕ ᴇɴ ᴘᴏѕɪᴛɪᴏɴ §r§f" + position + "§6 ᴅᴀɴѕ ʟᴀ ꜰɪʟᴇ ᴅ'ᴀᴛᴛᴇɴᴛᴇ"));
            position++;
        }
        sendQueuePositions();
    }

    private void sendQueuePositions() {
        Map<String, Integer> positions = new HashMap<>();
        int position = 1;
        for (Player player : queue) {
            positions.put(player.getUsername(), position);
            position++;
        }

        String json = new Gson().toJson(positions);

        MinecraftChannelIdentifier channel = MinecraftChannelIdentifier.from("meekueue:queue_positions");

        for (RegisteredServer server : plugin.getServer().getAllServers()) {
            server.sendPluginMessage(channel, json.getBytes());
        }
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

    public boolean isEnabled() {
        return enabled;
    }

    public void shutdown() {
        scheduler.shutdown();
        positionUpdater.shutdown();
    }
}