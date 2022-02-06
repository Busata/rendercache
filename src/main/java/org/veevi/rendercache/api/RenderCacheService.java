package org.veevi.rendercache.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Slf4j
@RequiredArgsConstructor
public class RenderCacheService {
    private final StorageService storageService;
    private final ImageService imageService;
    private final ImageFileNameFactory imageFileNameFactory;


    public ImageData fitHeight(String imageUrl, int height) {
        return this.loadOrCreate(
                String.format("fitHeight#%d", height),
                imageUrl,
                () -> this.imageService.fitHeight(imageUrl, height)
        );
    }

    public ImageData fitWidth(String imageUrl, int width) {
        return this.loadOrCreate(
                String.format("fitWidth#%d", width),
                imageUrl
                , () -> this.imageService.fitWidth(imageUrl, width));
    }

    private ImageData loadOrCreate(String key, String url, Supplier<ImageData> loader) {
        final var fileName = this.createFileName(key, url);
        if (this.storageService.exists(fileName)) {
            log.info("Loading file from cache: {}", fileName);
            return this.storageService.load(fileName);
        } else {
            log.info("Creating and storing image to cache: {}", fileName);
            final var data = loader.get();
            this.storageService.store(fileName, data);
            return data;
        }
    }


    private String createFileName(String key, String url) {
        return imageFileNameFactory.createFilename(key, url);
    }
}