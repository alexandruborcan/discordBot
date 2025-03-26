package proiect;

import java.io.FileReader;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * A utility class to read secret values from a JSON file.
 */
public class SecretFileReader {

    private static final String FILE_PATH = "secrets.json";

    /**
     * Reads the secrets.json file and returns the value associated with the given key.
     *
     * @param key the key whose value needs to be fetched from the secrets.json file.
     * @return the value for the given key from the secrets.json file, or null if an error occurs.
     * @throws IOException if an I/O error occurs while reading the file.
     * @throws JSONException if the JSON is invalid or the key is not found.
     */
    private static String getValue(String key) throws IOException, JSONException {
        try (FileReader reader = new FileReader(FILE_PATH)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            return jsonObject.getString(key);
        }
    }

    /**
     * Reads the secrets.json file and returns the value associated with the key "discord".
     *
     * @return the value for the key "discord" from the secrets.json file, or null if an error occurs.
     * @throws IOException if an I/O error occurs while reading the file.
     * @throws JSONException if the JSON is invalid or the key is not found.
     */
    public static String getDiscordKey() throws IOException, JSONException {
        return getValue("discord");
    }

    /**
     * Reads the secrets.json file and returns the value associated with the key "youtubeDataApi".
     *
     * @return the value for the key "youtubeDataApi" from the secrets.json file, or null if an error occurs.
     * @throws IOException if an I/O error occurs while reading the file.
     * @throws JSONException if the JSON is invalid or the key is not found.
     */
    public static String getYouTubeDataApiKey() throws IOException, JSONException {
        return getValue("youtubeDataApi");
    }

    /**
     * Reads the secrets.json file and returns the value associated with the key "amazonPolly".
     *
     * @return the value for the key "amazonPolly" from the secrets.json file, or null if an error occurs.
     * @throws IOException if an I/O error occurs while reading the file.
     * @throws JSONException if the JSON is invalid or the key is not found.
     */
    public static String getAmazonPollyKey() throws IOException, JSONException {
        return getValue("amazonPolly");
    }

    /**
     * Reads the secrets.json file and returns the value associated with the key "deepSeek".
     *
     * @return the value for the key "deepSeek" from the secrets.json file, or null if an error occurs.
     * @throws IOException if an I/O error occurs while reading the file.
     * @throws JSONException if the JSON is invalid or the key is not found.
     */
    public static String getDeepSeekKey() throws IOException, JSONException {
        return getValue("deepSeek");
    }
}