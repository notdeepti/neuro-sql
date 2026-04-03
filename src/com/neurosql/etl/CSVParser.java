package com.neurosql.etl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CSVParser {

    private static final int COL_TS_OFFSET    = 0;
    private static final int COL_CHANNEL_NAME = 1;
    private static final int COL_VOLTAGE      = 2;

    private static final java.util.Set<String> VALID_CHANNELS = java.util.Set.of(
            "Fp1", "Fp2", "T3", "T4", "C3", "C4", "O1", "O2"
    );

    public ParseResult parse(String csvPath) throws IOException {
        Path path = Paths.get(csvPath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("CSV file not found: " + csvPath);
        }

        List<Object[]> rows     = new ArrayList<>();
        List<String>   warnings = new ArrayList<>();
        int lineNum = 0;
        int skipped = 0;

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (lineNum == 1) { validateHeader(line, csvPath); continue; }
                if (line.isBlank()) continue;

                String[] parts = line.split(",", -1);

                if (parts.length != 3) {
                    warnings.add("Line " + lineNum + ": expected 3 columns, got " + parts.length + " — skipped");
                    skipped++;
                    continue;
                }

                try {
                    double tsOffset    = Double.parseDouble(parts[COL_TS_OFFSET].trim());
                    String channelName = parts[COL_CHANNEL_NAME].trim();
                    double voltage     = Double.parseDouble(parts[COL_VOLTAGE].trim());

                    if (!VALID_CHANNELS.contains(channelName)) {
                        warnings.add("Line " + lineNum + ": unknown channel '" + channelName + "' — skipped");
                        skipped++;
                        continue;
                    }
                    rows.add(new Object[]{tsOffset, channelName, voltage});

                } catch (NumberFormatException e) {
                    warnings.add("Line " + lineNum + ": number parse error — skipped");
                    skipped++;
                }
            }
        }

        System.out.printf("[CSVParser] Parsed %,d valid rows, %d skipped%n", rows.size(), skipped);
        return new ParseResult(rows.toArray(new Object[0][]), warnings, skipped);
    }

    private void validateHeader(String headerLine, String csvPath) {
        String[] cols = headerLine.split(",");
        if (cols.length < 3
                || !cols[0].trim().equalsIgnoreCase("ts_offset")
                || !cols[1].trim().equalsIgnoreCase("channel_name")
                || !cols[2].trim().equalsIgnoreCase("voltage")) {
            System.err.println("[CSVParser] WARNING: Unexpected header: " + headerLine);
        }
    }

    public static class ParseResult {
        private final Object[][] rows;
        private final List<String> warnings;
        private final int skippedCount;

        public ParseResult(Object[][] rows, List<String> warnings, int skippedCount) {
            this.rows         = rows;
            this.warnings     = warnings;
            this.skippedCount = skippedCount;
        }

        public Object[][] getRows()       { return rows; }
        public List<String> getWarnings() { return warnings; }
        public int getSkippedCount()      { return skippedCount; }
        public int getRowCount()          { return rows.length; }

        public void printWarnings() {
            if (warnings.isEmpty()) return;
            warnings.forEach(w -> System.out.println("  WARNING: " + w));
        }
    }
}