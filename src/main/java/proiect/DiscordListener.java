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
                // The play command should send the text to deepseek,
                // Deepseek should return some text (prefferably formatted in some way) containing the song names
                // We send the string of song names to a function like "parseSongs" that returns a vector or list of song names
                // We send that list to a function that downloads those songs using yt-dlp and returns a list of the file names
                // We send that list off to the audioplayer
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
