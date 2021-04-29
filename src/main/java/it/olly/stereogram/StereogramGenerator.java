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
public class StereogramGenerator {
    static int patternHeight = 200;
    static int patternWidth = 142;

    static boolean[][] pattern = new boolean[patternHeight][patternWidth];

    static boolean[][] getDisparityMap() throws Exception {
        int w = 600;
        int h = 400;
        boolean[][] ret = new boolean[h][w];

        // todo read from image
        BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(Color.white);
        g2d.fillRect(150, 100, 50, 50);
        g2d.fillRect(350, 200, 100, 35);
        g2d.dispose();

        // Save as PNG
        File file = new File("/temp/disparity.png");
        ImageIO.write(bufferedImage, "png", file);

        // compose disparity map. TODO -> integer with a level? 1-5?
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // Getting pixel color by position x and y
                int clr = bufferedImage.getRGB(x, y);
                int red = (clr & 0x00ff0000) >> 16;
                int green = (clr & 0x0000ff00) >> 8;
                int blue = clr & 0x000000ff;
                ret[y][x] = red > 0 || green > 0 || blue > 0;
            }
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        // generate binary rnd pattern black/white
        for (int y = 0; y < patternHeight; y++) {
            for (int x = 0; x < patternWidth; x++) {
                pattern[y][x] = Math.random() < 0.5;
            }
        }

        // get the disparity map. TODO load a gray-scale image
        boolean[][] disparityMap = getDisparityMap();

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(disparityMap[0].length, disparityMap.length, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // write pattern n times to image
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                // write pattern pixel
                int xOnPattern = x % patternWidth;
                int yOnPattern = y % patternHeight;
                if (pattern[yOnPattern][xOnPattern]) {
                    g2d.setColor(Color.white);
                } else {
                    g2d.setColor(Color.black);
                }
                g2d.fillRect(x, y, 1, 1);
            }
        }

        // shift pixels if needed
        for (int x = 0; x < bufferedImage.getWidth(); x++) {
            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                if (disparityMap[y][x]) {
                    // write pattern pixel
                    int xOnPattern = x % patternWidth;
                    int yOnPattern = y % patternHeight;
                    if (pattern[yOnPattern][xOnPattern]) {
                        g2d.setColor(Color.white);
                    } else {
                        g2d.setColor(Color.black);
                    }

                    int shift = 2;
                    // check boundaries (if x+shift>=width, do nothing)
                    g2d.fillRect(x + shift, y, 1, 1);
                }
            }
        }

        // Disposes of this graphics context and releases any system resources that it is using.
        g2d.dispose();

        // Save as PNG
        File file = new File("/temp/myimage.png");
        ImageIO.write(bufferedImage, "png", file);
    }

}
