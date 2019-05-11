package gui.components;

import gui.DataPrepareWnd;
import javafx.scene.layout.Background;
import sun.security.krb5.internal.crypto.Des;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DesignedPanel extends JPanel {
    public static final String BG = "/bg.jpg";
    public static final String BG2 = "/bg2.jpg";
    private Image img;

    public DesignedPanel(Color bg) {
        super();
        setBackground(bg);
    }

    public DesignedPanel(String bgPath) {
        super();
        this.img = getBackgroundImage(bgPath);
    }

    public DesignedPanel() {
        super();
        setBackground(Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (img == null)
            return;

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int w = screen.width;
        int h = screen.height;

        g.drawImage(img, 0, 0, w, h, null);
    }

    public static Image getBackgroundImage(String path){
        Image img;
        try {
            img = ImageIO.read(DesignedPanel.class.getResourceAsStream(path));
            return img;
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
