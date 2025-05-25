package proiect;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;
import static proiect.Initialize.init;

public class Main {
    public static void main(String[] args) throws IOException, URISyntaxException {
        init();
        String discordToken = null;
        try {
            discordToken = SecretFileReader.getDiscordKey();
        } catch (IOException | JSONException e) {
            System.err.println("Could not read Discord Key: " + e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.exit(1);
        }
        JDA api = JDABuilder.createDefault(discordToken, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                .addEventListeners(new DiscordListener())
                .build();

        // Register commands to make them visible globally on Discord:
        CommandListUpdateAction commands = api.updateCommands();

        // Add all your commands on this action instance
        commands.addCommands(
                Commands.slash("say", "Makes the bot say what you want")
                        .addOption(STRING, "content", "What the bot should say", true), // Accepting a user input
                Commands.slash("ping", "Makes the bot reply with Pong"),
                Commands.slash("play", "Play a song that fits the given text")
                        .addOption(STRING, "mood-or-feeling-or-situation", "The mood, feeling, or situation.", true),
                Commands.slash("stop", "Stop the music"),
                Commands.slash("skip", "Skip the currently playing song"),
                Commands.slash("pause", "Pause the currently playing song"),
                Commands.slash("unpause", "Resumes the paused song"),
                Commands.slash("resume", "Resumes the paused song"),
                Commands.slash("connect", "Connect to the voice channel"),
                Commands.slash("disconnect", "Disconnect from the voice channel"),
                Commands.slash("speak", "Make the bot speak in the voice channel")
                        .addOption(STRING, "text", "The text to be spoken", true),
                Commands.slash("queue", "Show the current music queue")
        ).queue();
    }

}