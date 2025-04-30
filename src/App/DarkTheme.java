package App;

import javafx.scene.Scene;
import javafx.scene.control.Alert;

public class DarkTheme {
    public static final String BACKGROUND_COLOR = "#2D2D2D";
    public static final String SECONDARY_COLOR = "#3D3D3D";
    public static final String ACCENT_COLOR = "#4285F4";
    public static final String ACCENT_COLOR_HOVER = "#3498db";
    public static final String TEXT_COLOR = "#FFFFFF";
    public static final String ERROR_COLOR = "#FF5252";
    
    
    // CSS styles that can be reused for consistent theming
    public static final String CSS_BACKGROUND = "-fx-background-color: " + BACKGROUND_COLOR + ";";
    public static final String CSS_BUTTON = "-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: " + TEXT_COLOR + "; -fx-font-weight: bold;";
    public static final String CSS_BUTTON_HOVER = "-fx-background-color: derive(" + ACCENT_COLOR + ", 20%);";
    public static final String CSS_FIELD = "-fx-background-color: " + SECONDARY_COLOR + "; -fx-text-fill: " + TEXT_COLOR + "; -fx-prompt-text-fill: derive(" + TEXT_COLOR + ", -30%);";
    public static final String CSS_LABEL = "-fx-text-fill: " + TEXT_COLOR + ";";
    
    // Apply the dark theme to a scene
    public static void applyTheme(Scene scene) {
        scene.getStylesheets().add(DarkTheme.class.getResource("/App/darktheme.css").toExternalForm());
    }
    
    public static void styleDialog(Alert alert) {
        Scene scene = alert.getDialogPane().getScene();
        applyTheme(scene);
    }
}
