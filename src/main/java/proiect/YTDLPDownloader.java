package proiect;

import java.io.*;

public class YTDLPDownloader {
    static String ytDlpPath;
    static String ffmpegPath;

    public static void runYtDlp(String videoUrl) throws IOException, InterruptedException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            ytDlpPath = "bin/win/yt-dlp.exe";
            ffmpegPath = "bin/win/ffmpeg.exe";

        } else if (os.contains("mac")) {
            ytDlpPath = "bin/mac/yt-dlp";
            ffmpegPath = "bin/mac/ffmpeg";
            if(!(new File("bin/mac/yt-dlp").setExecutable(true))){
                throw new FileNotFoundException();
            }
            if(!(new File("bin/mac/ffmpeg").setExecutable(true))){
                throw new FileNotFoundException();
            }
            if(!(new File("bin/mac/ffprobe").setExecutable(true))){
                throw new FileNotFoundException();
            }
        } else {
            ytDlpPath = "bin/linux/yt-dlp";
            ffmpegPath = "bin/linux/ffmpeg";
            if(!(new File("bin/linux/ffmpeg").setExecutable(true))){
                throw new FileNotFoundException();
            }
            if(!(new File("bin/linux/ffprobe").setExecutable(true))){
                throw new FileNotFoundException();
            }
        }

        ProcessBuilder pb = new ProcessBuilder(ytDlpPath, "-x", "--ffmpeg-location", ffmpegPath, "--audio-format", "mp3",  videoUrl);
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