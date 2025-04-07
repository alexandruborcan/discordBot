package proiect;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PollyHandler {
    private final PollyClient pollyClient;

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

    public File synthesizeSpeech(String text) throws IOException {
        SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                .text(text)
                .outputFormat(OutputFormat.MP3)
                .voiceId(VoiceId.JOANNA) // You can choose any voice available in the AWS Polly
                .engine("neural")
                .build();

        ResponseInputStream response = pollyClient.synthesizeSpeech(request);
        File mp3File = new File("output.mp3");

        FileOutputStream out = new FileOutputStream(mp3File);
        out.write(response.readAllBytes());
        out.close();

        return mp3File;
    }
}
