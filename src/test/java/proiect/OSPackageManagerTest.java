package proiect;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.python.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.python.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.python.apache.commons.compress.compressors.xz.XZCompressorOutputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class OSPackageManagerTest {

    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("os-package-test");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @Test
    void testGetFileSize() throws Exception {
        URL url = new URI("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf").toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        long size = OSPackageManager.getFileSize(url);
        long realSize = connection.getContentLengthLong();
        assertTrue(size >= 0, "The size should be greater than or equal to 0");
        assertEquals(realSize, size, "The size from getFileSize should match the real content length");
    }

    @Test
    void testDownloadFile_MockedConnection() throws Exception {
        Path tempFile = tempDir.resolve("testDownload.pdf");

        try {
            OSPackageManager.downloadFile("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf", tempFile.toString());
            assertTrue(Files.exists(tempFile));
            assertTrue(Files.size(tempFile) > 0);
        } catch (Exception e) {
            fail("Download threw exception: " + e.getMessage());
        }
    }

    @Test
    void testFfmpegUnzip_extractsOnlyBinFiles() throws IOException {
        Path zipPath = tempDir.resolve("test.zip");
        Path extractTo = tempDir.resolve("extract");

        // Create ZIP with dummy entries
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            zos.putNextEntry(new ZipEntry("ffmpeg-master-latest-win64-gpl/bin/ffmpeg"));
            zos.write("ffmpeg binary".getBytes());
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("other-folder/ignore.txt"));
            zos.write("should not be extracted".getBytes());
            zos.closeEntry();
        }

        OSPackageManager.ffmpegUnzip(zipPath.toString(), extractTo.toString());

        Path extracted = extractTo.resolve("ffmpeg");
        assertTrue(Files.exists(extracted));
        assertTrue(Files.readString(extracted).contains("ffmpeg"));
    }

    @Test
    void testTar2Zip_extractsEntriesAndDeletesSource() throws IOException {
        // Path to the tar.xz and zip output files
        Path tarPath = tempDir.resolve("test.tar.xz");
        Path zipPath = tempDir.resolve("out.zip");

        // Create a valid .tar.xz file for testing
        createTarXZ(tarPath);

        // Call tar2zip method to convert the .tar.xz file to .zip
        OSPackageManager.tar2zip(tarPath.toString(), zipPath.toString());

        // Verify that the zip file was created
        assertTrue(Files.exists(zipPath), "The .zip file was not created.");

        // Verify that the .tar.xz file was deleted after conversion
        assertFalse(Files.exists(tarPath), "The .tar.xz file was not deleted.");

        // Verify that the .zip file contains the extracted dummy file
        try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
            assertTrue(zipFile.stream().anyMatch(entry -> entry.getName().equals("dummy.txt")),
                    "The dummy.txt file was not found in the .zip file.");
        }
    }

    // Helper method to create a valid .tar.xz file
    private void createTarXZ(Path tarPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tarPath.toFile());
             XZCompressorOutputStream xzOut = new XZCompressorOutputStream(fos);
             TarArchiveOutputStream tarOut = new TarArchiveOutputStream(xzOut)) {

            tarOut.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            // Create a dummy file inside the tar
            File dummyFile = new File(tempDir.toFile(), "dummy.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dummyFile))) {
                writer.write("This is a dummy file.");
            }

            TarArchiveEntry tarEntry = new TarArchiveEntry(dummyFile, dummyFile.getName());
            tarOut.putArchiveEntry(tarEntry);

            try (FileInputStream fis = new FileInputStream(dummyFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) != -1) {
                    tarOut.write(buffer, 0, length);
                }
            }

            tarOut.closeArchiveEntry();
            tarOut.finish();
        }
    }
}

