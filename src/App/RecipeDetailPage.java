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
import java.util.List;

/**
 * The RecipeDetailPage class displays the full details of a meal post.
 * It includes the recipe information, ingredients, instructions, and comments.
 */
public class RecipeDetailPage {
    
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private final MealPost mealPost;
    private VBox commentsContainer;
    
    public RecipeDetailPage(DatabaseHelper databaseHelper, User currentUser, MealPost mealPost) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
        this.mealPost = mealPost;
    }
    
    public void show(Stage primaryStage) {
        primaryStage.setTitle(mealPost.getTitle() + " - Ptyxes");
        
        // Create the main scroll pane for the page content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + DarkTheme.BACKGROUND_COLOR + ";");
        
        // Main container for all content
        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle(DarkTheme.CSS_BACKGROUND);
        
        // Top navigation and back button
        HBox topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        
        Button backButton = new Button("← Back to Recipes");
        backButton.setStyle(DarkTheme.CSS_BUTTON);
        
        backButton.setOnAction(e -> {
            MainPage mainPage = new MainPage(databaseHelper, currentUser);
            mainPage.show(primaryStage);
        });
        
        topBar.getChildren().add(backButton);
        
        // Recipe title
        Text titleText = new Text(mealPost.getTitle());
        titleText.setFont(Font.font("System", FontWeight.BOLD, 28));
        titleText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        // Recipe metadata in a horizontal box
        HBox metadataBox = new HBox(20);
        metadataBox.setAlignment(Pos.CENTER_LEFT);
        
        Label prepTime = new Label("Prep Time: " + mealPost.getPreparationTime() + " min");
        prepTime.setStyle(DarkTheme.CSS_LABEL);
        
        Label cookTime = new Label("Cook Time: " + mealPost.getCookingTime() + " min");
        cookTime.setStyle(DarkTheme.CSS_LABEL);
        
        Label totalTime = new Label("Total Time: " + (mealPost.getPreparationTime() + mealPost.getCookingTime()) + " min");
        totalTime.setStyle(DarkTheme.CSS_LABEL);
        
        Label difficulty = new Label("Difficulty: " + mealPost.getDifficulty());
        difficulty.setStyle(DarkTheme.CSS_LABEL);
        
        Label servings = new Label("Servings: " + mealPost.getServings());
        servings.setStyle(DarkTheme.CSS_LABEL);
        
        Label dietaryType = new Label(mealPost.getDietaryType());
        dietaryType.setStyle(DarkTheme.CSS_LABEL);
        
        // Add color coding for dietary types
        if ("Vegan".equals(mealPost.getDietaryType())) {
            dietaryType.setTextFill(Color.web("#4CAF50")); // Green for vegan
        } else if ("Vegetarian".equals(mealPost.getDietaryType())) {
            dietaryType.setTextFill(Color.web("#8BC34A")); // Light green for vegetarian
        }
        
        metadataBox.getChildren().addAll(prepTime, cookTime, totalTime, difficulty, servings, dietaryType);
        
        // Recipe description
        VBox descriptionBox = new VBox(10);
        
        Text descriptionTitle = new Text("Description");
        descriptionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        descriptionTitle.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        Text descriptionText = new Text(mealPost.getDescription());
        descriptionText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        descriptionText.setWrappingWidth(700);
        
        descriptionBox.getChildren().addAll(descriptionTitle, descriptionText);
        
        // Ingredients section
        VBox ingredientsBox = new VBox(10);
        
        Text ingredientsTitle = new Text("Ingredients");
        ingredientsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        ingredientsTitle.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        VBox ingredientsList = new VBox(5);
        
        try {
            List<MealIngredient> ingredients = mealPost.getIngredients();

            if (ingredients.isEmpty()) {
                Text noIngredientsText = new Text("No ingredients listed for this recipe.");
                noIngredientsText.setFill(Color.web(DarkTheme.TEXT_COLOR));
                ingredientsList.getChildren().add(noIngredientsText);
            } else {
                for (MealIngredient ingredient : ingredients) {
                    Text ingredientText = ingredient.getUnit().isEmpty() ?
                            new Text("• " + ingredient.getQuantity() + " " + ingredient.getName()) :
                            new Text("• " + ingredient.getQuantity() + " " + ingredient.getUnit() + " " + ingredient.getName());
                    ingredientText.setFill(Color.web(DarkTheme.TEXT_COLOR));
                    ingredientsList.getChildren().add(ingredientText);
                }
            }
        } catch (Exception e) {
            Text errorText = new Text("Error loading ingredients: " + e.getMessage());
            errorText.setFill(Color.web(DarkTheme.ERROR_COLOR));
            ingredientsList.getChildren().add(errorText);
        }
        
        ingredientsBox.getChildren().addAll(ingredientsTitle, ingredientsList);
        
        // Instructions section
        VBox instructionsBox = new VBox(10);
        
        Text instructionsTitle = new Text("Instructions");
        instructionsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        instructionsTitle.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        Text instructionsText = new Text(mealPost.getInstructions());
        instructionsText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        instructionsText.setWrappingWidth(700);
        
        instructionsBox.getChildren().addAll(instructionsTitle, instructionsText);
        
        // Comments section
        VBox commentsSection = new VBox(15);
        
        Text commentsTitle = new Text("Comments");
        commentsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        commentsTitle.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        // Comment input fields
        TextArea commentInput = new TextArea();
        commentInput.setPromptText("Add a comment...");
        commentInput.setStyle(DarkTheme.CSS_FIELD);
        commentInput.setPrefRowCount(3);
        
        Button submitCommentButton = new Button("Post Comment");
        submitCommentButton.setStyle(DarkTheme.CSS_BUTTON);
        
        submitCommentButton.setOnAction(e -> {
            String commentText = commentInput.getText().trim();
            if (!commentText.isEmpty()) {
                try {
                    boolean success = databaseHelper.addComment(currentUser.getId(), mealPost.getId(), commentText);
                    if (success) {
                        // Clear input and refresh comments
                        commentInput.clear();
                        loadComments();
                    } else {
                        showError("Failed to post comment.");
                    }
                } catch (SQLException ex) {
                    showError("Could not post comment: " + ex.getMessage());
                }
            }
        });
        
        // Container for existing comments
        commentsContainer = new VBox(10);
        loadComments();
        
        commentsSection.getChildren().addAll(commentsTitle, commentInput, submitCommentButton, commentsContainer);
        
        // Add all components to main container
        mainContainer.getChildren().addAll(
            topBar,
            titleText,
            metadataBox,
            descriptionBox,
            ingredientsBox,
            instructionsBox,
            commentsSection
        );
        
        scrollPane.setContent(mainContainer);
        Scene scene = new Scene(scrollPane, 800, 700);
        DarkTheme.applyTheme(scene);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Loads and displays the comments for the current meal post.
     */
    private void loadComments() {
        commentsContainer.getChildren().clear();
        
        try {
            List<Comment> comments = mealPost.getComments(databaseHelper);
            
            if (comments.isEmpty()) {
                Text noCommentsText = new Text("No comments yet. Be the first to comment!");
                noCommentsText.setFill(Color.web(DarkTheme.TEXT_COLOR));
                commentsContainer.getChildren().add(noCommentsText);
                return;
            }
            
            for (Comment comment : comments) {
                VBox commentBox = createCommentBox(comment);
                commentsContainer.getChildren().add(commentBox);
            }
        } catch (SQLException e) {
            Text errorText = new Text("Error loading comments: " + e.getMessage());
            errorText.setFill(Color.web(DarkTheme.ERROR_COLOR));
            commentsContainer.getChildren().add(errorText);
        }
    }
    
    /**
     * Creates a visual representation of a given comment in the form of a VBox.
     * The comment box displays the author of the comment, the time it was posted,
     * and the content of the comment. If the comment belongs to the current user
     * or the user has admin privileges, a delete button is also included.
     *
     * @param comment The Comment object representing the comment to be displayed.
     * @return A VBox containing the styled elements representing the comment.
     * @throws SQLException If there is an error retrieving the author of the comment.
     */
    private VBox createCommentBox(Comment comment) throws SQLException {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-background-color: " + DarkTheme.SECONDARY_COLOR + 
                           "; -fx-padding: 10; -fx-background-radius: 5;");
        
        // Get the comment author
        User author = databaseHelper.getUserById(comment.getUserId());
        String authorName = author != null ? author.getUsername() : "Unknown User";
        
        // Comment header with author and timestamp
        HBox commentHeader = new HBox();
        commentHeader.setAlignment(Pos.CENTER_LEFT);
        
        Text authorText = new Text(authorName);
        authorText.setFont(Font.font("System", FontWeight.BOLD, 14));
        authorText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Text dateText = new Text(comment.getTimeAgo());
        dateText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        dateText.setFont(Font.font("System", 12));
        
        commentHeader.getChildren().addAll(authorText, spacer, dateText);
        
        // Comment text
        Text commentText = new Text(comment.getContent());
        commentText.setFill(Color.web(DarkTheme.TEXT_COLOR));
        commentText.setWrappingWidth(700);
        
        // Add delete option if comment belongs to current user
        if (comment.getUserId() == currentUser.getId() || currentUser.isAdmin()) {
            Button deleteButton = new Button("Delete");
            deleteButton.setStyle(DarkTheme.CSS_BUTTON + "-fx-background-color: #a02020;");
            
            deleteButton.setOnAction(e -> {
                try {
                    boolean success = databaseHelper.deleteComment(comment.getId(), currentUser.getId());
                    if (success) {
                        loadComments(); // Refresh comments after delete
                    } else {
                        showError("Failed to delete comment.");
                    }
                } catch (SQLException ex) {
                    showError("Could not delete comment: " + ex.getMessage());
                }
            });
            
            commentBox.getChildren().addAll(commentHeader, commentText, deleteButton);
        } else {
            commentBox.getChildren().addAll(commentHeader, commentText);
        }
        
        return commentBox;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        DarkTheme.styleDialog(alert);
        alert.showAndWait();
    }
}