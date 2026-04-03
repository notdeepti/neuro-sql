package com.neurosql.etl;

import com.neurosql.analytics.SpikeDetector;
import com.neurosql.model.EEGSignal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SpikeTest {

    // Change this number to whatever EEGDataLoader printed as Session ID
    private static final int SESSION_ID = 1;

    public static void main(String[] args) throws Exception {

        System.out.println("========================================");
        System.out.println("  Spike Detection Test");
        System.out.println("  Session ID: " + SESSION_ID);
        System.out.println("========================================");
        System.out.println();

        SpikeDetector detector = new SpikeDetector();
        List<EEGSignal> spikes = detector.detectSpikes(SESSION_ID);

        if (spikes.isEmpty()) {
            System.out.println("No spikes detected.");
            System.out.println("Possible reasons:");
            System.out.println("  1. SESSION_ID is wrong");
            System.out.println("  2. EEGDataLoader was never run");
            System.out.println("  3. Wrong database password in db.properties");
            return;
        }

        // Print spike table
        System.out.printf("%-8s  %-12s  %-14s  %-10s%n",
                "Channel", "Time (s)", "Voltage (uV)", "Z-Score");
        System.out.println("--------------------------------------------------");

        for (EEGSignal s : spikes) {
            System.out.printf("%-8s  %-12.4f  %-14.2f  %-10.2f%n",
                    s.getChannelName(),
                    s.getTsOffset(),
                    s.getVoltage(),
                    s.getZScore());
        }

        // Per-channel summary
        System.out.println();
        System.out.println("Spikes per channel:");
        System.out.println("-------------------------");

        Map<String, Long> perChannel = spikes.stream()
                .collect(Collectors.groupingBy(
                        EEGSignal::getChannelName,
                        Collectors.counting()));

        String[] channelOrder = {"Fp1", "Fp2", "T3", "T4", "C3", "C4", "O1", "O2"};
        for (String ch : channelOrder) {
            long count = perChannel.getOrDefault(ch, 0L);
            System.out.printf("  %-4s : %d%n", ch, count);
        }

        // Final summary
        System.out.println();
        System.out.println("========================================");
        System.out.println("  Total spikes flagged : " + spikes.size());
        System.out.println("  Channels affected    : " + perChannel.size() + " / 8");

        double avgZ = spikes.stream()
                .mapToDouble(EEGSignal::getZScore)
                .average()
                .orElse(0);
        System.out.printf("  Average Z-score      : %.2f%n", avgZ);

        double maxV = spikes.stream()
                .mapToDouble(EEGSignal::getVoltage)
                .max()
                .orElse(0);
        System.out.printf("  Peak spike voltage   : %.2f uV%n", maxV);
        System.out.println("========================================");
        System.out.println();
        System.out.println("Backend verified. Safe to run Main.java.");
    }
}