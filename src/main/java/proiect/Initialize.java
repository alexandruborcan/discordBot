package proiect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static proiect.OSPackageManager.*;

/**
 * Initialization utility class for setting up yt-dlp and ffmpeg executables based on the user's operating system.
 * <p>
 *     This class detects the operating system (Windows, macOS or Linux), ensures the appropriate directories exist,
 *     and downloads and prepares yt-dlp and ffmpeg if they are not already present. It also handles decompression
 *     and permission settings for Unix-based systems.
 * </p>
 */
public class Initialize {
    /** The file path to the yt-dlp executable, set according to the operating system. */
    static String ytDlpPath;

    /** The file path to the ffmpeg executable, set according to the operating system. */
    static String ffmpegPath;

    /**
     * Initializes the environment by setting up the paths for yt-dlp and ffmpeg executables
     * based on the operating system. It ensures that the required directories and files
     * are present, downloading and extracting them if necessary.
     *
     * @throws IOException if there is an error creating directories, downloading files, or setting permissions.
     * @throws URISyntaxException if a string could not be parsed as a URI reference.
     */
    public static void init() throws IOException, URISyntaxException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            checkAndCreateDirectory("win");
            ytDlpPath = "bin/win/yt-dlp.exe";
            ffmpegPath = "bin/win/ffmpeg.exe";

            if(Files.notExists(Paths.get(ytDlpPath))) {
                downloadFile("https://github.com/yt-dlp/yt-dlp/releases/download/2025.03.31/yt-dlp.exe", ytDlpPath);
            }
            if(Files.notExists(Paths.get(ffmpegPath))) {
                downloadFile("https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip", "bin/win/ffmpeg-builds.zip");
                ffmpegUnzip("bin/win/ffmpeg-builds.zip", "bin/win/");
                File dir = new File("bin/win/ffmpeg-builds.zip");
                if(!dir.delete()){
                    throw new IOException("Could not delete ffmpeg zip file");
                }
            }
        } else if (os.contains("mac")) {
            checkAndCreateDirectory("mac");
            ytDlpPath = "bin/mac/yt-dlp";
            ffmpegPath = "bin/mac/ffmpeg";

            if(Files.notExists(Paths.get(ytDlpPath))) {
                downloadFile("https://github.com/yt-dlp/yt-dlp/releases/download/2025.03.31/yt-dlp_macos", ytDlpPath);
                if (!(new File("bin/mac/yt-dlp").setExecutable(true))) {
                    throw new FileNotFoundException();
                }
            }
            if(Files.notExists(Paths.get(ffmpegPath))) {
                downloadFile("https://evermeet.cx/ffmpeg/ffmpeg-7.1.1.zip", "bin/mac/ffmpeg-7.1.1.zip");
                ffmpegUnzip("bin/mac/ffmpeg-7.1.1.zip", "bin/mac/");
                File dir = new File("bin/mac/ffmpeg-7.1.1.zip");
                if(!dir.delete()) {
                    throw new IOException("Could not delete ffmpeg zip file");
                }
                if (!(new File("bin/mac/ffmpeg").setExecutable(true))) {
                    throw new FileNotFoundException();
                }
            }
        } else {
            checkAndCreateDirectory("linux");
            ytDlpPath = "bin/linux/yt-dlp";
            ffmpegPath = "bin/linux/ffmpeg";

            if(Files.notExists(Paths.get(ytDlpPath))) {
                downloadFile("https://github.com/yt-dlp/yt-dlp/releases/download/2025.03.31/yt-dlp_linux", ytDlpPath);
            }
            if(Files.notExists(Paths.get(ffmpegPath))) {
                downloadFile("https://github.com/BtbN/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz", "bin/linux/ffmpeg-builds.tar.xz");
                tar2zip("bin/linux/ffmpeg-builds.tar.xz", "bin/linux/ffmpeg-builds.zip");
                ffmpegUnzip("bin/linux/ffmpeg-builds.zip", "bin/linux/");
                File dir = new File("bin/linux/ffmpeg-builds.zip");
                if(!dir.delete()) {
                    throw new IOException("Could not delete ffmpeg zip file");
                }
                if (!(new File("bin/linux/ffmpeg").setExecutable(true))) {
                    throw new FileNotFoundException();
                }
                if (!(new File("bin/linux/ffprobe").setExecutable(true))) {
                    throw new FileNotFoundException();
                }
            }
        }
    }

    /**
     * Checks if the directory structure for the given OS exists, and creates it if necessary.
     *
     * @param os the name of the OS subdirectory (e.g., "win", "mac", or "linux")
     * @throws IOException if the directory cannot be created
     */
    public static void checkAndCreateDirectory(String os) throws IOException {
        Path path = Paths.get("bin/" + os);
        if (!Files.exists(path)) {
            String currentDirectory = System.getProperty("user.dir");
            String binPath = currentDirectory + File.separator + "bin";
            File binDirectory = new File(binPath);

            if (!binDirectory.mkdir()) {
                throw new IOException("Failed to create bin directory");
            }

            String osPath = binPath + File.separator + os;
            File osDirectory = new File(osPath);
            if (!osDirectory.mkdir()) {
                throw new IOException("Failed to create " + os + "directory");
            }
        }
    }
}
