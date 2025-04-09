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
        String systemMessage = "";
        if (json) {
            systemMessage = "You are a bot that ONLY replies in the following format {\"songs\":[\"song1\", \"song2\"]}." +
                    "You DO NOT know about the existence of json or code formatting, you DO NOT use code blocks or" +
                    " any other kind of formatting but the provided one to reply.";
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
