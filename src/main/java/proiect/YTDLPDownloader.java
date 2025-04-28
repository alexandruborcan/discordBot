package proiect;

import java.io.*;
import java.net.URISyntaxException;

public class YTDLPDownloader {

    public static void runYtDlp(String videoUrl) throws IOException, InterruptedException, URISyntaxException {

        ProcessBuilder pb = new ProcessBuilder(Init.ytDlpPath, "-x", "--ffmpeg-location", Init.ffmpegPath, "--audio-format", "mp3",  videoUrl);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line); // or handle programmatically
            }
        }

        int exitCode = process.waitFor();
        System.out.println("yt-dlp finished with exit code: " + exitCode);
    }
}