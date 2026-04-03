"""
eeg_simulator.py
Generates simulated 8-channel EEG data and writes it as a CSV.

Usage:
    python eeg_simulator.py

Output:
    eeg_data.csv  — columns: ts_offset, channel_name, voltage

The CSV is then loaded into PostgreSQL via DatabaseManager.batchInsertEEGSignals().
"""

import numpy as np
import pandas as pd

# ── Config ──────────────────────────────────────────────────────────────────
CHANNELS     = ['Fp1', 'Fp2', 'T3', 'T4', 'C3', 'C4', 'O1', 'O2']
SAMPLE_RATE  = 256    # Hz
DURATION_SEC = 30     # seconds of EEG
SPIKES_PER_CHANNEL = 5   # injected spike events per channel
OUTPUT_FILE  = 'eeg_data.csv'

def simulate_channel(t: np.ndarray, rng: np.random.Generator) -> np.ndarray:
    """
    Realistic EEG = alpha (8-13 Hz) + beta (13-30 Hz) + gaussian noise.
    Random spike events are injected to simulate seizure activity.
    """
    alpha_freq = rng.uniform(8.0, 12.0)
    beta_freq  = rng.uniform(18.0, 25.0)

    alpha = np.sin(2 * np.pi * alpha_freq * t) * rng.uniform(15, 25)
    beta  = np.sin(2 * np.pi * beta_freq  * t) * rng.uniform(5,  12)
    noise = rng.normal(0, rng.uniform(3, 7), len(t))
    signal = alpha + beta + noise

    # Inject spikes (>3σ amplitude bursts)
    spike_indices = rng.choice(len(t), SPIKES_PER_CHANNEL, replace=False)
    for idx in spike_indices:
        amplitude = rng.uniform(150, 320)   # µV — well above the noise floor
        width     = rng.integers(2, 6)      # samples wide
        for offset in range(-width, width + 1):
            j = idx + offset
            if 0 <= j < len(t):
                signal[j] += amplitude * (1 - abs(offset) / (width + 1))

    return signal

def main():
    rng = np.random.default_rng(seed=42)   # reproducible output
    t   = np.linspace(0, DURATION_SEC, SAMPLE_RATE * DURATION_SEC, endpoint=False)

    rows = []
    for ch in CHANNELS:
        signal = simulate_channel(t, rng)
        for ts, v in zip(t, signal):
            rows.append({
                'ts_offset':    round(float(ts), 6),
                'channel_name': ch,
                'voltage':      round(float(v),  4)
            })

    df = pd.DataFrame(rows)
    df.to_csv(OUTPUT_FILE, index=False)
    total = len(df)
    print(f"Generated {total:,} rows ({SAMPLE_RATE} Hz × {DURATION_SEC}s × {len(CHANNELS)} channels)")
    print(f"Saved to: {OUTPUT_FILE}")
    print(f"Voltage range: {df['voltage'].min():.1f} µV to {df['voltage'].max():.1f} µV")

if __name__ == '__main__':
    main()