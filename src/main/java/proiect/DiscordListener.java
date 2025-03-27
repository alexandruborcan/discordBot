package proiect;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.Objects;

public class DiscordListener extends ListenerAdapter {
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
                handleSayCommand(event);
                break;
            case "ping":
                handlePingCommand(event);
                break;
            case "stop":
            case "skip":
            case "pause":
            case "play":
            case "unpause":
            case "resume":
                handleNotImplementedCommand(event);
                break;
            default:
                event.reply("Unknown command.").queue();
                break;
        }
    }

    /**
     * Handles the "say" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    private void handleSayCommand(SlashCommandInteractionEvent event) {
        String content = event.getOption("content", OptionMapping::getAsString);
        event.reply(Objects.requireNonNullElse(content, "Content cannot be null.")).queue();
    }

    /**
     * Handles the "ping" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    private void handlePingCommand(SlashCommandInteractionEvent event) {
        event.reply("Pong!").queue();
    }

    /**
     * Handles commands that are not implemented.
     *
     * @param event the event triggered by a slash command interaction.
     */
    private void handleNotImplementedCommand(SlashCommandInteractionEvent event) {
        event.reply("Not implemented.").queue();
    }
}
