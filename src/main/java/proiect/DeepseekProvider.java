package proiect;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.io.IOException;
import java.util.List;

import static proiect.SecretFileReader.getDeepSeekKey;


public class DeepseekProvider {

    public static String messageDeepseek(String message) throws IOException {
        return messageDeepseek(message, false);
    }

    public static String messageDeepseek(String message, boolean json) throws IOException {
        String systemMessage = "You only reply with songs that actually exist. DO NOT come up with song names of your own," +
                " you only reply with real discography.";
        if (message.isEmpty()) throw new IllegalArgumentException("Message cannot be empty.");
        if (message.contains("watch?v=")) return "{\"songs\":[\""+message+"\"]}";
        if (json) {
            systemMessage = "You are a bot that ONLY replies in the following format {\"songs\":[\"song1\", \"song2\"]}." +
                    "You DO NOT know about the existence of json or code formatting, you DO NOT use code blocks or" +
                    " any other kind of formatting but the provided one to reply. You only reply with songs that" +
                    " actually exist. DO NOT come up with song names of your own, you only reply with real discography." +
                    " If the prompt you receive contains a song name, an artist, or a combination of the two you reply in the" +
                    " already mentioned format, adding all information to the list like SONG by ARTIST.";
        }
        DeepseekProvider builder = new DeepseekProvider();
        OpenAIClient client = builder.DeepseekBuilder();

        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model("deepseek-chat")
                .maxCompletionTokens(1024)
                .addUserMessage(message)
                .addSystemMessage(systemMessage)
                .build();

        List<String> reply = client.chat().completions().create(createParams).choices().stream()
                .flatMap(choice -> choice.message().content().stream()).toList();
        System.out.println(reply);
        return reply.getFirst();
    }

    public OpenAIClient DeepseekBuilder() throws IOException {
        String key = getDeepSeekKey();
        return OpenAIOkHttpClient.builder()
                .baseUrl("https://api.deepseek.com/v1/")
                .apiKey(key)
                .build();
    }
}
