package com.neurosql.controller;

import com.neurosql.analytics.SpikeDetector;
import com.neurosql.dao.EEGSignalDAO;
import com.neurosql.dao.PatientDAO;
import com.neurosql.dao.SessionDAO;
import com.neurosql.model.EEGSignal;
import com.neurosql.model.Patient;
import com.neurosql.model.Session;
import com.neurosql.view.DashboardFrame;
import java.util.List;

public class DiagnosticController {

    private final DashboardFrame view;
    private final PatientDAO     patientDAO;
    private final SessionDAO     sessionDAO;
    private final EEGSignalDAO   signalDAO;
    private final SpikeDetector  spikeDetector;

    private Patient selectedPatient;
    private Session selectedSession;

    public DiagnosticController(DashboardFrame view) throws Exception {
        this.view          = view;
        this.patientDAO    = new PatientDAO();
        this.sessionDAO    = new SessionDAO();
        this.signalDAO     = new EEGSignalDAO();
        this.spikeDetector = new SpikeDetector();
        view.setController(this);
    }

    public void init() {
        try {
            List<Patient> patients = patientDAO.findAll();
            view.setPatients(patients);
            view.setStatus("Loaded " + patients.size()
                    + " patient(s). Select one to continue.");
        } catch (Exception e) {
            view.setStatus("ERROR loading patients: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onPatientSelected(Patient patient) {
        if (patient == null) return;
        this.selectedPatient = patient;
        try {
            List<Session> sessions =
                    sessionDAO.findByPatient(patient.getPatientId());
            view.setSessions(sessions);
            view.setStatus("Patient: " + patient.getName()
                    + " | Risk: " + patient.getHistoryRiskLevel()
                    + " | " + sessions.size() + " session(s)");
        } catch (Exception e) {
            view.setStatus("ERROR loading sessions: " + e.getMessage());
        }
    }

    public void onSessionSelected(Session session) {
        if (session == null) return;
        this.selectedSession = session;
        view.setStatus("Session " + session.getSessionId()
                + " selected. Click Load and Detect Spikes.");
    }

    public void onLoadRequested() {
        if (selectedSession == null) {
            view.setStatus("Please select a session first.");
            return;
        }
        view.setStatus("Loading EEG data and running spike detection...");

        new Thread(() -> {
            try {
                int sid = selectedSession.getSessionId();

                List<EEGSignal> allSignals = signalDAO.findBySession(sid);
                List<EEGSignal> spikes     = spikeDetector.detectSpikes(sid);

                javax.swing.SwingUtilities.invokeLater(() -> {
                    view.refreshChart(
                            allSignals.toArray(new EEGSignal[0]),
                            spikes.toArray(new EEGSignal[0])
                    );
                    view.setStatus(String.format(
                            "Session %d — %,d samples | %d spike(s) flagged",
                            sid, allSignals.size(), spikes.size()
                    ));
                });

            } catch (Exception e) {
                javax.swing.SwingUtilities.invokeLater(() ->
                        view.setStatus("ERROR: " + e.getMessage()));
                e.printStackTrace();
            }
        }, "eeg-loader").start();
    }
}