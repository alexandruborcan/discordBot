package proiect;

import org.python.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.python.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.python.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for managing OS-specific operations related to downloading, extracting,
 * and converting media tool packages such as ffmpeg and yt-dlp.
 */
public class OSPackageManager {

    /**
     * Downloads a file from a specified URL to a local file path, with progress printed to the console.
     *
     * @param urlStr the URL of the file to download.
     * @param file   the path where the downloaded file should be saved.
     * @throws IOException        if an I/O error occurs during the download.
     * @throws URISyntaxException if the URL is malformed.
     */
    public static void downloadFile(String urlStr, String file) throws IOException, URISyntaxException {
        URL url = new URI(urlStr).toURL();
        long size = getFileSize(url);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        double count;
        double downloaded = 0;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, (int) count);
            downloaded = downloaded + count;
            double progress = downloaded / size * 100;
            System.out.printf("Downloaded %.2f%%\n", progress);
        }
        fis.close();
        bis.close();
    }

    /**
     * Extracts only relevant ffmpeg binaries from a given ZIP archive into a destination directory.
     *
     * @param zipFilePath   the path to the ZIP archive from local directory.
     * @param destDirectory the directory where the extracted files will be saved.
     * @throws IOException if an error occurs during extraction.
     */
    public static void ffmpegUnzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new IOException("Unable to create directory " + destDirectory);
            }
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry = zipIn.getNextEntry();

            while (entry != null) {
                String entryName = entry.getName();
                System.out.println("Found entry: " + entryName); // <-- debug print
                if (entryName.startsWith("ffmpeg-master-latest-win64-gpl/bin/") && !entry.getName().endsWith("/")) {
                    // Only handle files under "bin/" (skip folders)
                    String fileName = entryName.substring("ffmpeg-master-latest-win64-gpl/bin/".length());
                    if (!fileName.contains("/")) {
                        // Only pick direct children of bin/, no subdirectories
                        String filePath = destDirectory + File.separator + fileName;
                        extractFile(zipIn, filePath);
                    }
                } else if (entryName.startsWith("ffmpeg-master-latest-linux64-gpl/bin/") && !entry.getName().endsWith("/")) {
                    // Only handle files under "bin/" (skip folders)
                    String fileName = entryName.substring("ffmpeg-master-latest-linux64-gpl/bin/".length());
                    if (!fileName.contains("/")) {
                        // Only pick direct children of bin/, no subdirectories
                        String filePath = destDirectory + File.separator + fileName;
                        extractFile(zipIn, filePath);
                    }
                } else if (entryName.endsWith("ffmpeg") && !entry.isDirectory()) {
                    // Extract only the filename, no path
                    String fileName = new File(entryName).getName(); // gets "ffmpeg"
                    String filePath = destDirectory + File.separator + fileName;
                    extractFile(zipIn, filePath);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }

    /**
     * Extracts a single file from a ZIP input stream to a specified output path.
     *
     * @param zipIn    the input stream of the ZIP file.
     * @param filePath the file path where the extracted file will be saved.
     * @throws IOException if an error occurs during extraction.
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        File outFile = new File(filePath);
        if (!outFile.getParentFile().mkdirs()) { // Create parent directories if needed
            throw new IOException("Unable to create directory " + outFile.getParentFile());
        }
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    /**
     * Retrieves the size of a remote file via a HEAD request.
     *
     * @param url the URL of the remote file.
     * @return the file size in bytes.
     */
    public static long getFileSize(URL url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            return conn.getContentLengthLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Converts a .tar.xz archive into a ZIP file.
     *
     * @param tarPath the path to the input .tar.xz file.
     * @param zipPath the path where the output ZIP file will be saved.
     */
    public static void tar2zip(String tarPath, String zipPath) {
        try (TarArchiveInputStream tarInput = new TarArchiveInputStream(
                new XZCompressorInputStream(new FileInputStream(tarPath)));
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {

            TarArchiveEntry currentEntry;
            byte[] buffer = new byte[8192];

            while ((currentEntry = tarInput.getNextTarEntry()) != null) {
                if (!currentEntry.isDirectory()) { // skip directories
                    ZipEntry zipEntry = new ZipEntry(currentEntry.getName());
                    zos.putNextEntry(zipEntry);

                    int read;
                    while ((read = tarInput.read(buffer)) != -1) {
                        zos.write(buffer, 0, read);
                    }
                    zos.closeEntry();
                }
            }

            // Delete the original tar.xz archive after conversion
            File dir = new File(tarPath);
            if (!dir.delete()) {
                throw new IOException("Unable to delete directory " + tarPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

