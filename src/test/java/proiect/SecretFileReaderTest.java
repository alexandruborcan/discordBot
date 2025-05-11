package proiect;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
class SecretFileReaderTest {

    private static final String TEST_FILE_PATH = "secrets_test.json";
    private static String originalFilePath;  // To store the original path

    /**
     * Helper method to set the file path for testing.
     */
    void overrideFilePath() {
        try {
            // Access the FILE_PATH field using reflection
            var field = SecretFileReader.class.getDeclaredField("FILE_PATH");
            field.setAccessible(true);  // Make the field accessible

            // Save the original file path before modifying it
            originalFilePath = (String) field.get(null);  // Get the original value

            // Set the new value to FILE_PATH
            field.set(null, TEST_FILE_PATH);
        } catch (Exception e) {
            fail("Could not override file path");
        }
    }

    void restoreFilePath() {
        try {
            // Restore the original file path after the test
            var field = SecretFileReader.class.getDeclaredField("FILE_PATH");
            field.setAccessible(true);
            field.set(null, originalFilePath);  // Set back to the original path
        } catch (Exception e) {
            fail("Could not restore original file path");
        }
    }

    @BeforeEach
    void setUp() throws IOException{
        overrideFilePath();
        // Setting up the test JSON
        JSONObject json = new JSONObject();
        json.put("discord", "test-discord-key");
        json.put("youtubeDataApi", "test-youtube-key");
        json.put("amazonPolly", "test-amazon-key");
        json.put("deepSeek", "test-deepseek-key");

        try (FileWriter writer = new FileWriter(TEST_FILE_PATH)) {
            writer.write(json.toString());
        }
    }

    @AfterEach
    void tearDown() {
        new File(TEST_FILE_PATH).delete();
        restoreFilePath();
    }

    @Test
    void getDiscordKey() throws IOException {
        assertEquals("test-discord-key", SecretFileReader.getDiscordKey());
    }

    @Test
    void getYouTubeDataApiKey() throws IOException {
        assertEquals("test-youtube-key", SecretFileReader.getYouTubeDataApiKey());
    }

    @Test
    void getAmazonPollyAccessKey() throws IOException {
        assertEquals("test-amazon-key", SecretFileReader.getAmazonPollyAccessKey());
    }

    @Test
    void getAmazonPollySecretKey() throws IOException {
        assertEquals("test-amazon-key", SecretFileReader.getAmazonPollySecretKey());
    }

    @Test
    void getDeepSeekKey() throws IOException {
        assertEquals("test-deepseek-key", SecretFileReader.getDeepSeekKey());
    }

    @Test
    void testMissingFileThrowsIOException() {
        new File(TEST_FILE_PATH).delete(); // Ensure file does not exist

        assertThrows(IOException.class, SecretFileReader::getDiscordKey);
    }

    @Test
    void testInvalidJsonThrowsJSONException() throws IOException, JSONException {
        try (FileWriter writer = new FileWriter(TEST_FILE_PATH)) {
            writer.write("Invalid JSON Content"); // Corrupted JSON
        }
        assertThrows(org.json.JSONException.class, SecretFileReader::getDiscordKey);
    }

    @Test
    void testMissingKeyThrowsJSONException() throws IOException, JSONException {
        JSONObject json = new JSONObject(); // Empty JSON, missing all keys
        try (FileWriter writer = new FileWriter(TEST_FILE_PATH)) {
            writer.write(json.toString());
        }
        assertThrows(org.json.JSONException.class, SecretFileReader::getDiscordKey);
    }

    @Test
    void testEmptyKeyReturnsEmptyString() throws IOException {
        JSONObject json = new JSONObject();
        json.put("discord", ""); // Empty value
        try (FileWriter writer = new FileWriter(TEST_FILE_PATH)) {
            writer.write(json.toString());
        }
        assertEquals("", SecretFileReader.getDiscordKey());
    }

    @Test
    void testFormattedJson() throws IOException {
        String formattedJson = "{\n  \"discord\" : \"test-discord-key\"\n}";
        try (FileWriter writer = new FileWriter(TEST_FILE_PATH)) {
            writer.write(formattedJson);
        }
        assertEquals("test-discord-key", SecretFileReader.getDiscordKey());
    }

}