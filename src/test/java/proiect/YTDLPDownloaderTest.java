package proiect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

public class YTDLPDownloaderTest {

    private final String dummyVideoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";

    @BeforeEach
    void setUp() {
        Initialize.ytDlpPath = "yt-dlp";
        Initialize.ffmpegPath = "ffmpeg";
    }

    @Test
    void testRunYtDlp_successfulExecution() throws InterruptedException{
        Process mockProcess = mock(Process.class);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Download complete".getBytes()));
        when(mockProcess.waitFor()).thenReturn(0);

        try (MockedConstruction<ProcessBuilder> mocked = mockConstruction(ProcessBuilder.class,
                (mockBuilder, context) -> {
                    when(mockBuilder.command(anyList())).thenReturn(mockBuilder);
                    when(mockBuilder.redirectErrorStream(true)).thenReturn(mockBuilder);
                    when(mockBuilder.start()).thenReturn(mockProcess);
                })) {

            assertDoesNotThrow(() -> YTDLPDownloader.runYtDlp(dummyVideoUrl));

            verify(mockProcess, times(1)).waitFor();
        }
    }

    @Test
    void testRunYtDlp_processFails() throws InterruptedException{
        Process mockProcess = mock(Process.class);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Some error".getBytes()));
        when(mockProcess.waitFor()).thenReturn(1); // Non-zero exit code

        try (MockedConstruction<ProcessBuilder> mocked = mockConstruction(ProcessBuilder.class,
                (mockBuilder, context) -> {
                    when(mockBuilder.command(anyList())).thenReturn(mockBuilder);
                    when(mockBuilder.redirectErrorStream(true)).thenReturn(mockBuilder);
                    when(mockBuilder.start()).thenReturn(mockProcess);
                })) {

            assertDoesNotThrow(() -> YTDLPDownloader.runYtDlp(dummyVideoUrl));

            verify(mockProcess, times(1)).waitFor();
        }
    }
}
