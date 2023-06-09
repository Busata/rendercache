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
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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


        BufferedImage bufferedImage = new BufferedImage(preferredWidth, height, data.getType());
        log.info("Rotation: {}", rotation);
        Graphics2D graphics = bufferedImage.createGraphics();


        graphics.drawImage(data, 0, 0, preferredWidth, height, null);
        graphics.dispose();
        return rotate(bufferedImage, rotation);
    }

    public static BufferedImage rotate(BufferedImage bimg, double angle) {

        final double rads = Math.toRadians(angle);
        final double sin = Math.abs(Math.sin(rads));
        final double cos = Math.abs(Math.cos(rads));
        final int w = (int) Math.floor(bimg.getWidth() * cos + bimg.getHeight() * sin);
        final int h = (int) Math.floor(bimg.getHeight() * cos + bimg.getWidth() * sin);
        final BufferedImage rotatedImage = new BufferedImage(w, h, bimg.getType());
        final AffineTransform at = new AffineTransform();
        at.translate(w / 2, h / 2);
        at.rotate(rads,0, 0);
        at.translate(-bimg.getWidth() / 2, -bimg.getHeight() / 2);
        final AffineTransformOp rotateOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        rotateOp.filter(bimg,rotatedImage);
        return rotatedImage;
    }


    public ImageData loadImage(String imageUrl) throws IOException, ImageProcessingException, MetadataException {
        final var imageStream = new URL(imageUrl).openStream();

        int orientation = 0;
        try {
            orientation = getOrientation(new URL(imageUrl).openStream());
        }
        catch(Exception ex) {
            log.error("Error reading orientation", ex);
        }

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
