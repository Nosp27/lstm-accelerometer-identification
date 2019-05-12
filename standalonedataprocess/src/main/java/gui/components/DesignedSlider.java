package gui.components;

import javax.swing.*;
import java.awt.*;

public class DesignedSlider extends JSlider {
    public DesignedSlider(int min, int max, int val){
        super(min, max, val);
        setBackground(DesignedPanel.PRIMARY);
    }
}
