import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ui.DashboardFrame;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            configureLookAndFeel();
            DashboardFrame dashboardFrame = new DashboardFrame();
            dashboardFrame.setVisible(true);
        });
    }

    private static void configureLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing will continue with the default look and feel.
        }
    }
}
