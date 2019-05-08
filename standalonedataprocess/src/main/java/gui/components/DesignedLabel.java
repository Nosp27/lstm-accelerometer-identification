package gui.components;

import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;

import javax.swing.*;
import java.awt.*;

public class DesignedLabel extends JLabel {
    public DesignedLabel(String s) {
        super(s);
        setForeground(Color.WHITE);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }

    public DesignedLabel() {
        super();
        setForeground(Color.WHITE);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
    }
}
