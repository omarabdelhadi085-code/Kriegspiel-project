package ru.vsu.cs.p_p_v.kriegspiel.sdk.cache;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class ImageFileCached {
    private static final Map<String, Image> cache = new ConcurrentHashMap<>();

    public static Image readImage(String path) {
        // Ensure path starts with / for JAR loading
        path = path.replace("\\", "/"); // Force forward slashes for JAR compatibility
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        // Debug print
        System.out.println("Trying to load: " + path);

        Image cachedImage = cache.get(path);
        if (cachedImage != null)
            return cachedImage;

        Image rawImage = null;
        try {
            java.net.URL imageUrl = ImageFileCached.class.getResource(path);
            if (imageUrl == null) {
                String msg = "Failed to load image inside JAR!\nOS: " + System.getProperty("os.name")
                        + "\nRequested Path: '" + path + "'";
                System.err.println(msg);
                javax.swing.JOptionPane.showMessageDialog(null, msg);
                System.exit(1);
            }
            rawImage = ImageIO.read(imageUrl);
            System.out.println("[ASSET_LOAD] Loaded: " + path);
        } catch (IOException e) {
            e.printStackTrace();
            return createErrorImage();
        }

        if (rawImage == null)
            return createErrorImage();

        GraphicsConfiguration gfx_config = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage optimisedImage = gfx_config.createCompatibleImage(
                rawImage.getWidth(null), rawImage.getHeight(null), Transparency.TRANSLUCENT);

        // Draw the old image on the new
        Graphics2D g2d = (Graphics2D) optimisedImage.getGraphics();
        g2d.drawImage(rawImage, 0, 0, null);
        g2d.dispose();

        cache.put(path, optimisedImage);

        return optimisedImage;
    }

    private static Image createErrorImage() {
        BufferedImage error_img = new BufferedImage(10, 10, TYPE_INT_ARGB);
        Graphics2D graphics = error_img.createGraphics();

        graphics.setPaint(new Color(255, 0, 0));
        graphics.fillRect(0, 0, error_img.getWidth(), error_img.getHeight());
        return error_img;
    }
}
