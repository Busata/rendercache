package org.veevi.rendercache.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.springframework.stereotype.Component;
import org.veevi.rendercache.configuration.RenderCacheProperties;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final RenderCacheProperties renderCacheProperties;

    @PostConstruct
    public void initializePath() {
        storagePath = Paths.get(renderCacheProperties.getStoragePath());
        log.info("Storage service initialized with path: {}", storagePath);
    }

    private Path storagePath;

    public boolean exists(String fileName) {
        Path filePath = storagePath.resolve(fileName);

        return Files.exists(filePath);
    }

    public ImageData load(String fileName) {
        Path filePath = storagePath.resolve(fileName);
        File inputFile = filePath.toFile();

        try {
            final var bufferedImage = ImageIO.read(inputFile);
            final var mediaFormat = MediaType.parse(new Tika().detect(inputFile));
            return new ImageData(mediaFormat, bufferedImage, 0);

        } catch (IOException e) {
           throw new RuntimeException("Could not read the file");
        }
    }

    public void store(String fileName, ImageData data) {

        Path filePath = storagePath.resolve(fileName);

        File outputFile = filePath.toFile();

        final var foldersCreated = outputFile.mkdirs();
        log.debug("Storing {}, folders created: {}", fileName, foldersCreated);

        try {
            ImageIO.write(data.image(), data.format().getSubtype(), outputFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not write the file");
        }
    }

}
