package com.daicy.devtools.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class IconGeneratorTest {
    private Stage stage;

    @Start
    private void start(Stage stage) {
        this.stage = stage;
    }

    @Test
    void testGenerateIcon() {
        Platform.runLater(() -> {
            Image icon = IconGenerator.generateIcon("Test");
            
            assertNotNull(icon);
            assertEquals(32, (int)icon.getWidth());
            assertEquals(32, (int)icon.getHeight());
            assertFalse(icon.isBackgroundLoading());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testGenerateIconWithEmptyString() {
        Platform.runLater(() -> {
            assertThrows(StringIndexOutOfBoundsException.class, () -> {
                IconGenerator.generateIcon("");
            });
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testGenerateIconWithNullString() {
        Platform.runLater(() -> {
            assertThrows(NullPointerException.class, () -> {
                IconGenerator.generateIcon(null);
            });
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testGenerateIconWithLongString() {
        Platform.runLater(() -> {
            Image icon = IconGenerator.generateIcon("LongTestString");
            
            assertNotNull(icon);
            assertEquals(32, (int)icon.getWidth());
            assertEquals(32, (int)icon.getHeight());
            
            // Check for presence of white pixels in a larger center region
            PixelReader reader = icon.getPixelReader();
            boolean foundWhitePixel = hasWhitePixelInRegion(reader, 10, 22, 10, 22);
            assertTrue(foundWhitePixel, "Should find at least one white pixel in center region (text)");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testGenerateIconWithSpecialCharacters() {
        Platform.runLater(() -> {
            Image icon = IconGenerator.generateIcon("@#$%");
            
            assertNotNull(icon);
            assertEquals(32, (int)icon.getWidth());
            assertEquals(32, (int)icon.getHeight());
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testIconVisualProperties() {
        Platform.runLater(() -> {
            Image icon = IconGenerator.generateIcon("T");
            
            PixelReader reader = icon.getPixelReader();
            
            // Test background color (should be #4A90E2)
            Color bgColor = reader.getColor(4, 16); // Check a pixel that should definitely be background
            Color expectedBgColor = Color.web("#4A90E2");
            assertTrue(colorEquals(bgColor, expectedBgColor), 
                "Background color should be #4A90E2");
            
            // Test for presence of white text in a larger region
            boolean foundWhitePixel = hasWhitePixelInRegion(reader, 10, 22, 10, 22);
            assertTrue(foundWhitePixel, "Should find at least one white pixel in center region (text)");
            
            // Test corner transparency (due to corner radius)
            Color cornerColor = reader.getColor(0, 0);
            assertEquals(0, cornerColor.getOpacity(),
                "Corner should be transparent due to corner radius");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void testIconCornerRadius() {
        Platform.runLater(() -> {
            Image icon = IconGenerator.generateIcon("T");
            PixelReader reader = icon.getPixelReader();
            
            // Check corners (should be transparent due to corner radius)
            Color topLeft = reader.getColor(0, 0);
            Color topRight = reader.getColor(31, 0);
            Color bottomLeft = reader.getColor(0, 31);
            Color bottomRight = reader.getColor(31, 31);
            
            assertEquals(0, topLeft.getOpacity(), "Top-left corner should be transparent");
            assertEquals(0, topRight.getOpacity(), "Top-right corner should be transparent");
            assertEquals(0, bottomLeft.getOpacity(), "Bottom-left corner should be transparent");
            assertEquals(0, bottomRight.getOpacity(), "Bottom-right corner should be transparent");
        });
        WaitForAsyncUtils.waitForFxEvents();
    }

    // Helper method to compare colors with tolerance
    private boolean colorEquals(Color c1, Color c2) {
        double tolerance = 0.1;
        return Math.abs(c1.getRed() - c2.getRed()) < tolerance &&
               Math.abs(c1.getGreen() - c2.getGreen()) < tolerance &&
               Math.abs(c1.getBlue() - c2.getBlue()) < tolerance &&
               Math.abs(c1.getOpacity() - c2.getOpacity()) < tolerance;
    }

    private boolean hasWhitePixelInRegion(PixelReader reader, int startX, int endX, int startY, int endY) {
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                Color pixelColor = reader.getColor(x, y);
                // More lenient white check - any very bright pixel
                if (pixelColor.getRed() > 0.9 && pixelColor.getGreen() > 0.9 && pixelColor.getBlue() > 0.9) {
                    return true;
                }
            }
        }
        return false;
    }
} 