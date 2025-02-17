package components.ui;

import components.utility.ResizeImage;
import components.utility.Sound;
import interfaces.Drawable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Intro implements Drawable {
    private static BufferedImage image = ResizeImage.getResizedImage("/Intro.png", 140);

    public JLabel introLabel = new JLabel();

    public Sound overworld = new Sound("/overworld.wav");

    public Intro() {
        introLabel.setBounds((WINDOW_WIDTH - image.getWidth()) / 2,
                (WINDOW_HEIGHT - image.getHeight()) / 2 - 50,
                image.getWidth(), image.getHeight());

        overworld.playInLoop(); // play the intro music
    }

    /**
     * Draws an image on the graphics context at the center of the window.
     *
     * @param g the graphics context to draw on
     */
    @Override
    public void draw(Graphics g) {
        g.drawImage(image, (WINDOW_WIDTH - image.getWidth()) / 2, (WINDOW_HEIGHT - image.getHeight()) / 2 - 50, null);
    }

    /**
     * Unimplemented methods from @interface.Drawable
     * 
     * @see components.Drawable#update()
     * @see components.Drawable#reset()
     */
    @Override
    public void update() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'reset'");
    }
}
