package gui.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DesignedPanel extends JPanel {
    private BufferedImage img;
    public DesignedPanel(Color bg){
        super();
        setBackground(bg);
    }

    public DesignedPanel(String imgPath) {
        super();
        try {
            img = ImageIO.read(new File(imgPath));
        } catch (IOException e) {
            img = null;
            setBackground(Color.DARK_GRAY);
        }
    }

    public DesignedPanel(){
        super();
        setBackground(Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if(img == null)
            return;

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int w = screen.width;
        int h = screen.height;

        g.drawImage(img, 0, 0, w, h, null);
    }
}
