package proiect;

import org.python.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.python.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.python.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.python.apache.commons.compress.compressors.xz.XZCompressorInputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class OSPackageManager {

    public static void downloadFile(String urlStr, String file) throws IOException, URISyntaxException {
        URL url = new URI(urlStr).toURL();
        long size = getFileSize(url);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        double count;
        double downloaded = 0;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, (int)count);
            downloaded = downloaded + count;
            double progress = downloaded / size * 100;
            System.out.printf("Downloaded %.2f%%\n", progress);
        }
        fis.close();
        bis.close();
    }

    public static void ffmpegUnzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdirs();
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
                }
                else if(entryName.startsWith("ffmpeg-master-latest-linux64-gpl/bin/") && !entry.getName().endsWith("/")) {
                    // Only handle files under "bin/" (skip folders)
                    String fileName = entryName.substring("ffmpeg-master-latest-linux64-gpl/bin/".length());
                    if (!fileName.contains("/")) {
                        // Only pick direct children of bin/, no subdirectories
                        String filePath = destDirectory + File.separator + fileName;
                        extractFile(zipIn, filePath);
                    }
                }
                else if (entryName.endsWith("ffmpeg") && !entry.isDirectory()) {
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

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        File outFile = new File(filePath);
        outFile.getParentFile().mkdirs(); // Create parent directories if needed
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

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
            File dir = new File(tarPath);
            dir.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

