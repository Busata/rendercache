package org.veevi.rendercache.api;

import org.apache.tika.mime.MediaType;

import java.awt.image.BufferedImage;

public record ImageData(
        MediaType format,
        BufferedImage image
){

    public float ratio() {
        return (float) image.getWidth() / (float) image.getHeight();
    }

    public ImageData updateImage(BufferedImage updatedImage) {
        return new ImageData(format, updatedImage);
    }
}
