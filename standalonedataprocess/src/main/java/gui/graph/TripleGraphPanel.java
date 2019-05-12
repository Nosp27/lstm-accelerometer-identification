package gui.graph;

import gui.components.DesignedPanel;
import gui.tabs.loadDataTab.dataLoader.YReader;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

public class TripleGraphPanel extends DesignedPanel {
    private ArrayList<GraphRender> graphRenders;

    public TripleGraphPanel() {
        setLayout(new GridLayout(3, 1, 10, 10));

        graphRenders = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            GraphRender g = GraphRender.getCentralTrimmedGraph(true);
            g.addListeners(this);
            graphRenders.add(g);
            add(g);
        }
    }

    public void setUpFile(File f) {
        ArrayList<Double[]> points = loadGraphData(f);
        reset();
        if (points != null)
            addPoints(points);
    }

    public void setUpFiles(File dir) {
        reset();
        if(!dir.exists())
            return;
        ArrayList<Double[]> points = new ArrayList<>();
        for (File f : dir.listFiles())
            if (f.exists())
                points.addAll(loadGraphData(f));
        if (!points.isEmpty())
            addPoints(points);
    }

    private ArrayList<Double[]> loadGraphData(File f) {
        try {
            YReader yr = new YReader(f);
            return new ArrayList<>(yr.readAll());
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    private void addPoints(Collection<Double[]> points) {
        for (Double[] p : points) {
            for (int i = 0; i < graphRenders.size(); i++)
                graphRenders.get(i).addPoint(p[i]);
        }
    }

    private void reset() {
        for (GraphRender g : graphRenders)
            g.reset();
    }
}
