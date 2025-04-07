package proiect;

import com.openai.client.OpenAIClientImpl;
import com.openai.models.beta.threads.messages.MessageCreateParams;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import java.io.IOException;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.json.JSONException;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;
import static proiect.DeepseekProvider.*;


import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;


public class Main {
    public static void main(String[] args) throws IOException {
        String discordToken = null;
        try {
            discordToken = SecretFileReader.getDiscordKey();
        } catch (IOException | JSONException e) {
            System.err.println("Could not read Discord Key: " + e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.exit(1);
        }
        JDA api = JDABuilder.createLight(discordToken, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .addEventListeners(new DiscordListener())
                .build();

        // Register commands to make them visible globally on Discord:
        CommandListUpdateAction commands = api.updateCommands();

        // Add all your commands on this action instance
        commands.addCommands(
                Commands.slash("say", "Makes the bot say what you want")
                        .addOption(STRING, "content", "What the bot should say", true), // Accepting a user input
                Commands.slash("ping", "Makes the bot reply with Pong")
        ).queue();

        String reply = messageDeepseek("I need 5 sad songs");
        System.out.println(reply);

    }

}