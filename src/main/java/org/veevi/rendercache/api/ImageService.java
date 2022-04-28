package org.veevi.rendercache.api;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
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
import java.io.InputStream;
import java.net.URL;

@Component
@Slf4j
public class ImageService {

    private final Tika tika = new Tika();

    public ImageData fitWidth(String imageUrl, int width) {
        try {
            final var data = loadImage(imageUrl);

            final var preferredHeight = (int) Math.ceil(width * data.ratio());
            return data.updateImage(scaleImage(data.image(), preferredHeight, width, data.rotation()));
        } catch (Exception e) {
            log.error("Could not scale image {} to width {}", imageUrl, width, e);
        }
        return null;
    }

    public ImageData fitHeight(String imageUrl, int height) {

        try {
            final var data = loadImage(imageUrl);

            final var preferredWidth = (int) Math.ceil(height * data.ratio());

            return data.updateImage(scaleImage(data.image(), height, preferredWidth, data.rotation()));
        } catch (Exception e) {
            log.error("Could not scale image {} to height {}", imageUrl, height, e);
        }
        return null;
    }

    private BufferedImage scaleImage(BufferedImage data, int height, int preferredWidth, int rotation) {

        int actualWidth = rotation == 270 || rotation == 90 ? height : preferredWidth;
        int actualHeight = rotation == 270 || rotation == 90 ? preferredWidth : height;

        BufferedImage bufferedImage = new BufferedImage(actualWidth, actualHeight, data.getType());
        log.info("Rotation: {}", rotation);
        Graphics2D graphics = bufferedImage.createGraphics();
        graphics.rotate(Math.toRadians(rotation), preferredWidth / 2f, height / 2f);


        graphics.drawImage(data, 0, 0, actualWidth, actualHeight, null);
        graphics.dispose();
        return bufferedImage;
    }


    public ImageData loadImage(String imageUrl) throws IOException, ImageProcessingException, MetadataException {
        final var imageStream = new URL(imageUrl).openStream();

        int orientation = getOrientation(new URL(imageUrl).openStream());

        final var imageBytes = IOUtils.toByteArray(imageStream);

        final var format = MediaType.parse(tika.detect(imageBytes));
        final var image = ImageIO.read(new ByteArrayInputStream(imageBytes));


        return new ImageData(format, image, orientation);
    }

    private int getOrientation(InputStream imageStream) throws ImageProcessingException, IOException, MetadataException {
        Metadata metadata = ImageMetadataReader.readMetadata(imageStream);
        ExifIFD0Directory exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
        int orientation = exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION);

        return switch (orientation) {
            case 1 -> // [Exif IFD0] Orientation - Top, left side (Horizontal / normal)
                    0;
            case 6 -> // [Exif IFD0] Orientation - Right side, top (Rotate 90 CW)
                    90;
            case 3 -> // [Exif IFD0] Orientation - Bottom, right side (Rotate 180)
                    180;
            case 8 -> // [Exif IFD0] Orientation - Left side, bottom (Rotate 270 CW)
                    270;
            default -> 0;
        };

    }


}
