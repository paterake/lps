import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TextToImage {
    public static void main(String arg[]) throws IOException {
        String key = "આસ્તા";
        BufferedImage bufferedImage = new BufferedImage(170, 30,
                BufferedImage.TYPE_INT_RGB);
        Graphics graphics = bufferedImage.getGraphics();
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.fillRect(0, 0, 200, 50);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial Black", Font.PLAIN, 48));
        graphics.drawString(key, 10, 25);
        ImageIO.write(bufferedImage, "jpg", new File("image.jpg"));
        System.out.println("Image Created");
    }
}
