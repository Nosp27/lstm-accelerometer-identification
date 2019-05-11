package gui.tabs.serverControlTab;

import accelTest.DataFeeding;
import gui.DesignControl;
import gui.components.DesignedButton;
import server.ServerWriter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.io.File;
import java.util.Vector;

public class ServerControlTab extends JPanel implements ServerWriter.ICallbackServer, DataFeeding.DataFeedListener {
    private JLabel serverInfo;
    private JLabel clientInfo;
    private DataFeedTab dataFeedTab;

    private boolean isFeeding = false;


    ServerWriter.IServerDataAccessor serverDataAccess;
    DataFeeding dataFeeder = new DataFeeding();

    public ServerControlTab() {
        setName("Remote");
        dataFeeder.setListener(this);
        serverDataAccess = ServerWriter.createServer(this);

        DesignControl.setTransparent(this);
        setLayout(new GridLayout(2, 2, 30, 30));

        serverInfo = createLabel(Color.DARK_GRAY, 15, Color.GREEN, this);

        clientInfo = createLabel(Color.DARK_GRAY, 15, Color.WHITE, this);

        dataFeedTab = new DataFeedTab(this::onFileRecieved);
        add(dataFeedTab);

        addFeedSwitch();

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

    private void addFeedSwitch() {
        JPanel feedSwitch = new JPanel(new GridLayout(1, 2, 10, 10));
        feedSwitch.setBackground(Color.DARK_GRAY);
        feedSwitch.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.white, Color.GRAY));

        JButton feed = new DesignedButton("Recognition mode");
        JButton train = new DesignedButton("Learning mode");
        train.setEnabled(false);

        feed.addActionListener(e -> feedSwitchActionListener(true, feed, train));
        train.addActionListener(e -> feedSwitchActionListener(false, feed, train));
        feedSwitch.add(feed);
        feedSwitch.add(train);
        add(feedSwitch);
    }

    private void feedSwitchActionListener(boolean _feed, JButton feed, JButton train) {
        isFeeding = _feed;
        feed.setEnabled(!_feed);
        train.setEnabled(_feed);
        dataFeedTab.setFeedMode(_feed);
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

    private synchronized boolean getFeedingState() {
        return isFeeding;
    }

    @Override
    public void onFileRecieved(File f) {
        if (isFeeding) {
            dataFeeder.feedData(f);
        }
    }

    @Override
    public void calculatedResult(Vector<Double> result) {
        String message;
        if (result == null) {
            message = "Error occured during identification process!";
        } else
            message = "Calculated: " + result.lastElement();
        dataFeedTab.setResult(result);
    }
}
