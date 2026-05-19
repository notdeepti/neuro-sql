package com.neurosql.view;

import com.neurosql.model.Patient;
import com.neurosql.model.Session;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * PatientInfoPanel — shows selected patient details and risk level
 * with a colour-coded indicator bar.
 */
public class PatientInfoPanel extends JPanel {

    private final JLabel nameLabel;
    private final JLabel ageLabel;
    private final JLabel riskLabel;
    private final JLabel sessionCountLabel;
    private final JPanel riskBar;

    public PatientInfoPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(22, 22, 35));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 50, 75)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        // Title
        JLabel title = new JLabel("Patient Information");
        title.setForeground(new Color(150, 180, 255));
        title.setFont(new Font("SansSerif", Font.BOLD, 12));

        // Info labels
        nameLabel         = makeLabel("Name: —");
        ageLabel          = makeLabel("Age: —");
        riskLabel         = makeLabel("Risk Level: —");
        sessionCountLabel = makeLabel("Sessions: —");

        // Risk colour bar
        riskBar = new JPanel();
        riskBar.setPreferredSize(new Dimension(0, 8));
        riskBar.setBackground(new Color(50, 50, 70));
        riskBar.setBorder(null);

        // Layout
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, 4));
        infoPanel.setOpaque(false);
        infoPanel.add(nameLabel);
        infoPanel.add(ageLabel);
        infoPanel.add(riskLabel);
        infoPanel.add(sessionCountLabel);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(title,    BorderLayout.NORTH);
        top.add(riskBar,  BorderLayout.CENTER);

        add(top,       BorderLayout.NORTH);
        add(infoPanel, BorderLayout.CENTER);
    }

    /**
     * Update the panel when a patient is selected.
     */
    public void updatePatient(Patient patient, List<Session> sessions) {
        if (patient == null) {
            nameLabel.setText("Name: —");
            ageLabel.setText("Age: —");
            riskLabel.setText("Risk Level: —");
            sessionCountLabel.setText("Sessions: —");
            riskBar.setBackground(new Color(50, 50, 70));
            return;
        }

        nameLabel.setText("Name: " + patient.getName());
        ageLabel.setText("Age: " + patient.getAge());

        String risk = patient.getHistoryRiskLevel();
        riskLabel.setText("Risk Level: " + risk);
        sessionCountLabel.setText("Sessions recorded: "
                + (sessions != null ? sessions.size() : 0));

        // Colour the risk bar
        switch (risk) {
            case "HIGH"   -> {
                riskBar.setBackground(new Color(200, 50, 50));
                riskLabel.setForeground(new Color(255, 100, 100));
            }
            case "MEDIUM" -> {
                riskBar.setBackground(new Color(200, 150, 30));
                riskLabel.setForeground(new Color(255, 200, 80));
            }
            default       -> {
                riskBar.setBackground(new Color(50, 180, 80));
                riskLabel.setForeground(new Color(100, 220, 100));
            }
        }
    }

    private JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(180, 180, 200));
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return label;
    }
}