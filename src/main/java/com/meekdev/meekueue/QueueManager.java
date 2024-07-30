package com.meekdev.meekueue;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

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

    private static final long RETRY_DELAY = 10; // 10 seconds between each attempt
    private static final long UPDATE_INTERVAL = 1; // 1 second interval for position updates

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
        if (enabled && !queue.contains(player) && !connecting.contains(playerId)) {
            queue.offer(player);
            updatePlayerPosition(player);
            player.sendMessage(Component.text("Vous avez été ajouté à la file d'attente."));
            plugin.getLogger().info(player.getUsername() + " ajouté à la file d'attente.");
        }
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
        if (player != null && !connecting.contains(player.getUniqueId())) {
            moveToMainServer(player);
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
                player.createConnectionRequest(mainServer.get()).connectWithIndication().thenAccept(success -> {
                    if (success) {
                        plugin.getLogger().info(player.getUsername() + " déplacé vers le serveur principal.");
                        queue.remove(player);
                        connecting.remove(playerId);
                        updateAllPlayerPositions();
                    } else {
                        plugin.getLogger().warn(player.getUsername() + " n'a pas pu rejoindre le serveur principal.");
                        scheduler.schedule(() -> {
                            connecting.remove(playerId);
                            processQueue();
                        }, RETRY_DELAY, TimeUnit.SECONDS);
                    }
                });
            } else {
                plugin.getLogger().error("Le serveur principal n'est pas disponible.");
                connecting.remove(playerId);
            }
        }
    }

    private void moveToServer(Player player, String serverName) {
        Optional<RegisteredServer> server = plugin.getServer().getServer(serverName);
        if (server.isPresent()) {
            player.createConnectionRequest(server.get()).connectWithIndication().thenAccept(success -> {
                if (success) {
                    plugin.getLogger().info(player.getUsername() + " déplacé vers le serveur " + serverName + ".");
                    queue.remove(player);
                    connecting.remove(player.getUniqueId());
                    updateAllPlayerPositions();
                } else {
                    plugin.getLogger().warn(player.getUsername() + " n'a pas pu rejoindre le serveur " + serverName + ".");
                    scheduler.schedule(() -> {
                        connecting.remove(player.getUniqueId());
                        processQueue();
                    }, RETRY_DELAY, TimeUnit.SECONDS);
                }
            });
        } else {
            plugin.getLogger().error("Le serveur " + serverName + " n'est pas disponible.");
            connecting.remove(player.getUniqueId());
        }
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
    }

    private void processNextPlayer() {
        if (!enabled || queue.isEmpty()) {
            return;
        }

        Player player = queue.peek(); // Use peek() instead of poll()
        if (player != null) {
            plugin.getLogger().info(player.getUsername() + " tente de rejoindre le serveur principal.");
            moveToMainServer(player);
        }
    }

    public void toggleQueue(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            processNextPlayer();
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
