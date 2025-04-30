package App;

import java.sql.SQLException;
import java.util.regex.Pattern;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class FirstPage {
    
    private final DatabaseHelper databaseHelper;
    private static final String APP_TITLE = "Ptyxes Login";
    
    public FirstPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    
    public void show(Stage primaryStage) {
        primaryStage.setTitle(APP_TITLE);
        
        // Main layout container
        BorderPane root = new BorderPane();
        root.setStyle(DarkTheme.CSS_BACKGROUND);
        root.setPadding(new Insets(20));
        
        // Header section
        Text headerText = new Text("Ptyxes");
        headerText.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        HBox headerBox = new HBox(headerText);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        // Login form
        GridPane loginForm = new GridPane();
        loginForm.setHgap(10);
        loginForm.setVgap(10);
        loginForm.setAlignment(Pos.CENTER);
        
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle(DarkTheme.CSS_LABEL);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle(DarkTheme.CSS_FIELD);
        usernameField.setPrefWidth(250);
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle(DarkTheme.CSS_LABEL);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle(DarkTheme.CSS_FIELD);
        
        Button loginButton = new Button("Login");
        loginButton.setStyle(DarkTheme.CSS_BUTTON);
        loginButton.setMaxWidth(Double.MAX_VALUE);
        
        // Add hover effect
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(DarkTheme.CSS_BUTTON));
        
        // Add login button functionality
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText(), primaryStage));
        
        // Add Enter key functionality to username field
        usernameField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus(); // Move focus to password field
            }
        });
        
        // Add Enter key functionality to password field
        passwordField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleLogin(usernameField.getText(), passwordField.getText(), primaryStage);
            }
        });
        
        Text register = new Text("Register");
        register.setFill(Color.web(DarkTheme.ACCENT_COLOR));
        register.setFont(Font.font("System", 12));

        // Add hover effect for register text
        register.setOnMouseEntered(e -> {
            register.setFill(Color.web(DarkTheme.ACCENT_COLOR_HOVER));
            register.setUnderline(true);
            register.setCursor(javafx.scene.Cursor.HAND);
        });
        register.setOnMouseExited(e -> {
            register.setFill(Color.web(DarkTheme.ACCENT_COLOR));
            register.setUnderline(false);
        });

        // Add click functionality for register text
        register.setOnMouseClicked(e -> showRegistrationDialog(primaryStage));
        
        // Add components to the form
        loginForm.add(usernameLabel, 0, 0);
        loginForm.add(usernameField, 0, 1);
        loginForm.add(passwordLabel, 0, 2);
        loginForm.add(passwordField, 0, 3);
        loginForm.add(loginButton, 0, 4);
        
        HBox registerBox = new HBox(register);
        registerBox.setAlignment(Pos.CENTER_RIGHT);
        registerBox.setPadding(new Insets(5, 0, 0, 0));
        loginForm.add(registerBox, 0, 5);
        
        // Create footer with copyright info
        Text footerText = new Text("© 2025 David Salazar");
        footerText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        footerText.setFont(Font.font("System", 10));
        
        HBox footerBox = new HBox(footerText);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(20, 0, 0, 0));
        
        // Add all components to the main layout
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(headerBox, loginForm, footerBox);
        root.setCenter(centerContent);
        
        // Create the scene
        Scene scene = new Scene(root, 400, 400);
        
        // Apply CSS styling for consistency
        DarkTheme.applyTheme(scene);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void handleLogin(String username, String password, Stage primaryStage) {
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(AlertType.ERROR, "Login Error", "Please enter both username and password.");
            return;
        }
        
        try {
            // Here you would implement the actual database authentication
            boolean authenticated = authenticateUser(username, password);
            
            if (authenticated) {
                // Navigate to the main application page upon successful login
                navigateToMainPage(primaryStage, username);
            } else {
                showAlert(AlertType.ERROR, "Login Failed", "Invalid username or password. Please try again.");
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }
    
    private boolean authenticateUser(String username, String password) {
        try {
            User user = databaseHelper.authenticateUser(username, password);
            return user != null;
        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
            return false;
        }
    }
    
    private void navigateToMainPage(Stage primaryStage, String username) {
        try {
            // Get the User object for the authenticated user
            User user = databaseHelper.getUserByUsername(username);
            if (user != null) {
                // Navigate to the MainPage
                MainPage mainPage = new MainPage(databaseHelper, user);
                mainPage.show(primaryStage);
            } else {
                showAlert(AlertType.ERROR, "Error", "Could not find user details");
            }
        } catch (SQLException e) {
            showAlert(AlertType.ERROR, "Error", "Database error: " + e.getMessage());
        }
    }
    
    private void showRegistrationDialog(Stage primaryStage) {
        // Create a dialog stage
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Register New Account");
        dialogStage.initOwner(primaryStage);
        
        // Create the layout and components
        BorderPane dialogRoot = new BorderPane();
        dialogRoot.setStyle(DarkTheme.CSS_BACKGROUND);
        dialogRoot.setPadding(new Insets(20));
        
        // Header text
        Text headerText = new Text("Create New Account");
        headerText.setFont(Font.font("System", FontWeight.BOLD, 20));
        headerText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        HBox headerBox = new HBox(headerText);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        // Registration form
        GridPane registrationForm = new GridPane();
        registrationForm.setHgap(10);
        registrationForm.setVgap(10);
        registrationForm.setAlignment(Pos.CENTER);
        
        // Username field
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle(DarkTheme.CSS_LABEL);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setStyle(DarkTheme.CSS_FIELD);
        
        // Email field
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle(DarkTheme.CSS_LABEL);
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle(DarkTheme.CSS_FIELD);
        
        // Password field
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle(DarkTheme.CSS_LABEL);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");
        passwordField.setStyle(DarkTheme.CSS_FIELD);
        
        // Confirm password field
        Label confirmPasswordLabel = new Label("Confirm Password:");
        confirmPasswordLabel.setStyle(DarkTheme.CSS_LABEL);
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setStyle(DarkTheme.CSS_FIELD);
        
        // Buttons
        Button registerButton = new Button("Register");
        registerButton.setStyle(DarkTheme.CSS_BUTTON);
        registerButton.setMaxWidth(Double.MAX_VALUE);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle(DarkTheme.CSS_BUTTON);
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        
        // Add hover effects
        registerButton.setOnMouseEntered(e -> registerButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        registerButton.setOnMouseExited(e -> registerButton.setStyle(DarkTheme.CSS_BUTTON));
        
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle(DarkTheme.CSS_BUTTON));
        
        // Add fields to the form
        registrationForm.add(usernameLabel, 0, 0);
        registrationForm.add(usernameField, 0, 1);
        registrationForm.add(emailLabel, 0, 2);
        registrationForm.add(emailField, 0, 3);
        registrationForm.add(passwordLabel, 0, 4);
        registrationForm.add(passwordField, 0, 5);
        registrationForm.add(confirmPasswordLabel, 0, 6);
        registrationForm.add(confirmPasswordField, 0, 7);
        
        // Button container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(registerButton, cancelButton);
        
        // Add components to dialog layout
        VBox centerContent = new VBox(20);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(headerBox, registrationForm, buttonBox);
        dialogRoot.setCenter(centerContent);
        
        // Handle register button action
        registerButton.setOnAction(e -> {
            // Validate inputs
            if (usernameField.getText().isEmpty() || emailField.getText().isEmpty() || 
                passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()) {
                showAlert(AlertType.ERROR, "Registration Error", "Please fill in all fields.");
                return;
            }
            
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                showAlert(AlertType.ERROR, "Registration Error", "Passwords do not match.");
                return;
            }
            
            // Basic email validation
            if (!isValidEmail(emailField.getText())) {
                showAlert(AlertType.ERROR, "Registration Error", "Please enter a valid email address.");
                return;
            }
            
            // Create the user
            try {
                User newUser = databaseHelper.createUser(
                    usernameField.getText(),
                    passwordField.getText(), // In a real app, use password hashing
                    emailField.getText(),
                    0 // Regular user role
                );
                
                if (newUser != null) {
                    showAlert(AlertType.INFORMATION, "Registration Successful", "Your account has been created. You can now log in.");
                    dialogStage.close();
                } else {
                    showAlert(AlertType.ERROR, "Registration Failed", "Could not create user account. The username might already be taken.");
                }
            } catch (SQLException ex) {
                showAlert(AlertType.ERROR, "Database Error", "Error creating user: " + ex.getMessage());
            }
        });
        
        // Handle cancel button action
        cancelButton.setOnAction(e -> dialogStage.close());
        
        // Create and show the scene
        Scene dialogScene = new Scene(dialogRoot, 350, 450);
        DarkTheme.applyTheme(dialogScene);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply dark theme to the dialog
        DarkTheme.styleDialog(alert);
        
        alert.showAndWait();
    }
}