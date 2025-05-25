package proiect;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.mockito.MockedStatic;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

class InitializeTest {

    @BeforeEach
    void setUp() {
        // Ensure bin directories exist
        new File("bin/win").mkdirs();
        new File("bin/mac").mkdirs();
        new File("bin/linux").mkdirs();
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testInitOnWindows() {
        try (MockedStatic<OSPackageManager> mocked = mockStatic(OSPackageManager.class)) {
            assertDoesNotThrow(Initialize::new);
            mocked.verify(() -> OSPackageManager.downloadFile(anyString(), anyString()), atLeastOnce());
            mocked.verify(() -> OSPackageManager.ffmpegUnzip(anyString(), anyString()), atLeastOnce());
        }
    }

    @Test
    @EnabledOnOs(OS.MAC)
    void testInitOnMac() {
        try (MockedStatic<OSPackageManager> mocked = mockStatic(OSPackageManager.class)) {
            assertDoesNotThrow(Initialize::new);
            mocked.verify(() -> OSPackageManager.downloadFile(anyString(), anyString()), atLeastOnce());
            mocked.verify(() -> OSPackageManager.ffmpegUnzip(anyString(), anyString()), atLeastOnce());
        }
    }

    @Test
    @EnabledOnOs(OS.LINUX)
    void testInitOnLinux() {
        try (MockedStatic<OSPackageManager> mocked = mockStatic(OSPackageManager.class)) {
            mocked.when(() -> OSPackageManager.tar2zip(anyString(), anyString())).thenAnswer(invocation -> null);
            assertDoesNotThrow(Initialize::new);
            mocked.verify(() -> OSPackageManager.downloadFile(anyString(), anyString()), atLeastOnce());
            mocked.verify(() -> OSPackageManager.tar2zip(anyString(), anyString()), atLeastOnce());
            mocked.verify(() -> OSPackageManager.ffmpegUnzip(anyString(), anyString()), atLeastOnce());
        }
    }
}