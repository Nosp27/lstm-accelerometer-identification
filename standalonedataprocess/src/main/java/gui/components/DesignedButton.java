package gui.components;

import javax.swing.*;
import java.awt.*;

public class DesignedButton extends JButton {
    public DesignedButton(String text, Runnable r){
        this(text);
        addActionListener(e->r.run());
    }

    public DesignedButton(String text){
        super(text);
        setBackground(Color.DARK_GRAY);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        setForeground(Color.WHITE);
        setFocusPainted(false);
    }

    public DesignedButton(){
        super();
        setBackground(Color.DARK_GRAY);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        setForeground(Color.WHITE);
        setFocusPainted(false);
    }
}
