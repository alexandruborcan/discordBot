package proiect;

import com.openai.client.OpenAIClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static proiect.DeepseekProvider.messageDeepseek;

public class DeepseekProviderTest {

    @Test
    public void testMessageDeepseekWithoutJson() throws IOException {
        // Test when the messageDeepseek method is called without JSON format
        String result = messageDeepseek("What are some songs like 'Shape of You'?");

        // Validate the result (the actual expected output will depend on what the API returns)
        assertNotNull(result, "The result should not be null");
        assertTrue(result.contains("song"), "The result should contain song data.");
    }

    @Test
    public void testMessageDeepseekWithJson() throws IOException {
        // Test when the messageDeepseek method is called with JSON format
        String result = messageDeepseek("What are some songs like 'Shape of You'?", true);

        assertNotNull(result, "The result should not be null");

        // Try to parse the response as a JSON object
        try {
            JSONObject jsonResponse = new JSONObject(result);

            // Check if the "songs" key exists and if it's an array
            assertTrue(jsonResponse.has("songs"), "The JSON response should contain a 'songs' key.");
            JSONArray songsArray = jsonResponse.getJSONArray("songs");
            assertFalse(songsArray.isEmpty(), "The 'songs' array should not be empty.");
        } catch (Exception e) {
            fail("The result is not a valid JSON response: " + e.getMessage());
        }
    }

    @Test
    public void testDeepseekBuilder() throws IOException {
        DeepseekProvider deepseekProvider = new DeepseekProvider();
        // Test the behavior of the DeepseekBuilder method
        OpenAIClient client = deepseekProvider.DeepseekBuilder();

        // Validate that the client is not null and that it has been initialized correctly
        assertNotNull(client, "The OpenAIClient should not be null");
    }

    @Test
    public void testMessageDeepseekWithEmptyOrNullString() throws IOException {
        // Test when the messageDeepseek method is called with an empty string
        // The method should throw an IllegalArgumentException for empty or null input
        assertThrows(IllegalArgumentException.class, () -> messageDeepseek(""), "The input should not be empty or null.");
    }

    @Test
    public void testMessageDeepseekEmptyOutput() throws IOException {
        // Test when the messageDeepseek method is called with a normal string input
        // The result should not be empty or null
        assertNotNull(messageDeepseek("How are you?"), "The result should not be empty or null.");
    }

    @Test
    public void testMessageDeepseekWithSpecialCharacters() throws IOException {
        // Test when the messageDeepseek method is called with special characters as input
        // The result should not be null when special characters are passed
        assertNotNull(messageDeepseek("≽^•⩊•^≼"), "The result should not be null when special characters are passed.");
    }
}
