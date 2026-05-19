package com.neurosql.analytics;

import com.neurosql.model.EEGSignal;
import com.neurosql.model.Patient;
import com.neurosql.model.Session;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ReportExporter — writes a spike detection report to a .txt file.
 * Called when the user clicks "Export Report" in the dashboard.
 */
public class ReportExporter {

    /**
     * Export a spike report to the given file path.
     *
     * @param path     full path to write the report to
     * @param patient  the patient being reported on
     * @param session  the session being reported on
     * @param spikes   list of detected spikes
     * @throws IOException if the file cannot be written
     */
    public void export(String path,
                       Patient patient,
                       Session session,
                       List<EEGSignal> spikes) throws IOException {

        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(path)))) {

            String line = "=".repeat(60);
            String now  = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Header
            pw.println(line);
            pw.println("  NEURO-SQL EEG SPIKE DETECTION REPORT");
            pw.println(line);
            pw.println();
            pw.println("Generated     : " + now);
            pw.println("Patient Name  : " + patient.getName());
            pw.println("Patient Age   : " + patient.getAge());
            pw.println("Risk Level    : " + patient.getHistoryRiskLevel());
            pw.println("Session ID    : " + session.getSessionId());
            pw.println("Session Date  : " + session.getSessionDate());
            pw.println();

            // Summary
            pw.println(line);
            pw.println("  SUMMARY");
            pw.println(line);
            pw.println("Total spikes detected : " + spikes.size());

            if (!spikes.isEmpty()) {
                Map<String, Long> perChannel = spikes.stream()
                        .collect(Collectors.groupingBy(
                                EEGSignal::getChannelName,
                                Collectors.counting()));

                pw.println("Channels affected     : "
                        + perChannel.size() + " / 8");

                double avgZ = spikes.stream()
                        .mapToDouble(EEGSignal::getZScore)
                        .average().orElse(0);
                double maxZ = spikes.stream()
                        .mapToDouble(EEGSignal::getZScore)
                        .max().orElse(0);
                double maxV = spikes.stream()
                        .mapToDouble(EEGSignal::getVoltage)
                        .max().orElse(0);

                pw.printf("Average Z-score       : %.2f%n", avgZ);
                pw.printf("Peak Z-score          : %.2f%n", maxZ);
                pw.printf("Peak voltage          : %.2f uV%n", maxV);
                pw.println();

                // Per channel breakdown
                pw.println(line);
                pw.println("  PER CHANNEL BREAKDOWN");
                pw.println(line);
                String[] order = {
                        "Fp1","Fp2","T3","T4","C3","C4","O1","O2"
                };
                for (String ch : order) {
                    long count = perChannel.getOrDefault(ch, 0L);
                    String bar = "#".repeat((int) count);
                    pw.printf("  %-4s : %2d spike(s)  %s%n",
                            ch, count, bar);
                }
                pw.println();

                // Full spike table
                pw.println(line);
                pw.println("  FULL SPIKE LIST");
                pw.println(line);
                pw.printf("%-5s %-8s %-12s %-14s %-10s %-10s%n",
                        "No.", "Channel", "Time (s)",
                        "Voltage (uV)", "Z-Score", "Severity");
                pw.println("-".repeat(60));

                int i = 1;
                for (EEGSignal s : spikes) {
                    String sev = getSeverity(s.getZScore());
                    pw.printf("%-5d %-8s %-12.4f %-14.2f %-10.2f %-10s%n",
                            i++,
                            s.getChannelName(),
                            s.getTsOffset(),
                            s.getVoltage(),
                            s.getZScore(),
                            sev);
                }
            }

            pw.println();
            pw.println(line);
            pw.println("  END OF REPORT");
            pw.println(line);
        }

        System.out.println("[Report] Exported to: " + path);
    }

    private String getSeverity(double z) {
        if (z >= 10) return "CRITICAL";
        if (z >= 6)  return "HIGH";
        if (z >= 3)  return "MODERATE";
        return "LOW";
    }
}