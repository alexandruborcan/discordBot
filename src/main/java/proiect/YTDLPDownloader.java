package proiect;

import java.io.*;

/**
 * Utility class for downloading and converting YouTube videos to MP3 format using yt-dlp.
 */
public class YTDLPDownloader {

    /**
     * Executes the yt-dlp process to download and convert a YouTube video to MP3 format.
     *
     * @param videoUrl the URL of the YouTube video to download
     * @throws IOException if an I/O error occurs when starting or reading from the process
     * @throws InterruptedException if the current thread is interrupted while waiting for the process to finish
     */
    public static void runYtDlp(String videoUrl) throws IOException, InterruptedException{

        ProcessBuilder pb = new ProcessBuilder(
                Init.ytDlpPath,
                "-x", "--ffmpeg-location", Init.ffmpegPath,
                "--audio-format", "mp3",
                videoUrl);
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