package proiect;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.util.Objects;

public class SlashCommandHandler {

    private static AudioPlayerManager audioPlayerManager;
    private AudioPlayer audioPlayer = null;

    public SlashCommandHandler() {
        if (audioPlayerManager == null) {
            audioPlayerManager = new DefaultAudioPlayerManager();
            audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
            AudioSourceManagers.registerRemoteSources(audioPlayerManager);
            AudioSourceManagers.registerLocalSource(audioPlayerManager);
            audioPlayer = audioPlayerManager.createPlayer();
        }
    }

    /**
     * Handles the "disconnect" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    @SuppressWarnings("DataFlowIssue")
    public void handleDisconnectCommand(SlashCommandInteractionEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        if (!selfVoiceState.inAudioChannel()) {
            event.reply("Not connected to any voice channel.").queue();
            return;
        }

        final AudioManager audioManager = self.getGuild().getAudioManager();
        audioManager.closeAudioConnection();
        event.reply("Disconnected from voice channel.").queue();
    }

    /**
     * Handles the "connect" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    @SuppressWarnings("DataFlowIssue")
    public void handleConnectCommand(SlashCommandInteractionEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        if (selfVoiceState.inAudioChannel()) {
            event.reply("Already connected to a voice channel: " + selfVoiceState.getChannel().getName()).queue();
            return;
        }

        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (!memberVoiceState.inAudioChannel()) {
            event.reply("You are not connected to any voice channel.").queue();
            return;
        }

        final AudioManager audioManager = event.getGuild().getAudioManager();
        final VoiceChannel memberVoiceChannel = memberVoiceState.getChannel().asVoiceChannel();

        audioManager.openAudioConnection(memberVoiceChannel);
        event.reply("Connected to voice channel: " + memberVoiceChannel.getName()).queue();
    }

    /**
     * Handles the "say" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    public void handleSayCommand(SlashCommandInteractionEvent event) {
        String content = event.getOption("content", OptionMapping::getAsString);
        event.reply(Objects.requireNonNullElse(content, "Content cannot be null.")).queue();
    }

    /**
     * Handles the "ping" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    public void handlePingCommand(SlashCommandInteractionEvent event) {
        event.reply("Pong!").queue();
    }

    /**
     * Handles commands that are not implemented.
     *
     * @param event the event triggered by a slash command interaction.
     */
    public void handleNotImplementedCommand(SlashCommandInteractionEvent event) {
        event.reply("Not implemented.").queue();
    }

    /**
     * Handles the "speak" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    public void handleSpeakCommand(SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();
        if (!memberVoiceState.inAudioChannel()) {
            event.reply("You are not connected to any voice channel.").queue();
            return;
        }

        final AudioManager audioManager = event.getGuild().getAudioManager();
        final VoiceChannel memberVoiceChannel = memberVoiceState.getChannel().asVoiceChannel();

        audioManager.setSendingHandler(new AudioPlayerSendHandler(audioPlayer));
        audioManager.openAudioConnection(memberVoiceChannel);

        String filePath;
        try {
            PollyHandler.create(); // Just in case, it doesn't break anything
            filePath = PollyHandler.synthesizeSpeech(event.getOption("text", OptionMapping::getAsString));
        } catch (IOException e) {
            event.reply("Failed to synthesise speech! Did you forget to call PollyHandler.create() before?").queue();
            return;
        }
        audioPlayerManager.loadItem(filePath, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                audioPlayer.playTrack(audioTrack);
                event.reply("Playing track: " + audioTrack.getInfo().title).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
            }

            @Override
            public void noMatches() {
                event.reply("No matches found for file path: " + filePath).queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                e.printStackTrace();
            }
        });
    }
}