package gui.graph;

import configWork.ConfigManager;
import gui.components.DesignedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.Locale;
import java.util.Vector;

public class GraphRender extends JPanel {
    private Vector<Double> ys = new Vector<>();
    private float interval = 6;
    private int firstPoint = 0;

    private int offsetX = 60;
    private int offsetY = 10;
    private int storedX = 0;

    private double zoomStep = 10;
    private int intervalOverride = 0;

    private Color lineColor = Color.WHITE;

    //max physical deviation
    private Double maxDeviation = 250d;

    //starting point for drawing
    private double trim = .5;
    private boolean suppressListening;

    public static GraphRender getCentralTrimmedGraph(boolean noMove){
        GraphRender gr = new GraphRender(noMove);
        gr.trim = .5;
        return gr;
    }

    public static GraphRender getBottomTrimmedGraph(){
        GraphRender gr = new GraphRender(false);
        gr.trim = 1;
        gr.maxDeviation = 1d;
        gr.zoomStep = .1;
        gr.intervalOverride = 1;
        return gr;
    }

    public GraphRender(boolean suppressListening) {
        this.suppressListening = suppressListening;
        setFocusable(!suppressListening);
        setBackground(Color.darkGray);
        setOpaque(true);
        addListeners(this);
    }

    public synchronized void addPoint(Double ds) {
        ys.add(ds);
        repaint();
    }

    public synchronized void shiftRight(int x) {
        if (firstPoint + x > 0) {
            if (firstPoint + x <= ys.size() - getWidth() / interval)
                firstPoint += x;
            else firstPoint = (int) (ys.size() - getWidth() / interval);
        } else firstPoint = 0;
        repaint();
    }

    public void changeInterval(int delta) {
        float _delta = delta / 3f;
        if (interval + _delta < 80 && interval + _delta >= 1)
            interval += _delta;
    }

    public void addListeners(JComponent jc) {
        if(suppressListening && jc == this)
            return;

        MouseAdapter ma = new MouseAdapter() {
            boolean isPressed;
            Point prevPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                prevPoint = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getModifiersEx() != MouseEvent.BUTTON1_DOWN_MASK)
                    return;

                Point currPoint = e.getPoint();
                int deltaX = currPoint.x - prevPoint.x;
                int deltaY = deltaX == 0 ? currPoint.y - prevPoint.y : 0;
                prevPoint = currPoint;
                shiftRight(-deltaX);
                changeInterval(deltaY);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int delta = e.getUnitsToScroll();
                System.out.println(delta);
                zoom((int) Math.signum(delta));
            }
        };
        jc.addMouseMotionListener(ma);
        jc.addMouseListener(ma);
        jc.addMouseWheelListener(ma);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //setBackground
        GradientPaint gp = new GradientPaint(0, 0, DesignedPanel.PRIMARY_LIGHT,
                getWidth(), getHeight(), DesignedPanel.PRIMARY);
        ((Graphics2D) g).setPaint(gp);
        g.fillRect(0, 0, getWidth(), getHeight());

        Dimension screen = this.getSize();

        int left = offsetX,
                up = offsetY,
                right = screen.width - offsetX,
                down = screen.height - offsetY,
                startPointY = Math.max((int)(screen.height * trim) - offsetY, 0);

        int height = down - up;


        //draw axes
        ((Graphics2D) g).setPaint(Color.WHITE);//set white color for axes

        g.drawLine(left, down, left, up);
        g.drawLine(left, down, right, down);

        if (ys.size() < firstPoint + 2)
            return;

        if (firstPoint < 0)
            return;

        drawDashes(g, left, down, up);


            for (int i = firstPoint + 1; i < ys.size(); ++i) {
                int currentX = left + (int) interval * (i - firstPoint);

                int y0 = y(i - 1, height);
                int y1 = y(i, height);

                g.setColor(lineColor);
                g.drawLine(currentX, startPointY - y0, currentX + (int) interval, startPointY - y1);
            }
            if (storedX > 1) {
                int _oval_y = y(storedX - 1, height);

                g.setColor(lineColor);
                g.drawOval(left + storedX * (int) interval - 5, startPointY - _oval_y - 5, 10, 10);
            }
    }

    private void drawDashes(Graphics g, int left, int down, int up) {
        //horizontal axis
        double delay = Integer.parseInt(ConfigManager.loadProperty("fixed-time-interval")) * 1f / 1e+3d;
        double overallTime = ys.size() * delay;

        double spanSec = .5;
        while ((int) (spanSec / delay * interval) < 60)
            spanSec *= 2;

        int simpleCounter = 0;
        for (double i = 0; i < overallTime; i += spanSec, simpleCounter++) {
            int _x = left + (int) interval * ((int) (i / delay) - firstPoint);
            double _v = intervalOverride > 0 ? intervalOverride * (_x - left)/interval : i;
            g.drawLine(_x, down - 5, _x, down + 5);
            g.drawString(String.format(Locale.ENGLISH, "%.1f", _v), _x, down - 10);
        }

        //vertical axis
        int numOfDashes = 5;
        double stepPhysical = maxDeviation / numOfDashes;
        int stepPixel = (int) ((down - up) / maxDeviation * stepPhysical);

        double deviation = maxDeviation * trim;
        for (int y = up; y < down; deviation -= stepPhysical, y += stepPixel) {
            g.drawLine(left - 5, y, left + 5, y);
            g.drawString(String.format("%.2f", deviation), 3, y);
        }
    }

    public int y(int i, int height) {
        return (int) (height * ys.get(i) / maxDeviation);
    }

    public void zoom(int n) {
        if (maxDeviation + zoomStep * n > 1)
            maxDeviation += zoomStep * n;
        repaint();
    }

    public void reset(){
        ys.clear();
        repaint();
    }
}