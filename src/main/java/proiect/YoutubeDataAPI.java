package proiect;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * A utility class that interacts with the YouTube Data API v3 to perform keyword-based
 * video searches and extract video links for a given list of song titles.
 * <p>
 * This class provides:
 * <ul>
 *   <li>Initialization of the YouTube service.</li>
 *   <li>Searching YouTube for a single video based on a keyword.</li>
 *   <li>Batch searching YouTube for video links from an input JSON structure.</li>
 * </ul>
 * <p>
 * API key is retrieved using {@link SecretFileReader#getYouTubeDataApiKey()}.
 * <p>
 * Dependencies:
 * - Google API Client Library for Java
 * - org.json (JSON processing)
 */

public class YoutubeDataAPI {
    private static final String API_KEY;
    private static final String APPLICATION_NAME = "Youtube API";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    // Static block to load the API key from an external file
    static {
        try {
            API_KEY = SecretFileReader.getYouTubeDataApiKey();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes and returns a YouTube service client.
     *
     * @return an authorized {@link YouTube} service object
     * @throws GeneralSecurityException if a security error occurs during initialization
     * @throws IOException              if an I/O error occurs while creating the HTTP transport
     */

    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Searches YouTube for the first video that matches the given keyword.
     *
     * @param keyWord the keyword or song title to search for
     * @return the full YouTube video URL of the top search result, or a message if no results are found
     * @throws GeneralSecurityException if a security error occurs during API access
     * @throws IOException              if an error occurs during the API request
     */

    public static String searchYoutube(String keyWord) throws GeneralSecurityException, IOException {
        YouTube youtubeService = getService();
        YouTube.Search.List request = youtubeService.search().list("snippet");

        SearchListResponse response = request
                .setPart("snippet")
                .setQ(keyWord)
                .setType("video")
                .setMaxResults(1L)
                .setKey(API_KEY)
                .execute();

        JSONObject jsonResponse = new JSONObject(response.toString());

        JSONArray items = jsonResponse.getJSONArray("items");
        if (items.isEmpty()) {
            return "No result found";
        }

        JSONObject firstItem = items.getJSONObject(0);
        JSONObject idObject = firstItem.getJSONObject("id");
        String videoId = idObject.getString("videoId");

        return "https://www.youtube.com/watch?v=" + videoId;
    }

    /**
     * Extracts video links for each song listed in the input JSON string.
     * The input must be a JSON object containing a "songs" array.
     * Example:
     * <pre>
     * {
     *   "songs": ["Song Title 1", "Song Title 2"]
     * }
     * </pre>
     *
     * @param input the input JSON string containing an array of song titles
     * @return a JSON object with a "links" array of corresponding YouTube video URLs
     * @throws GeneralSecurityException if a security error occurs during API access
     * @throws IOException              if an error occurs during any API call
     */

    public static JSONObject extractSongLinks(String input) throws GeneralSecurityException, IOException {
        JSONObject responseObject = new JSONObject(input);
        JSONArray songsArray = responseObject.getJSONArray("songs");
        JSONObject result = new JSONObject();
        JSONArray linksArray = new JSONArray();

        for (int i = 0; i < songsArray.length(); i++) {
            String song = songsArray.getString(i);
            String link = searchYoutube(song);
            linksArray.put(link);
        }

        result.put("links", linksArray);
        return result;
    }
}
