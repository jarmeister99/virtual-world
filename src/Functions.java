import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

import processing.core.PImage;
import processing.core.PApplet;

final class Functions {

    private static final int COLOR_MASK = 0xffffff;


    // UNMOVED

    public static PImage getCurrentImage(Object entity) {
        if (entity instanceof Background) {
            return ((Background) entity).images
                    .get(((Background) entity).imageIndex);
        } else if (entity instanceof Entity) {
            return ((Entity) entity).images.get(((Entity) entity).imageIndex);
        } else {
            throw new UnsupportedOperationException(
                    String.format("getCurrentImage not supported for %s",
                            entity));
        }
    }

    /*
      Called with color for which alpha should be set and alpha value.
      setAlpha(img, color(255, 255, 255), 0));
    */
    public static void setAlpha(PImage img, int maskColor, int alpha) {
        int alphaValue = alpha << 24;
        int nonAlpha = maskColor & COLOR_MASK;
        img.format = PApplet.ARGB;
        img.loadPixels();
        for (int i = 0; i < img.pixels.length; i++) {
            if ((img.pixels[i] & COLOR_MASK) == nonAlpha) {
                img.pixels[i] = alphaValue | nonAlpha;
            }
        }
        img.updatePixels();
    }


    public static int clamp(int value, int low, int high) {
        return Math.min(high, Math.max(value, low));
    }


}
