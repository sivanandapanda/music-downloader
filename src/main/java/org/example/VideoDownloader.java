package org.example;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import static java.nio.file.Files.isDirectory;

public class VideoDownloader {
    private final static String downloadList = "download-list.txt";
    private static String downloadDirectory;

    public static void main(String[] args) throws IOException {
        var dotEnv = Dotenv.configure().directory(".").ignoreIfMalformed().ignoreIfMissing().load();
        downloadDirectory = dotEnv.get("VIDEO_DOWNLOAD_DIRECTORY");
        var process = Runtime.getRuntime()
                .exec(downloadDirectory + "/../yt-dlp https://xhamster2.com/videos/shower-love-7278789");
        //noinspection StatementWithEmptyBody
        while (process.isAlive()) {}

//        downloadVideos();
    }

    private static void downloadVideos() throws IOException {
        var tasks = Files.readAllLines(Paths.get(downloadDirectory + "/../" + downloadList));

        int batchSize = Runtime.getRuntime().availableProcessors() / 3;

        IntStream.range(0, (tasks.size() + batchSize - 1) / batchSize)
                .mapToObj(i -> tasks.subList(i * batchSize, Math.min((i + 1) * batchSize, tasks.size())))
                .forEach(list -> {
                    downloadInBatches(list);
                    System.out.println("Done with list " + list);
                });
    }

    private static void downloadInBatches(List<String> tasks) {
        try (var executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2)) {
            var futures = new ArrayList<Future>();
            var counter = new CountDownLatch(tasks.size());

            tasks.forEach(item -> {
                var future = executor.submit(() -> {
                    try {
                        var process = Runtime.getRuntime().exec(downloadDirectory + "/../yt-dlp " + item);
                        //noinspection StatementWithEmptyBody
                        while (process.isAlive()) {}

                        try (var stream = Files.list(Path.of("."))) {
                            stream.filter(path -> !isDirectory(path))
                                    .filter(path -> path.toFile().getName().contains("mp4")).forEach(path -> {
                                        try {
                                            Files.move(path, Path.of(downloadDirectory + "/" + path.toFile().getName()));
                                        } catch (IOException e) {
                                            System.err.println("Failed move " + path.toFile()
                                                    .getName() + " to desired location. please do so manually");
                                        }
                                    });
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to download item " + item + e.getMessage());
                    }
                });

                futures.add(future);
            });

            for (var future: futures) {
                while (future.isDone()) {
                    counter.countDown();
                }
            }

            counter.await();
        } catch (InterruptedException e) {
            System.out.println("counter interrupted" + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
