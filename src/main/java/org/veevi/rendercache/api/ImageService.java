package org.veevi.rendercache.api;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MediaType;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

@Component
@Slf4j
public class ImageService {

    private final Tika tika = new Tika();

    public ImageData fitWidth(String imageUrl, int width) {
        try {
            final var data = loadImage(imageUrl);

            final var preferredHeight = (int) Math.ceil(width * data.ratio());
            return data.updateImage(scaleImage(data.image(), preferredHeight, width));
        } catch (IOException e) {
            log.error("Could not scale image {} to width {}", imageUrl, width, e);
        }
        return null;
    }

    public ImageData fitHeight(String imageUrl, int height) {

        try {
            final var data = loadImage(imageUrl);

            final var preferredWidth = (int) Math.ceil(height * data.ratio());

            return data.updateImage(scaleImage(data.image(), height, preferredWidth));
        } catch (IOException e) {
            log.error("Could not scale image {} to height {}", imageUrl, height, e);
        }
        return null;
    }

    private BufferedImage scaleImage(BufferedImage data, int height, int preferredWidth) {
        BufferedImage bufferedImage = new BufferedImage(preferredWidth, height, data.getType());
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.drawImage(data, 0, 0, preferredWidth, height, null);
        graphics.dispose();
        return bufferedImage;
    }


    public ImageData loadImage(String imageUrl) throws IOException {
        final var imageStream = new URL(imageUrl).openStream();
        final var imageBytes = IOUtils.toByteArray(imageStream);

        final var format = MediaType.parse(tika.detect(imageBytes));
        final var image = ImageIO.read(new ByteArrayInputStream(imageBytes));


        return new ImageData(format, image);
    }


}
