package com.neurosql.analytics;

import com.neurosql.dao.DatabaseManager;
import com.neurosql.model.EEGSignal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SpikeDetector {

    private static final double Z_THRESHOLD = 3.0;

    public List<EEGSignal> detectSpikes(int sessionId) throws Exception {
        List<EEGSignal> spikes = new ArrayList<>();

        String sql =
                "WITH windowed AS (" +
                        "    SELECT" +
                        "        signal_id," +
                        "        session_id," +
                        "        ts_offset," +
                        "        channel_name," +
                        "        voltage," +
                        "        AVG(voltage) OVER (" +
                        "            PARTITION BY session_id, channel_name" +
                        "            ORDER BY ts_offset" +
                        "            ROWS BETWEEN 10 PRECEDING AND 10 FOLLOWING" +
                        "        ) AS rolling_avg," +
                        "        STDDEV(voltage) OVER (" +
                        "            PARTITION BY session_id, channel_name" +
                        "            ORDER BY ts_offset" +
                        "            ROWS BETWEEN 10 PRECEDING AND 10 FOLLOWING" +
                        "        ) AS rolling_stddev," +
                        "        RANK() OVER (" +
                        "            PARTITION BY session_id, channel_name" +
                        "            ORDER BY voltage DESC" +
                        "        ) AS voltage_rank" +
                        "    FROM eeg_signals" +
                        "    WHERE session_id = ?" +
                        ")," +
                        "scored AS (" +
                        "    SELECT *," +
                        "        (voltage - rolling_avg) / NULLIF(rolling_stddev, 0) AS z_score" +
                        "    FROM windowed" +
                        ")" +
                        "SELECT *" +
                        " FROM scored" +
                        " WHERE z_score > ?" +
                        " ORDER BY ts_offset, channel_name";

        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sessionId);
            ps.setDouble(2, Z_THRESHOLD);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                EEGSignal spike = new EEGSignal();
                spike.setSignalId(rs.getLong("signal_id"));
                spike.setSessionId(rs.getInt("session_id"));
                spike.setTsOffset(rs.getDouble("ts_offset"));
                spike.setChannelName(rs.getString("channel_name"));
                spike.setVoltage(rs.getDouble("voltage"));
                spike.setZScore(rs.getDouble("z_score"));
                spikes.add(spike);
            }
        }

        System.out.printf("SpikeDetector: found %d spike(s) in session %d%n",
                spikes.size(), sessionId);
        return spikes;
    }
}
