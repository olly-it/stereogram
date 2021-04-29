package it.olly.stereogram;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Thanks to "Stereogram Tutorial" by Julio M. Otuyama (https://www.ime.usp.br/~otuyama/stereogram/basic/index.html)
 * 
 * @author alessio olivieri
 *
 */
public class StereogramGenerator2 {
    static int patternWidth = 142;

    static int[][] getDisparityMap() throws Exception {
        int w = 500;
        int h = 150;
        int[][] ret = new int[w][h];

        // todo read from image
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(Color.white);
        g2d.fillRect(50, 50, 50, 50);
        g2d.fillOval(175, 25, 100, 100);
        g2d.fillRect(350, 100, 100, 35);
        g2d.dispose();

        // Save as PNG
        File file = new File("/temp/disparity.png");
        ImageIO.write(bufferedImage, "png", file);

        // compose disparity map. TODO -> integer with a level? 1-5?
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // Getting pixel color by position x and y
                int clr = bufferedImage.getRGB(x, y);
                int red = (clr & 0x00ff0000) >> 16;
                int green = (clr & 0x0000ff00) >> 8;
                int blue = clr & 0x000000ff;
                ret[x][y] = (red > 0 || green > 0 || blue > 0) ? 1 : 0;
            }
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        // disparityMap and pattern array has to be considered with [col][row] (like x,y)

        // get the disparity map. TODO load a gray-scale image
        int[][] disparityMap = getDisparityMap();
        int disparityWidth = disparityMap.length;
        int disparityHeight = disparityMap[0].length;

        int totSteps = 1 + (disparityWidth / patternWidth);

        int[][] pattern = new int[patternWidth][disparityHeight];
        // generate binary rnd pattern black/white
        for (int x = 0; x < patternWidth; x++) {
            for (int y = 0; y < disparityHeight; y++) {
                pattern[x][y] = (Math.random() < 0.5) ? 1 : 0;
            }
        }

        // Constructs a BufferedImage of one of the predefined image types.
        // the image created will have size = (n*pattern.width, disparitymap.height)
        BufferedImage bufferedImage = new BufferedImage(patternWidth
                * (totSteps + 1), disparityHeight, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // write pattern for the 1st column (with width = pattern's width) that will be the base for the next steps
        for (int x = 0; x < patternWidth; x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                // write pattern pixel
                if (pattern[x][y] > 0) {
                    g2d.setColor(Color.white);
                } else {
                    g2d.setColor(Color.black);
                }
                g2d.fillRect(x, y, 1, 1);
            }
        }

        // starts the process. every step will be the size of patternWidth
        for (int step = 1; step <= totSteps; step++) {
            // write the previous pattern as my step's background
            for (int x = 0; x < patternWidth; x++) {
                for (int y = 0; y < bufferedImage.getHeight(); y++) {
                    int rgb = bufferedImage.getRGB(realX(x, step - 1), y);
                    g2d.setColor(new Color(rgb));
                    g2d.fillRect(realX(x, step), y, 1, 1);
                }
            }

            // write shifted things
            for (int x = 0; x < patternWidth; x++) {
                for (int y = 0; y < bufferedImage.getHeight(); y++) {
                    int xOnDisparityMap = realX(x, step) - patternWidth;
                    if (xOnDisparityMap < disparityWidth && disparityMap[xOnDisparityMap][y] > 0) {
                        int rgb = bufferedImage.getRGB(realX(x, step), y);
                        // TODO check boundaries
                        g2d.setColor(new Color(rgb));
                        g2d.fillRect(realX(x, step) - 2, y, 1, 1);
                    }
                }
            }
        }

        // Disposes of this graphics context and releases any system resources that it is using.
        g2d.dispose();

        // Save as PNG
        File file = new File("/temp/myimage.png");
        ImageIO.write(bufferedImage, "png", file);
    }

    private static int realX(int x, int step) {
        return step * patternWidth + x;
    }
}
