package gui.components;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class DesignedPanel extends JPanel {
    public static final String BG = "/bg2.jpeg";
    public static final Color PRIMARY = new Color(0x080f19);
    public static final Color PRIMARY_LIGHT = new Color(0x161f2b);
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
        setBackground(PRIMARY);
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

    private static Image getBackgroundImage(String path){
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
