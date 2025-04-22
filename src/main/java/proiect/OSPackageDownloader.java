package proiect;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;

public class OSPackageDownloader {

    public static void windowspackageDownloader() throws IOException, URISyntaxException {
        URI file_uri = new URI("link");
        URL file_url = file_uri.toURL();

        BufferedInputStream in = new BufferedInputStream(file_url.openStream());
    }
}
