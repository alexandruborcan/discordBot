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

public class PollyHandler {
    private final PollyClient pollyClient;

    /**
     * Constructor for PollyHandler.
     * Initializes the PollyClient with AWS credentials.
     */
    public PollyHandler() {
        String accessKey;
        String privateKey;
        try {
            accessKey = SecretFileReader.getAmazonPollyAccessKey();
            privateKey = SecretFileReader.getAmazonPollySecretKey();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, privateKey);
        this.pollyClient = PollyClient.builder()
                .region(Region.EU_CENTRAL_1) // Set your desired AWS region
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }

    /**
     * Synthesizes speech from the given text and saves it to an MP3 file.
     * The MP3 file is saved in the current working directory with the name being
     * the first 20 characters of the text followed by ".mp3".
     *
     * @param text the text to synthesize
     * @return the MP3 file containing the synthesized speech
     * @throws IOException if an error occurs while writing the file
     */
    public File synthesizeSpeech(String text) throws IOException {
        // TODO: Try out different voices and pick the best one
        SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                .text(text)
                .outputFormat(OutputFormat.MP3)
                .voiceId(VoiceId.JOANNA) // You can choose any voice available in AWS Polly
                .engine("neural")
                .build();

        ResponseInputStream<SynthesizeSpeechResponse> response = pollyClient.synthesizeSpeech(request);
        // Get the first 20 characters if the text is longer than 20 characters
        // Otherwise get the whole text
        String fileName = text.length() > 20 ? text.substring(0, 20) : text;
        File mp3File = new File(fileName);

        FileOutputStream out = new FileOutputStream(mp3File);
        out.write(response.readAllBytes());
        out.close();

        return mp3File;
    }
}
