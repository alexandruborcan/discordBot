package proiect;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

import java.io.IOException;
import java.util.List;

import static proiect.SecretFileReader.*;


public class DeepseekProvider {
    DeepseekProvider(){};
    public OpenAIClient DeepseekBuilder() throws IOException {
        String key = getDeepSeekKey();
        return OpenAIOkHttpClient.builder()
                .baseUrl("https://api.deepseek.com/v1/")
                .apiKey(key)
                .build();
    }

    public static String messageDeepseek(String message) throws IOException {
        DeepseekProvider builder = new DeepseekProvider();
        OpenAIClient client = builder.DeepseekBuilder();

        ChatCompletionCreateParams createParams = ChatCompletionCreateParams.builder()
                .model("deepseek-chat")
                .maxCompletionTokens(1024)
                .addUserMessage(message)
                .addSystemMessage("reply only using the following format " +
                        "{\"songs\":[\"song1\", \"song2\"]}. Only reply with said format, no added text and reply as a string") // TODO
                .build();

        List<String> reply = client.chat().completions().create(createParams).choices().stream()
                .flatMap(choice -> choice.message().content().stream()).toList();
        return reply.getFirst();
    }
}
