package gui;

import configWork.ConfigManager;
import gui.tabs.loadDataTab.DataLoadTab;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class DesignControl {
    public static void setBackgroundImage(JFrame frame){
        BufferedImage img;
        try {
            img = ImageIO.read(new File(ConfigManager.loadProperty("background-image")));
            frame.setContentPane(new JPanel(new BorderLayout()) {
                @Override
                protected void paintComponent(Graphics g) {
                    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

                    int w = screen.width;
                    int h = screen.height;

                    g.drawImage(img, 0, 0, w, h, null);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setTransparent(JComponent p) {
        System.out.println(p.getName() + " " + p.isOpaque());
        p.setBackground(new Color(0, 0, 0, 0));
    }
}
