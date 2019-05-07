package gui.graph;

import configWork.ConfigManager;
import gui.DesignControl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Collection;
import java.util.Vector;

public class GraphRender extends JPanel {
    private Vector<Double[]> ys = new Vector<>();
    private float interval = 6;
    private int firstPoint = 0;

    int offsetX = 10;
    int offsetY = 10;

    int storedX = 0;

    //max physical deviation
    private Double maxDeviation = 250d;

    public GraphRender() {
        setBackground(Color.darkGray);
        setOpaque(true);
        addListeners();
    }

    public synchronized void addPoints(Collection<Double[]> ds) {
        ys.addAll(ds);
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

    private void addListeners() {
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
        addMouseMotionListener(ma);
        addMouseListener(ma);
        addMouseWheelListener(ma);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //setBackground
        GradientPaint gp = new GradientPaint(0, 0, Color.black,
                getWidth(), getHeight(), Color.DARK_GRAY);
        ((Graphics2D) g).setPaint(gp);
        g.fillRect(0, 0, getWidth(), getHeight());

        Dimension screen = this.getSize();

        int left = offsetX,
                up = offsetY,
                right = screen.width - offsetX,
                down = screen.height - offsetY,
                center = screen.height / 2 - offsetX;

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

        for (int k = 0; k < 3; ++k) {
            for (int i = firstPoint + 1; i < ys.size(); ++i) {
                int currentX = left + (int) interval * (i - firstPoint);

                int y0 = y(i - 1, k, height);
                int y1 = y(i, k, height);

                //System.out.println(String.format("(%d; %d) -> (%d; %d)", currentX, down - y0, currentX, down - y1));
                g.setColor(getColor(k));
                g.drawLine(currentX, center - y0, currentX + (int) interval, center - y1);
            }
            if (storedX > 1) {
                int _oval_y = y(storedX - 1, k, height);

                g.setColor(getColor(k));
                g.drawOval(left + storedX * (int) interval - 5, center - _oval_y - 5, 10, 10);
            }

        }
    }

    private void drawDashes(Graphics g, int left, int down, int up) {
        //horizontal axis
        double delay = Integer.parseInt(ConfigManager.loadProperty("fixed-time-interval")) * 1f / 1e+3d;
        double overallTime = ys.size() * delay;

        double spanSec = .5;
        while ((int) (spanSec / delay * interval) < 60)
            spanSec *= 2;

        for (double i = 0; i < overallTime; i += spanSec) {
            int _x = left + (int) interval * ((int) (i / delay) - firstPoint);
            g.drawLine(_x, down - 5, _x, down + 5);
            g.drawString(String.format("%.1f", i), _x, down - 10);
        }

        //vertical axis
        int numOfDashes = 5;
        double stepPhysical = maxDeviation / numOfDashes;
        int stepPixel = (int) ((down - up) / maxDeviation * stepPhysical);

        double deviation = maxDeviation / 2;
        for (int y = up; y < down; deviation -= stepPhysical, y += stepPixel) {
            g.drawLine(left - 5, y, left + 5, y);
            g.drawString(String.format("%.2f", deviation), left + 10, y);
        }
    }

    public int y(int i, int k, int height) {
        return (int) (height * ys.get(i)[k] / maxDeviation);
    }

    public void zoom(int n) {
        if (maxDeviation + 10 * n > 1)
            maxDeviation += 10 * n;
        repaint();
    }


    private Color getColor(int k) {
        switch (k) {
            case 0:
                return Color.MAGENTA;
            case 1:
                return Color.GREEN;
            case 2:
                return Color.ORANGE;
            default:
                return Color.ORANGE;
        }
    }

    public void clearPoints() {
        ys.clear();
    }
}