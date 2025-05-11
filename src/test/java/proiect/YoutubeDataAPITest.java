package proiect;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class YoutubeDataAPITest {

    @Test
    public void testSearchYoutubeReturnsLink() throws Exception {
        String videoId = "abc123";
        String mockJson = new JSONObject()
                .put("items", new JSONArray()
                        .put(new JSONObject()
                                .put("id", new JSONObject()
                                        .put("videoId", videoId))))
                .toString();

        SearchListResponse mockResponse = mock(SearchListResponse.class);
        YouTube mockYouTube = mock(YouTube.class);
        YouTube.Search mockSearch = mock(YouTube.Search.class);
        YouTube.Search.List mockList = mock(YouTube.Search.List.class);

        try (MockedStatic<YoutubeDataAPI> mockedStatic = mockStatic(YoutubeDataAPI.class, CALLS_REAL_METHODS)) {
            // Stub static getService() method
            mockedStatic.when(YoutubeDataAPI::getService).thenReturn(mockYouTube);

            // Build mock chain
            when(mockYouTube.search()).thenReturn(mockSearch);
            when(mockSearch.list("snippet")).thenReturn(mockList);
            when(mockList.setPart(anyString())).thenReturn(mockList);
            when(mockList.setQ(anyString())).thenReturn(mockList);
            when(mockList.setType(anyString())).thenReturn(mockList);
            when(mockList.setMaxResults(anyLong())).thenReturn(mockList);
            when(mockList.setKey(anyString())).thenReturn(mockList);

            // Final method mocking
            when(mockList.execute()).thenReturn(mockResponse);
            when(mockResponse.toString()).thenReturn(mockJson); // ← key line

            String result = YoutubeDataAPI.searchYoutube("test");
            assertEquals("https://www.youtube.com/watch?v=" + videoId, result);
        }
    }

    @Test
    public void testExtractSongLinks() throws Exception {
        String inputJson = new JSONObject()
                .put("songs", new JSONArray().put("Song A").put("Song B"))
                .toString();

        try (MockedStatic<YoutubeDataAPI> mockedStatic = mockStatic(YoutubeDataAPI.class, CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> YoutubeDataAPI.searchYoutube("Song A")).thenReturn("linkA");
            mockedStatic.when(() -> YoutubeDataAPI.searchYoutube("Song B")).thenReturn("linkB");

            JSONObject result = YoutubeDataAPI.extractSongLinks(inputJson);
            JSONArray links = result.getJSONArray("links");

            assertEquals(2, links.length());
            assertEquals("linkA", links.getString(0));
            assertEquals("linkB", links.getString(1));
        }
    }
}
