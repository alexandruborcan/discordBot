package proiect;

import java.io.*;
import java.util.Random;

/**
 * Utility class for downloading and converting YouTube videos to MP3 format using yt-dlp.
 */
public class YTDLPDownloader {

    /**
     * Executes the yt-dlp process to download and convert a YouTube video to MP3 format.
     *
     * @param videoUrl the URL of the YouTube video to download
     * @return The name of the file that was downloaded
     * @throws IOException if an I/O error occurs when starting or reading from the process
     * @throws InterruptedException if the current thread is interrupted while waiting for the process to finish
     */
    public static String runYtDlp(String videoUrl) throws IOException, InterruptedException{
        Random rand = new Random();
        String absoluteTime = String.valueOf(System.currentTimeMillis());
        String fileName = "yt-dlp-" + absoluteTime + rand.nextInt() + ".mp3";
        ProcessBuilder pb = new ProcessBuilder(
                Initialize.ytDlpPath,
                "-x", "--ffmpeg-location", Initialize.ffmpegPath,
                "--embed-metadata",
                "--audio-format", "mp3",
                "-S", "+size", // smallest size available
                "--output", fileName,
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
        return fileName;
    }
}