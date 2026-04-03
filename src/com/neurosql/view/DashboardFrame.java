package com.neurosql.view;

import com.neurosql.controller.DiagnosticController;
import com.neurosql.model.EEGSignal;
import com.neurosql.model.Patient;
import com.neurosql.model.Session;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class DashboardFrame extends JFrame {

    private final JList<Patient> patientList;
    private final JList<Session> sessionList;
    private final JLabel         spikeCountLabel;
    private final JLabel         statusLabel;
    private final EEGChartPanel  chartPanel;

    private DiagnosticController controller;

    public DashboardFrame() {
        super("Neuro-SQL EEG Diagnostic Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 750);
        setLocationRelativeTo(null);

        patientList     = new JList<>();
        sessionList     = new JList<>();
        spikeCountLabel = new JLabel("Spikes detected: -");
        spikeCountLabel.setForeground(new Color(220, 80, 80));
        spikeCountLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        chartPanel = new EEGChartPanel(null, null);
        JScrollPane chartScroll = new JScrollPane(chartPanel);
        chartScroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        statusLabel = new JLabel(
                "Ready. Select a patient to begin.");
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));

        getContentPane().setLayout(new BorderLayout(6, 0));
        getContentPane().add(buildSidebar(), BorderLayout.WEST);
        getContentPane().add(chartScroll,   BorderLayout.CENTER);
        getContentPane().add(statusLabel,   BorderLayout.SOUTH);
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(new EmptyBorder(10, 8, 10, 8));
        panel.setBackground(new Color(28, 28, 40));

        JLabel pLabel = new JLabel("Patients");
        pLabel.setForeground(Color.LIGHT_GRAY);
        pLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        patientList.setBackground(new Color(38, 38, 55));
        patientList.setForeground(Color.WHITE);
        patientList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        patientList.setCellRenderer(new PatientCellRenderer());
        patientList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && controller != null) {
                controller.onPatientSelected(
                        patientList.getSelectedValue());
            }
        });

        JLabel sLabel = new JLabel("Sessions");
        sLabel.setForeground(Color.LIGHT_GRAY);
        sLabel.setFont(new Font("SansSerif", Font.BOLD, 12));

        sessionList.setBackground(new Color(38, 38, 55));
        sessionList.setForeground(Color.WHITE);
        sessionList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);
        sessionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && controller != null) {
                controller.onSessionSelected(
                        sessionList.getSelectedValue());
            }
        });

        JButton loadBtn = new JButton("Load and Detect Spikes");
        loadBtn.setBackground(new Color(60, 130, 200));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setFocusPainted(false);
        loadBtn.addActionListener(e -> {
            if (controller != null) controller.onLoadRequested();
        });

        JPanel top = new JPanel(new GridLayout(6, 1, 0, 4));
        top.setOpaque(false);
        top.add(pLabel);
        top.add(new JScrollPane(patientList));
        top.add(sLabel);
        top.add(new JScrollPane(sessionList));
        top.add(spikeCountLabel);
        top.add(loadBtn);

        panel.add(top, BorderLayout.NORTH);
        return panel;
    }

    public void setController(DiagnosticController controller) {
        this.controller = controller;
    }

    public void setPatients(List<Patient> patients) {
        DefaultListModel<Patient> model = new DefaultListModel<>();
        patients.forEach(model::addElement);
        patientList.setModel(model);
    }

    public void setSessions(List<Session> sessions) {
        DefaultListModel<Session> model = new DefaultListModel<>();
        sessions.forEach(model::addElement);
        sessionList.setModel(model);
    }

    public void refreshChart(EEGSignal[] all, EEGSignal[] spikes) {
        chartPanel.update(List.of(all), List.of(spikes));
        spikeCountLabel.setText("Spikes detected: " + spikes.length);
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
    }

    private static class PatientCellRenderer
            extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
            if (value instanceof Patient p) {
                setText(p.getName() + " (age " + p.getAge() + ")");
                String risk = p.getHistoryRiskLevel();
                if ("HIGH".equals(risk))
                    setForeground(new Color(255, 100, 100));
                else if ("MEDIUM".equals(risk))
                    setForeground(new Color(255, 200, 80));
                else
                    setForeground(new Color(100, 200, 100));
            }
            setBackground(isSelected
                    ? new Color(60, 80, 120)
                    : new Color(38, 38, 55));
            return this;
        }
    }
}