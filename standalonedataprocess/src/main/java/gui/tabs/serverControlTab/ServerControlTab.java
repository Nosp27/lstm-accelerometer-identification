package gui.tabs.serverControlTab;

import gui.DesignControl;
import server.ServerWriter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class ServerControlTab extends JPanel {
    private JLabel serverInfo;
    private JLabel clientInfo;
    private JLabel dataTransferInfo;

    ServerWriter.IListenServer serverDataAccess;

    public ServerControlTab() {
        setName("Remote");
        serverDataAccess = ServerWriter.createServer();

        DesignControl.setTransparent(this);
        setLayout(new GridLayout(2, 2, 30, 30));

        serverInfo = createLabel(Color.DARK_GRAY, 15, Color.GREEN, this);

        clientInfo = createLabel(Color.DARK_GRAY, 15, Color.WHITE, this);

        dataTransferInfo = createLabel(Color.DARK_GRAY, 15, Color.WHITE, this);
        dataTransferInfo.setForeground(Color.WHITE);

        startMonitoringData();
    }

    private JLabel createLabel(Color bg, int border, Color c, Container ct) {
        JPanel holder = new JPanel(new SpringLayout());
        holder.setBackground(bg);
        holder.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.white, Color.GRAY));
        holder.getBorder().getBorderInsets(holder).set(border, border, border, border);

        JLabel label;
        label = new JLabel();
        label.setFont(new Font("Monospaced", Font.PLAIN, 13));
        label.setForeground(c);

        holder.add(label, SpringLayout.WEST);
        ct.add(holder);
        return label;
    }

    private void feedData(){

    }

    private void startMonitoringData() {
        new Thread(() -> {
            try {
                while (true) {
                    clientInfo.setText(serverDataAccess.getClientinfo());
                    serverInfo.setText(serverDataAccess.getServerInfo());
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {

            }
        }).start();
    }
}
