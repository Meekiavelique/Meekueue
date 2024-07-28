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

    @Inject
    public Meekueue(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        queueManager = new QueueManager(this);
        server.getEventManager().register(this, new QueueListener(queueManager));
        server.getCommandManager().register("queue", new QueueCommand(queueManager, this));
        logger.info("Meekueue has been initialized.");
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }
}