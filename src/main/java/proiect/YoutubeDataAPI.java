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

public class YoutubeDataAPI {
    private static final String API_KEY;
    private static final String APPLICATION_NAME = "Youtube API";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    static {
        try {
            API_KEY = SecretFileReader.getYouTubeDataApiKey();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new YouTube.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

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
