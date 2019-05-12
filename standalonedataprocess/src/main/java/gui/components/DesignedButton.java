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
        setBackground(DesignedPanel.PRIMARY);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        setForeground(Color.WHITE);
        setFocusPainted(false);
    }

    public DesignedButton(){
        super();
        setBackground(DesignedPanel.PRIMARY);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        setForeground(Color.WHITE);
        setFocusPainted(false);
    }
}
