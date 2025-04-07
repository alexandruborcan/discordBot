package proiect;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;

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
            case "speak":
                commandHandler.handleSpeakCommand(event);
                break;
            default:
                event.reply("Unknown command.").queue();
                break;
        }
    }

    /**
     * Handles the guild voice update events.
     * This method gets called whenever there is an event in a voice channel.
     *
     * @param event the event triggered by a guild voice update.
     */
    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        // If a user or a bot leaves any channel
        if (event.getChannelLeft() != null) {
            // If the bot is in the channel that the event occured, and the bot is alone, then disconnect.
            if (event.getChannelLeft().getMembers().size() == 1) {
                Member self = event.getGuild().getSelfMember();
                AudioManager audioManager = self.getGuild().getAudioManager();
                audioManager.closeAudioConnection();
            }
        }
    }
}
