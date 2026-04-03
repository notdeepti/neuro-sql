package com.neurosql.view;

import com.neurosql.model.EEGSignal;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EEGChartPanel extends JPanel {

    private static final int    LANE_HEIGHT  = 90;
    private static final int    LEFT_MARGIN  = 65;
    private static final int    RIGHT_MARGIN = 20;
    private static final int    TOP_PADDING  = 20;
    private static final Color  BG_COLOR     = new Color(15, 15, 25);
    private static final Color  GRID_COLOR   = new Color(40, 40, 60);
    private static final Color  SPIKE_COLOR  = new Color(220, 50, 50, 200);
    private static final Color  LABEL_COLOR  = new Color(180, 180, 200);

    private static final String[] CHANNELS =
            {"Fp1", "Fp2", "T3", "T4", "C3", "C4", "O1", "O2"};

    private static final Color[] CHANNEL_COLORS = {
            new Color(100, 180, 255),
            new Color(80,  210, 160),
            new Color(255, 200,  80),
            new Color(200, 130, 255),
            new Color(100, 220, 100),
            new Color(255, 130, 100),
            new Color(255, 180, 255),
            new Color(130, 210, 255)
    };

    private List<EEGSignal> allSignals;
    private List<EEGSignal> spikes;

    public EEGChartPanel(List<EEGSignal> allSignals,
                         List<EEGSignal> spikes) {
        this.allSignals = allSignals;
        this.spikes     = spikes;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(1200,
                TOP_PADDING + LANE_HEIGHT * CHANNELS.length + 30));
    }

    public void update(List<EEGSignal> allSignals,
                       List<EEGSignal> spikes) {
        this.allSignals = allSignals;
        this.spikes     = spikes;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (allSignals == null || allSignals.isEmpty()) {
            g.setColor(LABEL_COLOR);
            g.setFont(new Font("SansSerif", Font.ITALIC, 14));
            g.drawString("No EEG data loaded. "
                            + "Select a patient and session.", 80,
                    getHeight() / 2);
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        double maxTime = allSignals.stream()
                .mapToDouble(EEGSignal::getTsOffset)
                .max().orElse(1.0);

        Map<String, List<EEGSignal>> byChannel = allSignals.stream()
                .collect(Collectors.groupingBy(
                        EEGSignal::getChannelName));

        for (int i = 0; i < CHANNELS.length; i++) {
            int laneY = TOP_PADDING + i * LANE_HEIGHT;
            drawLaneBackground(g2, laneY, i);
            drawWaveform(g2,
                    byChannel.getOrDefault(CHANNELS[i], List.of()),
                    laneY, maxTime, CHANNEL_COLORS[i], CHANNELS[i]);
        }

        if (spikes != null && !spikes.isEmpty()) {
            drawSpikeOverlay(g2, maxTime);
        }

        drawTimeAxis(g2, maxTime);
    }

    private void drawLaneBackground(Graphics2D g2,
                                    int laneY, int index) {
        g2.setColor(index % 2 == 0
                ? new Color(20, 20, 35)
                : new Color(15, 15, 25));
        g2.fillRect(LEFT_MARGIN, laneY,
                getWidth() - LEFT_MARGIN - RIGHT_MARGIN,
                LANE_HEIGHT);
        g2.setColor(GRID_COLOR);
        g2.setStroke(new BasicStroke(0.5f));
        int midY = laneY + LANE_HEIGHT / 2;
        g2.drawLine(LEFT_MARGIN, midY,
                getWidth() - RIGHT_MARGIN, midY);
    }

    private void drawWaveform(Graphics2D g2,
                              List<EEGSignal> data,
                              int laneY, double maxTime,
                              Color color, String label) {
        int w    = getWidth() - LEFT_MARGIN - RIGHT_MARGIN;
        int midY = laneY + LANE_HEIGHT / 2;
        double yScale = (LANE_HEIGHT * 0.42) / 100.0;

        g2.setColor(color);
        g2.setFont(new Font("Monospaced", Font.BOLD, 11));
        g2.drawString(label, 5, midY + 4);

        if (data.isEmpty()) return;

        int n    = data.size();
        int[] xp = new int[n];
        int[] yp = new int[n];
        for (int j = 0; j < n; j++) {
            xp[j] = LEFT_MARGIN + (int)(
                    data.get(j).getTsOffset() / maxTime * w);
            yp[j] = midY - (int)(
                    data.get(j).getVoltage() * yScale);
        }
        g2.setStroke(new BasicStroke(1.1f,
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        g2.drawPolyline(xp, yp, n);
    }

    private void drawSpikeOverlay(Graphics2D g2, double maxTime) {
        int w = getWidth() - LEFT_MARGIN - RIGHT_MARGIN;
        g2.setColor(SPIKE_COLOR);
        g2.setStroke(new BasicStroke(1.2f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 1f,
                new float[]{5f, 4f}, 0));

        for (EEGSignal spike : spikes) {
            int x = LEFT_MARGIN + (int)(
                    spike.getTsOffset() / maxTime * w);
            g2.drawLine(x, TOP_PADDING,
                    x, TOP_PADDING + LANE_HEIGHT * CHANNELS.length);

            for (int i = 0; i < CHANNELS.length; i++) {
                if (CHANNELS[i].equals(spike.getChannelName())) {
                    int my = TOP_PADDING + i * LANE_HEIGHT + 2;
                    g2.setStroke(new BasicStroke(1f));
                    g2.fillPolygon(
                            new int[]{x - 5, x + 5, x},
                            new int[]{my, my, my + 9}, 3);
                    break;
                }
            }
        }
    }

    private void drawTimeAxis(Graphics2D g2, double maxTime) {
        int y = TOP_PADDING + LANE_HEIGHT * CHANNELS.length + 15;
        int w = getWidth() - LEFT_MARGIN - RIGHT_MARGIN;
        g2.setColor(LABEL_COLOR);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("Time (s)", LEFT_MARGIN, y);
        for (int sec = 0; sec <= (int) maxTime; sec++) {
            int x = LEFT_MARGIN + (int)(sec / maxTime * w);
            g2.drawString(String.valueOf(sec), x - 3, y);
        }
    }
}