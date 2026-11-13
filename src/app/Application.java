package app;

import ui.LoginFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import java.awt.Font;
import java.util.Enumeration;

/**
 * Graphical entry point (Part 2).
 *
 * <p>Keeps look-and-feel setup out of the frames: Nimbus is installed once,
 * the default font is applied to every UI component, and only then is the
 * login window created on the Swing event dispatch thread.</p>
 *
 * <p>The Part 1 console test ({@code app.Main}) is untouched; run this class
 * to use the GUI.</p>
 */
public final class Application {

    private Application() {
    }

    public static void main(String[] args) {
        installLookAndFeel();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    /** Installs Nimbus and a consistent 14pt font across the application. */
    private static void installLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            // Nimbus ships with the JDK, so this is only a fallback.
            System.err.println("Nimbus look and feel not available: " + ex.getMessage());
        }
        applyGlobalFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    /**
     * Walks the UIManager defaults and replaces every default font with the
     * given one, so menus, labels, tables and buttons all match.
     */
    private static void applyGlobalFont(Font font) {
        FontUIResource resource = new FontUIResource(font);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, resource);
            }
        }
    }
}