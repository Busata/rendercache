package org.veevi.rendercache.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class RenderCacheEndpoint {

    private final RenderCacheService renderCacheService;

    @GetMapping("/fit_height/{height}")
    public ResponseEntity<byte[]> fitHeight(@RequestParam(name = "url") String imageUrl, @PathVariable int height) throws IOException {
        final var imageData = this.renderCacheService.fitHeight(imageUrl, height);

        return createResponseEntity(imageData);
    }

    @GetMapping("/fit_width/{width}")
    public ResponseEntity<byte[]> fitWidth(@RequestParam(name = "url") String imageUrl, @PathVariable int width) throws IOException {
        final var imageData = this.renderCacheService.fitWidth(imageUrl, width);

        return createResponseEntity(imageData);
    }

    private ResponseEntity<byte[]> createResponseEntity(ImageData imageData) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(imageData.image(), imageData.format().getSubtype(), bos);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.valueOf(imageData.format().toString()))
                .contentLength(bos.size())
                .body(bos.toByteArray());
    }
}
