package gui.tabs.serverControlTab;

import accelTest.DataFeeding;
import gui.components.DesignedButton;
import gui.components.DesignedPanel;
import server.ServerWriter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.io.File;
import java.util.Vector;

public class ServerControlTab extends DesignedPanel implements ServerWriter.ICallbackServer, DataFeeding.DataFeedListener {
    private JLabel serverInfo;
    private JLabel clientInfo;
    private DataFeedPanel dataFeedTab;

    private boolean isFeeding = false;


    private ServerWriter.IServerDataAccessor serverDataAccess;
    private DataFeeding dataFeeder = new DataFeeding();

    public ServerControlTab() {
        super(BG);
        setName("Remote");
        dataFeeder.setListener(this);
        serverDataAccess = ServerWriter.createServer(this);
        setLayout(new GridLayout(2, 2, 30, 30));
        serverInfo = createLabel(PRIMARY, 15, Color.WHITE, this);
        clientInfo = createLabel(PRIMARY, 15, Color.WHITE, this);
        dataFeedTab = new DataFeedPanel(this::onFileRecieved);
        add(dataFeedTab);
        dataFeedTab.setFeedMode(getFeedingState());
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

        ct.add(label, SpringLayout.WEST);
//        ct.add(holder);
        return label;
    }

    private void addFeedSwitch() {
        JPanel feedSwitch = new JPanel(new GridLayout(1, 2, 10, 10));
        feedSwitch.setBackground(Color.DARK_GRAY);
        feedSwitch.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.white, Color.GRAY));

        JButton feed = new DesignedButton("Recognition mode");
        JButton train = new DesignedButton("Learning mode");
        train.setBackground(PRIMARY);
        feed.setBackground(PRIMARY);
        train.setEnabled(false);

        feed.addActionListener(e -> feedSwitchActionListener(true, feed, train));
        train.addActionListener(e -> feedSwitchActionListener(false, feed, train));
        feedSwitch.add(feed);
        feedSwitch.add(train);
        add(feedSwitch);
    }

    private void feedSwitchActionListener(boolean _feed, JButton feed, JButton train) {
        synchronized (this) {
            isFeeding = _feed;
            feed.setEnabled(!_feed);
            train.setEnabled(_feed);
        }
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
        if (getFeedingState()) {
            dataFeeder.feedData(f);
        }
    }

    @Override
    public void calculatedResult(Vector<Double> result) {
        dataFeedTab.setResult(result);
        if (result != null)
            serverDataAccess.sendResponse(result.lastElement() > .5f);
    }
}
