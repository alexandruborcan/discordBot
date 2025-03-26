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

    private static final String TEST_FILE_PATH = "secrets.json";

    @BeforeEach
    void setUp() throws IOException{
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
    void getAmazonPollyKey() throws IOException {
        assertEquals("test-amazon-key", SecretFileReader.getAmazonPollyKey());
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