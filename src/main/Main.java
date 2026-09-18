package com.digitaldetective;

import com.digitaldetective.ui.WelcomeScreen;

import javax.swing.*;

/**
 * Application entry point. Launches the Swing UI on the Event Dispatch
 * Thread, starting with the welcome screen.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Use the operating system's native look and feel as a base; the
        // application then layers its own dark theme colors on top via Theme.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the cross-platform default look and feel if the
            // native one is unavailable on this system.
        }

        SwingUtilities.invokeLater(() -> {
            WelcomeScreen welcomeScreen = new WelcomeScreen();
            welcomeScreen.setVisible(true);
        });
    }
}
