package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.isDirectory;
import static java.nio.file.Files.list;

public class MusicDownloader {
    private final static String downloadList = "download-list.txt";
    private static String inputDirectory;
    private static String outputDirectory;

    public static void main(String[] args) throws IOException {
        var dotEnv = Dotenv.configure()
                .directory(".")
                .ignoreIfMalformed().ignoreIfMissing().load();
        inputDirectory = dotEnv.get("INPUT_DIRECTORY");
        outputDirectory = dotEnv.get("OUTPUT_DIRECTORY");

        downloadVideos();
        convertDownloadedVideoToAudio();
    }

    private static void downloadVideos() throws IOException {

        Files.readAllLines(Paths.get(inputDirectory + "/" +downloadList))
                .forEach(item -> {
                    try {
                        @SuppressWarnings("deprecation")
                        var process = Runtime.getRuntime().exec(inputDirectory + "/../yt-dlp " + item);
                        //noinspection StatementWithEmptyBody
                        while (process.isAlive()) {}

                        try(var stream = Files.list(Path.of("."))) {
                            stream.filter(path -> !isDirectory(path))
                                    .filter(path -> path.toFile().getName().contains("mp4"))
                                    .forEach(path -> {
                                        try {
                                            Files.move(path, Path.of(inputDirectory+"/"+path.toFile().getName()));
                                        } catch (IOException e) {
                                            System.err.println("Failed move " + path.toFile().getName() + " to desired location. please do so manually");
                                        }
                                    });
                        }

                    } catch (IOException e) {
                        System.err.println("Failed to download item " + item);
                    }
                });
    }

    private static void convertDownloadedVideoToAudio() throws IOException {
        var inputDirectoryPath = Paths.get(inputDirectory);
        var outputDirectoryPath = Paths.get(outputDirectory);

        if (Files.notExists(inputDirectoryPath)) {
            System.err.println("Input directory doesn't exists. exiting");
            return;
        }

        if (Files.notExists(outputDirectoryPath)) {
            Files.createDirectory(outputDirectoryPath);
        }

        try (var stream = list(inputDirectoryPath)) {
            stream.filter(path -> !isDirectory(path)).filter(path -> !path.toFile().getName().contains(
                    downloadList)).forEach(videoFile -> {
                var source = videoFile.toFile();
                var target = new File(outputDirectory + source.getName().replace(
                        "4",
                        "3"));
                convertToMp3(source, target);
            });
        }
    }

    private static void convertToMp3(File source, File target) {
        try {
            var audioAttributes = new AudioAttributes();
            audioAttributes.setCodec("libmp3lame");
            audioAttributes.setBitRate(128000);
            audioAttributes.setChannels(2);
            audioAttributes.setSamplingRate(44100);

            var encodingAttributes = new EncodingAttributes();
            encodingAttributes.setOutputFormat("mp3");
            encodingAttributes.setAudioAttributes(audioAttributes);

            var encoder = new Encoder();
            encoder.encode(new MultimediaObject(source),
                           target,
                           encodingAttributes);
        } catch (EncoderException e) {
            System.err.println("Failed to convert file " + source.getName());
        }
    }
}
