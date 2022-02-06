package org.veevi.rendercache.api;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.apache.commons.codec.binary.Hex.encodeHexString;

@Component
public class ImageFileNameFactory {

    public String createFilename(String key, String imageUrl) {
        String hash = createHash(key + "#" + imageUrl);
        return String.format("%s.%s", hash, getFileExtension(imageUrl));
    }

    @SneakyThrows
    private String createHash(String key) {
        MessageDigest sha256 = MessageDigest.getInstance("SHA256");
        byte[] digest = sha256.digest(key.getBytes(StandardCharsets.UTF_8));
        return encodeHexString(digest);
    }


    private String getFileExtension(String url) {
        return url.substring(url.lastIndexOf('.') + 1);
    }
}
