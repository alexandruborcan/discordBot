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
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Objects;

import static proiect.DeepseekProvider.messageDeepseek;
import static proiect.YTDLPDownloader.runYtDlp;
import static proiect.YoutubeDataAPI.extractSongLinks;

public class SlashCommandHandler {

    private static AudioPlayerManager audioPlayerManager;
    private AudioPlayer audioPlayer = null;
    private TrackScheduler trackScheduler;

    public SlashCommandHandler() {
        if (audioPlayerManager == null) {
            audioPlayerManager = new DefaultAudioPlayerManager();
            audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
            AudioSourceManagers.registerRemoteSources(audioPlayerManager);
            AudioSourceManagers.registerLocalSource(audioPlayerManager);
            audioPlayer = audioPlayerManager.createPlayer();
            trackScheduler = new TrackScheduler(audioPlayer);
            audioPlayer.addListener(trackScheduler);
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

            // Mark this file to be deleted on exit
            File f = new File(filePath);
            f.deleteOnExit();
        } catch (IOException e) {
            event.reply("Failed to synthesise speech! Did you forget to call PollyHandler.create() before?").queue();
            return;
        }
        audioPlayerManager.loadItem(filePath, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                audioPlayer.playTrack(audioTrack);
                event.reply("Speaking...").queue();
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

    public void handlePlayCommand(SlashCommandInteractionEvent event) {
        event.reply("Got it. Loading...");
        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        // Check if the user is in a voice channel
        if (!memberVoiceState.inAudioChannel()) {
            event.reply("You are not connected to any voice channel.").queue();
            return;
        }

        final AudioManager audioManager = event.getGuild().getAudioManager();
        final VoiceChannel memberVoiceChannel = memberVoiceState.getChannel().asVoiceChannel();

        // Set up the audio sending handler and connect to the voice channel
        audioManager.setSendingHandler(new AudioPlayerSendHandler(audioPlayer));
        audioManager.openAudioConnection(memberVoiceChannel);

        // Extract the content from the command and process it
        String deepseekOutput;
        try {
            deepseekOutput = messageDeepseek(event.getOption("mood-or-feeling-or-situation", OptionMapping::getAsString), true);
        } catch (IOException e) {
            event.reply("An error occurred while processing the content.").queue();
            throw new RuntimeException(e);
        }

        JSONArray links;
        try {
            links = extractSongLinks(deepseekOutput).getJSONArray("links");
        } catch (GeneralSecurityException | IOException e) {
            event.reply("An error occurred while extracting song links.").queue();
            throw new RuntimeException(e);
        }

        ArrayList<String> fileNames = new ArrayList<>();
        for (int i = 0; i < links.length(); i++) {
            String link = links.getString(i);
            try {
                fileNames.add(runYtDlp(link));
            } catch (IOException | InterruptedException e) {
                event.reply("An error occurred while downloading the song: " + link).queue();
                throw new RuntimeException(e);
            }
        }

        // Queue tracks or playlists for playback
        for (String filePath : fileNames) {
            // Mark this file to be deleted on exit
            File f = new File(filePath);
            f.deleteOnExit();

            audioPlayerManager.loadItem(filePath, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack audioTrack) {
                    trackScheduler.queue(audioTrack);
                    event.reply("Added to queue: " + audioTrack.getInfo().title).queue();
                }

                @Override
                public void playlistLoaded(AudioPlaylist audioPlaylist) {
                    for (AudioTrack track : audioPlaylist.getTracks()) {
                        trackScheduler.queue(track);
                    }
                    event.reply("Playlist loaded: " + audioPlaylist.getName() + ". Adding to queue...").queue();
                }

                @Override
                public void noMatches() {
                    event.reply("No matches found for file path: " + filePath).queue();
                }

                @Override
                public void loadFailed(FriendlyException e) {
                    event.reply("Failed to load the track: " + filePath).queue();
                    e.printStackTrace();
                }
            });
    }
}

}