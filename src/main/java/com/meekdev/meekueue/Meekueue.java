package com.meekdev.meekueue;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

@Plugin(
        id = "meekueue",
        name = "Meekueue",
        version = "1.0.0",
        description = "A queue management plugin for Velocity",
        authors = {"MeekDev"}
)
public class Meekueue {

    private final ProxyServer server;
    private final Logger logger;
    private QueueManager queueManager;
    public static final String VERSION = "1.0-SNAPSHOT";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_ORANGE = "\033[38;5;208m";

    @Inject
    public Meekueue(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        queueManager = new QueueManager(this);
        String fullVersion = server.getVersion().toString();
        String versionNumber = extractVersionNumber(fullVersion);

        server.getEventManager().register(this, new QueueListener(queueManager, this));
        server.getCommandManager().register("queue", new QueueCommand(queueManager, this));

        logger.info(ANSI_WHITE + "▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁" + ANSI_RESET);
        logger.info(" ");
        logger.info("                 [" + ANSI_ORANGE + "Meekueue " + ANSI_RESET + "Velocity]           ");
        logger.info(String.format(ANSI_RESET + "  The plugin is initiated running on version " + ANSI_ORANGE + Meekueue.VERSION + ANSI_RESET));
        logger.info((ANSI_RESET + "   Version : " + ANSI_ORANGE + versionNumber) + ANSI_RESET);
        logger.info("  Github : https://github.com/Meekiavelique/Meekueue");
        logger.info(" ");
        logger.info("▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁▁");
    }
    private String extractVersionNumber(String fullVersion) {
        String[] parts = fullVersion.split(" ");
        for (String part : parts) {
            if (part.startsWith("3.")) {
                return part;
            }
        }
        return "Unknown";
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }
}