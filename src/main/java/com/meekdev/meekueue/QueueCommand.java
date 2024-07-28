package com.meekdev.meekueue;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class QueueCommand implements SimpleCommand {

    private final QueueManager queueManager;
    private final Meekueue plugin;

    public QueueCommand(QueueManager queueManager, Meekueue plugin) {
        this.queueManager = queueManager;
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        // Check if the player has the necessary permission
        if (!invocation.source().hasPermission("meekueue.admin")) {
            invocation.source().sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length < 1) {
            invocation.source().sendMessage(Component.text("Usage: /queue <enable|disable|whitelist> [player]", NamedTextColor.YELLOW));
            return;
        }

        switch (args[0].toLowerCase()) {
            case "enable":
                queueManager.toggleQueue(true);
                invocation.source().sendMessage(Component.text("Queue enabled.", NamedTextColor.GREEN));
                break;
            case "disable":
                queueManager.toggleQueue(false);
                invocation.source().sendMessage(Component.text("Queue disabled.", NamedTextColor.GREEN));
                break;
            case "whitelist":
                if (args.length < 3) {
                    invocation.source().sendMessage(Component.text("Usage: /queue whitelist <add|remove> <player>", NamedTextColor.YELLOW));
                    return;
                }
                handleWhitelist(invocation, args[1], args[2]);
                break;
            default:
                invocation.source().sendMessage(Component.text("Unknown subcommand. Use enable, disable, or whitelist.", NamedTextColor.RED));
                break;
        }
    }

    private void handleWhitelist(Invocation invocation, String action, String playerName) {
        Optional<Player> optionalPlayer = plugin.getServer().getPlayer(playerName);

        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            if ("add".equalsIgnoreCase(action)) {
                queueManager.addToWhitelist(player.getUniqueId());
                invocation.source().sendMessage(Component.text("Added " + playerName + " to the whitelist.", NamedTextColor.GREEN));
            } else if ("remove".equalsIgnoreCase(action)) {
                queueManager.removeFromWhitelist(player.getUniqueId());
                invocation.source().sendMessage(Component.text("Removed " + playerName + " from the whitelist.", NamedTextColor.GREEN));
            } else {
                invocation.source().sendMessage(Component.text("Invalid whitelist action. Use 'add' or 'remove'.", NamedTextColor.RED));
            }
        } else {
            invocation.source().sendMessage(Component.text("Player not found.", NamedTextColor.RED));
        }
    }
}
