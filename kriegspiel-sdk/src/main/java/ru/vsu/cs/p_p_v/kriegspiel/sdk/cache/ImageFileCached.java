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
    
    // Enable debug logging via system property: -Dkriegspiel.debug.resources=true
    private static final boolean DEBUG_RESOURCES = 
        Boolean.parseBoolean(System.getProperty("kriegspiel.debug.resources", "false"));

    public static Image readImage(String path) {
        // Normalize path: ensure forward slashes and leading slash for classpath resources
        path = path.replace("\\", "/"); // Force forward slashes for JAR compatibility
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        // Use normalized path as cache key (path is already normalized above)
        Image cachedImage = cache.get(path);
        if (cachedImage != null)
            return cachedImage;

        Image rawImage = null;
        try {
            // Use context classloader first (most reliable for application code)
            // Falls back to system classloader, then class's classloader
            // This ensures resources are found regardless of module/classloader structure
            java.net.URL imageUrl = null;
            String loaderUsed = null;
            
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            if (contextClassLoader != null) {
                imageUrl = contextClassLoader.getResource(path.substring(1)); // Remove leading /
                if (imageUrl != null) loaderUsed = "context";
            }
            
            if (imageUrl == null) {
                ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                if (systemClassLoader != null) {
                    imageUrl = systemClassLoader.getResource(path.substring(1));
                    if (imageUrl != null) loaderUsed = "system";
                }
            }
            
            if (imageUrl == null) {
                // Fallback to class-relative loading (for backwards compatibility)
                imageUrl = ImageFileCached.class.getResource(path);
                if (imageUrl != null) loaderUsed = "class-relative";
            }
            
            if (DEBUG_RESOURCES && imageUrl != null) {
                System.out.println("[RESOURCE_DEBUG] Loaded: " + path + " via " + loaderUsed + " classloader");
            }
            
            if (imageUrl == null) {
                String msg = "Failed to load image resource!\nOS: " + System.getProperty("os.name")
                        + "\nRequested Path: '" + path + "'\n"
                        + "This indicates the resource is not on the classpath.\n"
                        + "Ensure images are in src/main/resources and properly packaged.\n"
                        + "Enable debug logging with: -Dkriegspiel.debug.resources=true";
                System.err.println(msg);
                javax.swing.JOptionPane.showMessageDialog(null, msg);
                return createErrorImage();
            }
            
            rawImage = ImageIO.read(imageUrl);
        } catch (IOException e) {
            System.err.println("IOException while loading image: " + path);
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
