package proiect;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechResponse;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Vector;

public class PollyHandler {
    private static PollyClient pollyClient = null;
    public static Vector<String> fileNames = new Vector<>();

    /**
     * Constructor for PollyHandler.
     * Initializes the PollyClient with AWS credentials.
     * @throws IOException if there was a problem reading the credentials.
     */
    private PollyHandler() throws IOException {
        String accessKey;
        String privateKey;
        accessKey = SecretFileReader.getAmazonPollyAccessKey();
        privateKey = SecretFileReader.getAmazonPollySecretKey();

        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, privateKey);
        pollyClient = PollyClient.builder()
                .region(Region.EU_CENTRAL_1) // Set your desired AWS region
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    /**
     * Create a new PollyHandler instance.
     * You must call this function before using the synthesizeSpeech method.
     * Calling this function multiple times will not create multiple instances.
     * @throws IOException if there was a problem reading the credentials.
     */
    public static synchronized void create() throws IOException {
        if (pollyClient == null) {
            // Disabling this warning is okay because we're using a singleton pattern
            //noinspection InstantiationOfUtilityClass
            new PollyHandler();
        }
    }

    /**
     * Synthesizes speech from the given text and saves it to an MP3 file.
     * The MP3 file is saved in the current working directory with the name being
     * the first 20 characters of the text followed by ".mp3".
     *
     * @param text the text to synthesize
     * @return the MP3 file containing the synthesized speech
     * @throws IOException if an error occurs while writing the file
     * @throws IllegalStateException if the PollyClient is not initialized
     */
    public static String synthesizeSpeech(String text) throws IOException, IllegalStateException {
        if (pollyClient == null) {
            throw new IllegalStateException("PollyClient is not initialized. Call create() method first.");
        }

        // TODO: Try out different voices and pick the best one
        SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                .text(text)
                .outputFormat(OutputFormat.MP3)
                .voiceId(VoiceId.JOANNA) // You can choose any voice available in AWS Polly
                .engine("neural")
                .build();

        String fileName = generateFileName(text);
        try (ResponseInputStream<SynthesizeSpeechResponse> response = pollyClient.synthesizeSpeech(request);
             FileOutputStream out = new FileOutputStream(fileName)) {
            out.write(response.readAllBytes());
        }
        if (fileNames.size() > 10) {
            // Remove the oldest file if we have more than 10 files
            String oldestFileName = fileNames.removeFirst();
            File oldestFile = new File(oldestFileName);
            if (oldestFile.exists()) {
                oldestFile.delete();
            }
        }
        fileNames.add(fileName);
        return fileName;
    }

    /**
     * Generates a file name for the MP3 file based on the given text and the current timestamp.
     * The file name is created by stripping the text of any special characters (not letters),
     * taking the first 20 characters, appending the current timestamp in milliseconds,
     * and adding the ".mp3" extension.
     * @param text the text to be used in the file name
     * @return the generated file name
     */
    private static String generateFileName(String text) {
        String baseName = text.replaceAll("[^a-zA-Z]", "");
        baseName = baseName.length() > 20 ? baseName.substring(0, 20) : baseName;
        return baseName + "_" + Instant.now().toEpochMilli() + ".mp3";
    }
}
