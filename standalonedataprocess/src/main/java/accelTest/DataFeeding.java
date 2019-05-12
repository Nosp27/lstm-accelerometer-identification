package accelTest;

import configWork.ConfigManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Vector;

public class DataFeeding {
    private volatile LRNN feedingNet;
    private Vector<File> filesToFeed = new Vector<>();
    private Vector<Double> results = new Vector<>();
    private Thread dataFeedThread;
    private DataFeedListener listener;

    public void setListener(DataFeedListener listener) {
        this.listener = listener;
    }

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
            return feedingNet.feedNet(DataPrepare.getFeedableData(dataFile));
        } catch (IOException | NoSuchElementException e) {
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

                        try {
                            results.add(feedDataInternal(filesToFeed.firstElement()));
                            filesToFeed.remove(filesToFeed.firstElement());
                            listener.calculatedResult(results);
                        } catch (IllegalArgumentException e) {
                            listener.calculatedResult(null);
                        }
                    }
                }
            } catch (InterruptedException e) {
            }
        });
    }

    public interface DataFeedListener {
        void calculatedResult(Vector<Double> result);
    }
}
