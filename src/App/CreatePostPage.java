package App;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreatePostPage {
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private final List<MealIngredient> ingredients = new ArrayList<>();
    private VBox ingredientsContainer;
    
    public CreatePostPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }
    
    public void show(Stage primaryStage) {
        primaryStage.setTitle("Create New Post - Ptyxes");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + DarkTheme.BACKGROUND_COLOR + ";");
        
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle(DarkTheme.CSS_BACKGROUND);
        
        // Title
        Text headerText = new Text("Create New Post");
        headerText.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        // Form fields
        TextField titleField = new TextField();
        titleField.setPromptText("Recipe Title");
        titleField.setStyle(DarkTheme.CSS_FIELD);
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Brief description of your recipe");
        descriptionArea.setStyle(DarkTheme.CSS_FIELD);
        descriptionArea.setPrefRowCount(3);
        
        TextArea instructionsArea = new TextArea();
        instructionsArea.setPromptText("Step-by-step cooking instructions");
        instructionsArea.setStyle(DarkTheme.CSS_FIELD);
        instructionsArea.setPrefRowCount(5);
        
        // Numeric inputs
        HBox timeInputs = new HBox(20);
        timeInputs.setAlignment(Pos.CENTER_LEFT);
        
        Spinner<Integer> prepTimeSpinner = new Spinner<>(1, 999, 15);
        prepTimeSpinner.setEditable(true);
        prepTimeSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        prepTimeSpinner.setStyle(DarkTheme.CSS_FIELD);
        
        Spinner<Integer> cookTimeSpinner = new Spinner<>(1, 999, 30);
        cookTimeSpinner.setEditable(true);
        cookTimeSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        cookTimeSpinner.setStyle(DarkTheme.CSS_FIELD);
        
        Spinner<Integer> servingsSpinner = new Spinner<>(1, 99, 4);
        servingsSpinner.setEditable(true);
        servingsSpinner.getStyleClass().add(Spinner.STYLE_CLASS_SPLIT_ARROWS_HORIZONTAL);
        servingsSpinner.setStyle(DarkTheme.CSS_FIELD);
        
        VBox prepTimeBox = new VBox(5);
        prepTimeBox.getChildren().addAll(new Label("Prep Time (min)"), prepTimeSpinner);
        
        VBox cookTimeBox = new VBox(5);
        cookTimeBox.getChildren().addAll(new Label("Cook Time (min)"), cookTimeSpinner);
        
        VBox servingsBox = new VBox(5);
        servingsBox.getChildren().addAll(new Label("Servings"), servingsSpinner);
        
        timeInputs.getChildren().addAll(prepTimeBox, cookTimeBox, servingsBox);
        
        // Difficulty selection
        ComboBox<String> difficultyCombo = new ComboBox<>();
        difficultyCombo.getItems().addAll("Easy", "Medium", "Hard");
        difficultyCombo.setValue("Medium");
        difficultyCombo.setStyle(DarkTheme.CSS_FIELD);
        
        // Ingredients section
        Text ingredientsHeader = new Text("Ingredients");
        ingredientsHeader.setFont(Font.font("System", FontWeight.BOLD, 16));
        ingredientsHeader.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        ingredientsContainer = new VBox(10);
        
        Button addIngredientButton = new Button("Add Ingredient");
        addIngredientButton.setStyle(DarkTheme.CSS_BUTTON);
        addIngredientButton.setOnAction(e -> addIngredientRow());
        
        // Action buttons
        HBox buttonBar = new HBox(20);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle(DarkTheme.CSS_BUTTON);
        
        Button submitButton = new Button("Create Post");
        submitButton.setStyle(DarkTheme.CSS_BUTTON);
        
        buttonBar.getChildren().addAll(cancelButton, submitButton);
        
        // Add all components to main container
        mainContainer.getChildren().addAll(
            headerText,
            new Label("Title:"),
            titleField,
            new Label("Description:"),
            descriptionArea,
            new Label("Instructions:"),
            instructionsArea,
            timeInputs,
            new Label("Difficulty:"),
            difficultyCombo,
            ingredientsHeader,
            ingredientsContainer,
            addIngredientButton,
            buttonBar
        );
        
        // Set up cancel action
        cancelButton.setOnAction(e -> {
            MainPage mainPage = new MainPage(databaseHelper, currentUser);
            mainPage.show(primaryStage);
        });
        
        // Set up submit action
        submitButton.setOnAction(e -> {
            try {
                // Validate inputs
                if (titleField.getText().trim().isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please enter a title");
                    return;
                }
                
                // Create the post
                MealPost post = currentUser.createMealPost(
                    databaseHelper,
                    titleField.getText().trim(),
                    descriptionArea.getText().trim(),
                    instructionsArea.getText().trim(),
                    prepTimeSpinner.getValue(),
                    cookTimeSpinner.getValue(),
                    servingsSpinner.getValue(),
                    difficultyCombo.getValue(),
                    getIngredientsList()
                );
                
                if (post != null) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Post created successfully!");
                    MainPage mainPage = new MainPage(databaseHelper, currentUser);
                    mainPage.show(primaryStage);
                }
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create post: " + ex.getMessage());
            }
        });
        
        scrollPane.setContent(mainContainer);
        Scene scene = new Scene(scrollPane, 800, 700);
        DarkTheme.applyTheme(scene);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void addIngredientRow() {
        HBox ingredientRow = new HBox(10);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Ingredient name");
        nameField.setStyle(DarkTheme.CSS_FIELD);
        nameField.setPrefWidth(200);
        
        TextField quantityField = new TextField();
        quantityField.setPromptText("Amount");
        quantityField.setStyle(DarkTheme.CSS_FIELD);
        quantityField.setPrefWidth(100);
        
        TextField unitField = new TextField();
        unitField.setPromptText("Unit");
        unitField.setStyle(DarkTheme.CSS_FIELD);
        unitField.setPrefWidth(100);
        
        Button removeButton = new Button("Remove");
        removeButton.setStyle(DarkTheme.CSS_BUTTON);
        removeButton.setOnAction(e -> ingredientsContainer.getChildren().remove(ingredientRow));
        
        ingredientRow.getChildren().addAll(nameField, quantityField, unitField, removeButton);
        ingredientsContainer.getChildren().add(ingredientRow);
    }
    
    private List<MealIngredient> getIngredientsList() {
        List<MealIngredient> ingredients = new ArrayList<>();
        
        for (javafx.scene.Node node : ingredientsContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox row = (HBox) node;
                TextField nameField = (TextField) row.getChildren().get(0);
                TextField quantityField = (TextField) row.getChildren().get(1);
                TextField unitField = (TextField) row.getChildren().get(2);
                
                if (!nameField.getText().trim().isEmpty()) {
                    MealIngredient ingredient = new MealIngredient();
                    ingredient.setName(nameField.getText().trim());
                    ingredient.setQuantity(Float.parseFloat(quantityField.getText().trim()));
                    ingredient.setUnit(unitField.getText().trim());
                    ingredients.add(ingredient);
                }
            }
        }
        
        return ingredients;
    }
    
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        DarkTheme.styleDialog(alert);
        alert.showAndWait();
    }
}