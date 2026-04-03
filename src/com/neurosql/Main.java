package com.neurosql;

import com.neurosql.controller.DiagnosticController;
import com.neurosql.view.DashboardFrame;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                // Step 1 - Create the window
                DashboardFrame frame = new DashboardFrame();

                // Step 2 - Create controller and connect it to the window
                // This also opens the PostgreSQL connection
                DiagnosticController controller = new DiagnosticController(frame);

                // Step 3 - Load patient list from database immediately
                controller.init();

                // Step 4 - Show the window
                frame.setVisible(true);

            } catch (Exception e) {
                System.err.println("========================================");
                System.err.println("STARTUP FAILED: " + e.getMessage());
                System.err.println("----------------------------------------");
                System.err.println("Check the following:");
                System.err.println("  1. PostgreSQL is running");
                System.err.println("     Open Windows Services, find");
                System.err.println("     postgresql-x64-17, click Start");
                System.err.println("  2. Password in resources/db.properties");
                System.err.println("     is correct");
                System.err.println("  3. schema.sql was run in pgAdmin");
                System.err.println("     (tables must exist)");
                System.err.println("========================================");
                e.printStackTrace();
            }
        });
    }
}