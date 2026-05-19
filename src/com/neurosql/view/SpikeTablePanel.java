package com.neurosql.view;

import com.neurosql.model.EEGSignal;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * SpikeTablePanel — displays detected spikes in a sortable table.
 * Shows channel, time, voltage and z-score for each spike.
 * Rows are colour-coded by z-score severity.
 */
public class SpikeTablePanel extends JPanel {

    private static final String[] COLUMNS = {
            "#", "Channel", "Time (s)", "Voltage (µV)", "Z-Score", "Severity"
    };

    private final DefaultTableModel tableModel;
    private final JTable            table;
    private final JLabel            summaryLabel;

    public SpikeTablePanel() {
        setLayout(new BorderLayout(0, 4));
        setBackground(new Color(18, 18, 28));

        // Header label
        JLabel header = new JLabel("  Spike Detection Results");
        header.setForeground(new Color(200, 200, 220));
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setOpaque(true);
        header.setBackground(new Color(30, 30, 45));
        header.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        // Table model — non-editable
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setBackground(new Color(22, 22, 35));
        table.setForeground(new Color(200, 200, 220));
        table.setGridColor(new Color(45, 45, 65));
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setSelectionBackground(new Color(60, 80, 120));
        table.setFillsViewportHeight(true);

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(70);
        table.getColumnModel().getColumn(2).setPreferredWidth(90);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        // Header styling
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(new Color(35, 35, 55));
        tableHeader.setForeground(new Color(150, 180, 255));
        tableHeader.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Row colour renderer based on severity
        table.setDefaultRenderer(Object.class, new SeverityRenderer());

        // Enable sorting by clicking column headers
        table.setAutoCreateRowSorter(true);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(new Color(18, 18, 28));
        scroll.setBorder(BorderFactory.createLineBorder(
                new Color(50, 50, 70)));

        // Summary label at bottom
        summaryLabel = new JLabel("  No spikes loaded yet.");
        summaryLabel.setForeground(new Color(150, 150, 180));
        summaryLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        summaryLabel.setOpaque(true);
        summaryLabel.setBackground(new Color(25, 25, 38));

        add(header,       BorderLayout.NORTH);
        add(scroll,       BorderLayout.CENTER);
        add(summaryLabel, BorderLayout.SOUTH);
    }

    /**
     * Populate the table with detected spikes.
     * Called by DashboardFrame.refreshChart().
     */
    public void updateSpikes(List<EEGSignal> spikes) {
        tableModel.setRowCount(0); // clear existing rows

        if (spikes == null || spikes.isEmpty()) {
            summaryLabel.setText("  No spikes detected in this session.");
            return;
        }

        int index = 1;
        for (EEGSignal s : spikes) {
            String severity = getSeverity(s.getZScore());
            tableModel.addRow(new Object[]{
                    index++,
                    s.getChannelName(),
                    String.format("%.4f", s.getTsOffset()),
                    String.format("%.2f",  s.getVoltage()),
                    String.format("%.2f",  s.getZScore()),
                    severity
            });
        }

        // Count per channel for summary
        java.util.Map<String, Long> perChannel = spikes.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        EEGSignal::getChannelName,
                        java.util.stream.Collectors.counting()));

        double maxZ = spikes.stream()
                .mapToDouble(EEGSignal::getZScore)
                .max().orElse(0);

        summaryLabel.setText(String.format(
                "  Total: %d spike(s) across %d channel(s)  |  " +
                        "Peak Z-score: %.2f  |  " +
                        "Click any column header to sort",
                spikes.size(), perChannel.size(), maxZ));
    }

    private String getSeverity(double zScore) {
        if (zScore >= 10) return "CRITICAL";
        if (zScore >= 6)  return "HIGH";
        if (zScore >= 3)  return "MODERATE";
        return "LOW";
    }

    // Colour each row based on severity column
    private static class SeverityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {

            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, col);

            if (!isSelected) {
                String severity = (String) table.getValueAt(row,
                        table.getColumnCount() - 1);
                switch (severity) {
                    case "CRITICAL" ->
                            setBackground(new Color(80, 20, 20));
                    case "HIGH" ->
                            setBackground(new Color(70, 35, 10));
                    case "MODERATE" ->
                            setBackground(new Color(50, 45, 10));
                    default ->
                            setBackground(new Color(22, 22, 35));
                }
                setForeground(new Color(200, 200, 220));
            }
            return this;
        }
    }
}