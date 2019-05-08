package gui.tabs.serverControlTab;

import accelTest.DataPrepare_ALT;
import accelTest.LRNN;
import configWork.ConfigManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

public class DataFeeding {
    volatile LRNN feedingNet;

    private Vector<File> filesToFeed = new Vector<>();
    private Vector<Double> results = new Vector<>();

    private Thread dataFeedThread;
    private DataFeedListener listener;

    public void feedData(File f) {
        synchronized (this) {
            filesToFeed.add(f);
            if (dataFeedThread == null) {
                dataFeedThread = getDataFeedThread();
                dataFeedThread.start();
            } else if (filesToFeed.size() == 1)
                this.notifyAll();
        }
    }

    public boolean isFeeding() {
        return dataFeedThread.getState() == Thread.State.RUNNABLE;
    }

    private double feedDataInternal(File dataFile) {
        if (feedingNet == null)
            feedingNet = new LRNN();

        try {
            feedingNet.loadNet(new File(ConfigManager.loadProperty("net-out")));

            return feedingNet.feedNet(DataPrepare_ALT.getFeedableData(dataFile));
        } catch (IOException e) {
            throw new IllegalArgumentException("incorrect file " + dataFile.getName());
        }
    }

    private Thread getDataFeedThread() {
        return new Thread(() -> {
            try {
                while (true) {
                    synchronized (DataFeeding.this) {
                        if (filesToFeed.isEmpty())
                            wait();

                        results.add(feedDataInternal(filesToFeed.firstElement()));
                        filesToFeed.remove(filesToFeed.firstElement());
                        listener.calculatedResult(results);
                    }
                }
            } catch (InterruptedException e) {

            }
        });
    }

    public interface DataFeedListener{
        void calculatedResult(Vector<Double> result);
    }
}
