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
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static proiect.DeepseekProvider.messageDeepseek;
import static proiect.YTDLPDownloader.runYtDlp;
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
        audioPlayer.stopTrack();
        trackScheduler.stopTrack();
        trackScheduler.clearQueue();
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

    /**
     * Handles the "play" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    public void handlePlayCommand(SlashCommandInteractionEvent event) {
        InteractionHook interactionHook = event.getHook(); // We save the interaction hook so we can edit the reply later
        event.deferReply().queue(); // This gives the bot a larger window of time to respond (15 minutes instead of 3 seconds)
        final String eventID = event.getId();
        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = member.getVoiceState();

        // Check if the user is in a voice channel
        if (!memberVoiceState.inAudioChannel()) {
            interactionHook.editOriginal("You are not connected to any voice channel.").queue();
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
            interactionHook.editOriginal("Waiting for Deepseek to respond...").queue();
            deepseekOutput = messageDeepseek(event.getOption("mood-or-feeling-or-situation", OptionMapping::getAsString), true);
        } catch (IOException e) {
            interactionHook.editOriginal("An error occurred while processing the content.").queue();
            throw new RuntimeException(e);
        }

        JSONArray songs;
        songs = new JSONObject(deepseekOutput).getJSONArray("songs");

        interactionHook.editOriginal("Downloading songs...").queue();
        Stream<Object> songStream = StreamSupport.stream(songs.spliterator(), true);
        songStream.forEach(song -> {
            try {
                String filePath;
                if(song.toString().contains("watch?v="))  filePath = runYtDlp((String) song, true);
                else filePath = runYtDlp((String) song, false);

                audioPlayerManager.loadItem(filePath, new AudioLoadResultHandler() {
                    static String eventReply = "";
                    static String innerEventID = "";

                    @Override
                    public void trackLoaded(AudioTrack audioTrack) {
                        // If this class is called from a different event, we need to clear the eventReply because it's
                        // static and will accumulate over time
                        if (!eventID.equals(innerEventID)) {
                            innerEventID = eventID;
                            eventReply = "";
                        }
                        audioTrack.setUserData(new File(filePath));
                        trackScheduler.queue(audioTrack);
                        eventReply = eventReply.concat("Added to queue: " + audioTrack.getInfo().title + "\n");
                        interactionHook.editOriginal(eventReply).queue();
                    }

                    @Override
                    public void playlistLoaded(AudioPlaylist audioPlaylist) {
                        for (AudioTrack track : audioPlaylist.getTracks()) {
                            trackScheduler.queue(track);
                        }
                        interactionHook.editOriginal("Playlist loaded: " + audioPlaylist.getName() + ". Adding to queue...").queue();
                    }

                    @Override
                    public void noMatches() {
                        interactionHook.editOriginal("No matches found for file path: " + filePath).queue();
                    }

                    @Override
                    public void loadFailed(FriendlyException e) {
                        interactionHook.editOriginal("Failed to load the track: " + filePath).queue();
                        e.printStackTrace();
                    }
                });
            } catch (IOException | InterruptedException e) {
                interactionHook.editOriginal("An error occurred while downloading the song: " + song).queue();
                throw new RuntimeException(e);
            }

        });
    }

    /**
     * Handles the "skip" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    public void handleSkipCommand(SlashCommandInteractionEvent event) {
        if (audioPlayer == null || trackScheduler == null) {
            event.reply("No track is currently playing.").queue();
            return;
        }

        if (audioPlayer.getPlayingTrack() == null) {
            event.reply("No track is currently playing.").queue();
            return;
        }

        trackScheduler.nextTrack();
        event.reply("Skipped to the next track: " + trackScheduler.getCurrentTrackTitle()).queue();
    }

    /**
     * Handles the "stop" command.
     *
     * @param event the event triggered by a slash command interaction.
     */
    public void handleStopCommand(SlashCommandInteractionEvent event) {
        if (audioPlayer == null || trackScheduler == null) {
            event.reply("No track is currently playing.").queue();
            return;
        }

        audioPlayer.stopTrack();
        trackScheduler.stopTrack();
        trackScheduler.clearQueue();

        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        if (!selfVoiceState.inAudioChannel()) {
            event.reply("Not connected to any voice channel.").queue();
            return;
        }

        final AudioManager audioManager = self.getGuild().getAudioManager();
        event.reply("Stopped the music and cleared the queue.").queue();
    }

    /**
     * Handles the pause and unpause commands.
     *
     * @param event       the event triggered by a slash command interaction.
     * @param shouldPause true if the command is to pause, false to unpause.
     */
    public void handlePauseUnpauseCommand(SlashCommandInteractionEvent event, boolean shouldPause) {
        if (audioPlayer == null || trackScheduler == null) {
            event.reply("No track is currently playing.").queue();
            return;
        }

        if (audioPlayer.getPlayingTrack() == null) {
            event.reply("No track is currently playing.").queue();
            return;
        }

        if (shouldPause) {
            audioPlayer.setPaused(true);
            event.reply("Paused the current track: " + trackScheduler.getCurrentTrackTitle()).queue();
        } else {
            audioPlayer.setPaused(false);
            event.reply("Resumed the current track: " + trackScheduler.getCurrentTrackTitle()).queue();
        }
    }

    public void handleQueueCommand(SlashCommandInteractionEvent event) {
        if (audioPlayer == null || trackScheduler == null) {
            event.reply("No track is currently playing.").queue();
            return;
        }

        if (trackScheduler.getQueue().isEmpty()) {
            event.reply("The queue is empty.").queue();
            return;
        }

        StringBuilder queueMessage = new StringBuilder("**Currently playing song:**\n");
        AudioTrack currentTrack = audioPlayer.getPlayingTrack();
        queueMessage.append(currentTrack.getInfo().title).append("\n");
        queueMessage.append("**Queue:**\n");

        int count = 1;
        for (AudioTrack track : trackScheduler.getQueue()) {
            queueMessage.append(count++).append(". ").append(track.getInfo().title).append("\n");
        }
        event.reply(queueMessage.toString()).queue();
    }
}