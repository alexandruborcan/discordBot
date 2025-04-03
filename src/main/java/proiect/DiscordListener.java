package proiect;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {

    private final SlashCommandHandler commandHandler = new SlashCommandHandler();

    /**
     * Handles the slash command interactions.
     *
     * @param event the event triggered by a slash command interaction.
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getInteraction().getName();
        switch (commandName) {
            case "say":
                commandHandler.handleSayCommand(event);
                break;
            case "ping":
                commandHandler.handlePingCommand(event);
                break;
            case "stop":
            case "skip":
            case "pause":
            case "play":
            case "unpause":
            case "resume":
                commandHandler.handleNotImplementedCommand(event);
                break;
            case "connect":
                commandHandler.handleConnectCommand(event);
                break;
            case "disconnect":
                commandHandler.handleDisconnectCommand(event);
                break;
            default:
                event.reply("Unknown command.").queue();
                break;
        }
    }
}