package gui.components;

import it.unimi.dsi.fastutil.shorts.ShortRBTreeSet;

import javax.swing.*;
import java.awt.*;

public class DesignedLabel extends JLabel {
    public DesignedLabel(String s) {
        super(s);
        setUp();
    }

    public DesignedLabel() {
        super();
        setUp();
    }

    private void setUp(){
        setForeground(Color.WHITE);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        setVerticalAlignment(TOP);
    }
}
