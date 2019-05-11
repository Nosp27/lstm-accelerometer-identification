package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class DesignControl {
    public static void setTransparent(JComponent p) {
        System.out.println(p.getName() + " " + p.isOpaque());
        p.setBackground(new Color(0, 0, 0, 0));
    }
}
