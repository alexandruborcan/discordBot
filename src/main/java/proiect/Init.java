package proiect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import static proiect.OSPackageManager.*;

public class Init {
    static String ytDlpPath;
    static String ffmpegPath;

    Init() throws IOException, URISyntaxException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {

            ytDlpPath = "bin/win/yt-dlp.exe";
            ffmpegPath = "bin/win/ffmpeg.exe";
            downloadFile("https://github.com/yt-dlp/yt-dlp/releases/download/2025.03.31/yt-dlp.exe", ytDlpPath);
            downloadFile("https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip", "bin/win/ffmpeg-builds.zip");
            ffmpegUnzip("bin/win/ffmpeg-builds.zip", "bin/win/");
            File dir = new File("bin/win/ffmpeg-builds.zip");
            dir.delete();

        } else if (os.contains("mac")) {
            ytDlpPath = "bin/mac/yt-dlp";
            ffmpegPath = "bin/mac/ffmpeg";
            downloadFile("https://github.com/yt-dlp/yt-dlp/releases/download/2025.03.31/yt-dlp_macos", ytDlpPath);
            downloadFile("https://evermeet.cx/ffmpeg/ffmpeg-7.1.1.zip", "bin/mac/ffmpeg-7.1.1.zip");
            ffmpegUnzip("bin/mac/ffmpeg-7.1.1.zip", "bin/mac/");
            File dir = new File("bin/mac/ffmpeg-7.1.1.zip");
            dir.delete();

            if(!(new File("bin/mac/yt-dlp").setExecutable(true))){
                throw new FileNotFoundException();
            }
            if(!(new File("bin/mac/ffmpeg").setExecutable(true))){
                throw new FileNotFoundException();
            }
        } else {
            ytDlpPath = "bin/linux/yt-dlp";
            ffmpegPath = "bin/linux/ffmpeg";
            downloadFile("https://github.com/yt-dlp/yt-dlp/releases/download/2025.03.31/yt-dlp_linux", ytDlpPath);
            downloadFile("https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz", "bin/linux/ffmpeg-builds.tar.xz");
            tar2zip("bin/linux/ffmpeg-builds.tar.xz", "bin/linux/ffmpeg-builds.zip");
            ffmpegUnzip("bin/linux/ffmpeg-builds.zip", "bin/linux/");
            File dir = new File("bin/linux/ffmpeg-builds.zip");
            dir.delete();
            if(!(new File("bin/linux/ffmpeg").setExecutable(true))){
                throw new FileNotFoundException();
            }
            if(!(new File("bin/linux/ffprobe").setExecutable(true))){
                throw new FileNotFoundException();
            }
        }
    }
}
