package App;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

/**
 * The MainPage class displays meal posts after successful user login.
 * It includes a dashboard with meal posts, navigation, and user options.
 */
public class MainPage {
    
    private final DatabaseHelper databaseHelper;
    private final User currentUser;
    private static final String APP_TITLE = "Ptyxes - Meal Posts";
    private int currentPage = 0;
    private static final int PAGE_SIZE = 5;
    private String currentSearchQuery = "";
    private String currentDifficulty = "All";
    private String currentTimeFilter = "All";
    private String currentDietaryFilter = "All";
    private String currentSortMode = "Date"; // Default sort by date
    private VBox postsContainer;
    private Text pageText;
    private Button prevButton;
    private Button nextButton;
    
    public MainPage(DatabaseHelper databaseHelper, User currentUser) {
        this.databaseHelper = databaseHelper;
        this.currentUser = currentUser;
    }

    public void show(Stage primaryStage) {
        primaryStage.setTitle(APP_TITLE);
        
        // Main layout container
        BorderPane root = new BorderPane();
        root.setStyle(DarkTheme.CSS_BACKGROUND);
        
        // Create top navigation bar
        HBox topBar = createTopBar(primaryStage);
        root.setTop(topBar);
        
        // Create sidebar for filtering and options
        VBox sidebar = createSidebar(primaryStage);
        root.setLeft(sidebar);
        
        // Create main content area for meal posts
        ScrollPane contentArea = new ScrollPane();
        contentArea.setFitToWidth(true);
        contentArea.setStyle("-fx-background: " + DarkTheme.BACKGROUND_COLOR + "; -fx-border-color: " + DarkTheme.BACKGROUND_COLOR + ";");
        
        // Store reference to postsContainer
        this.postsContainer = new VBox(15);
        this.postsContainer.setPadding(new Insets(20));
        loadMealPosts();
        
        contentArea.setContent(postsContainer);
        root.setCenter(contentArea);
        
        // Create pagination controls at the bottom
        HBox paginationBar = createPaginationBar();
        root.setBottom(paginationBar);
        
        // Create the scene
        Scene scene = new Scene(root, 1000, 700);
        
        // Apply CSS styling
        DarkTheme.applyTheme(scene);
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Creates and configures the top bar of the application interface, including
     * title, search functionality, sorting options, and user profile section.
     *
     * @param primaryStage the primary stage of the application, used for window management
     * @return an HBox representing the top bar of the application
     */
    private HBox createTopBar(Stage primaryStage) {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15));
        topBar.setSpacing(20);
        topBar.setStyle("-fx-background-color: " + DarkTheme.SECONDARY_COLOR + ";");
        
        // App logo/title
        Text appTitle = new Text("Ptyxes");
        appTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        appTitle.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search recipes...");
        searchField.setStyle(DarkTheme.CSS_FIELD);
        searchField.setPrefWidth(300);
        
        Button searchButton = new Button("Search");
        searchButton.setStyle(DarkTheme.CSS_BUTTON);
        
        // Sort dropdown
        Label sortLabel = new Label("Sort by:");
        sortLabel.setStyle(DarkTheme.CSS_LABEL);
        
        ComboBox<String> sortComboBox = new ComboBox<>();
        sortComboBox.getItems().addAll("Date", "Reputation", "Preparation Time", "Cooking Time");
        sortComboBox.setValue(currentSortMode);
        sortComboBox.setStyle(DarkTheme.CSS_FIELD);
        
        // Add sort functionality
        sortComboBox.setOnAction(e -> {
            currentSortMode = sortComboBox.getValue();
            currentPage = 0;
            loadMealPosts();
            updatePaginationButtons();
        });
        
        // Search functionality
        searchButton.setOnAction(e -> {
            currentSearchQuery = searchField.getText().trim();
            currentPage = 0;
            loadMealPosts();
            updatePaginationButtons();
        });
        
        // Search on Enter key
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                currentSearchQuery = searchField.getText().trim();
                currentPage = 0;
                loadMealPosts();
                updatePaginationButtons();
            }
        });
        
        // Add hover effect
        searchButton.setOnMouseEntered(e -> searchButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        searchButton.setOnMouseExited(e -> searchButton.setStyle(DarkTheme.CSS_BUTTON));
        
        // User profile section
        HBox userSection = new HBox(10);
        userSection.setAlignment(Pos.CENTER_RIGHT);
        
        Label usernameLabel = new Label("Welcome, " + currentUser.getUsername() + "!");
        usernameLabel.setStyle(DarkTheme.CSS_LABEL);
        
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle(DarkTheme.CSS_BUTTON);
        
        // Add hover effect
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle(DarkTheme.CSS_BUTTON));
        
        // Add logout functionality
        logoutButton.setOnAction(e -> {
            FirstPage firstPage = new FirstPage(databaseHelper);
            firstPage.show(primaryStage);
        });
        
        // Add components to user section with auto-spacing
        userSection.getChildren().addAll(usernameLabel, logoutButton);
        
        // Use a Region to push the user section to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Add all components to the top bar
        topBar.getChildren().addAll(appTitle, searchField, searchButton, sortLabel, sortComboBox, spacer, userSection);
        
        return topBar;
    }
    
    /**
     * Creates and configures the sidebar of the application interface, including filter options
     * for difficulty, total time, dietary preferences, and user actions such as creating a new post
     * or viewing owned posts.
     *
     * @param primaryStage the primary stage of the application, used for managing window transitions
     * @return a VBox containing the sidebar layout with filters and action buttons
     */
    private VBox createSidebar(Stage primaryStage) {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: " + DarkTheme.SECONDARY_COLOR + ";");
        
        Text filterTitle = new Text("Filters");
        filterTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        filterTitle.setFill(Color.web(DarkTheme.TEXT_COLOR));
        
        // Difficulty filter
        VBox difficultyFilter = new VBox(5);
        Label difficultyLabel = new Label("Difficulty:");
        difficultyLabel.setStyle(DarkTheme.CSS_LABEL);
        
        ToggleGroup difficultyGroup = new ToggleGroup();
        
        RadioButton easyOption = new RadioButton("Easy");
        easyOption.setStyle(DarkTheme.CSS_LABEL);
        easyOption.setToggleGroup(difficultyGroup);
        
        RadioButton mediumOption = new RadioButton("Medium");
        mediumOption.setStyle(DarkTheme.CSS_LABEL);
        mediumOption.setToggleGroup(difficultyGroup);
        
        RadioButton hardOption = new RadioButton("Hard");
        hardOption.setStyle(DarkTheme.CSS_LABEL);
        hardOption.setToggleGroup(difficultyGroup);
        
        RadioButton allOption = new RadioButton("All");
        allOption.setStyle(DarkTheme.CSS_LABEL);
        allOption.setToggleGroup(difficultyGroup);
        allOption.setSelected(true);
        
        // Add filter change listeners
        difficultyGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selected = (RadioButton) newValue;
                currentDifficulty = selected.getText();
                currentPage = 0;
                loadMealPosts();
                updatePaginationButtons();
            }
        });
        
        difficultyFilter.getChildren().addAll(difficultyLabel, easyOption, mediumOption, hardOption, allOption);
        
        // Time filter
        VBox timeFilter = new VBox(5);
        Label timeLabel = new Label("Total Time:");
        timeLabel.setStyle(DarkTheme.CSS_LABEL);
        
        ToggleGroup timeGroup = new ToggleGroup();
        
        RadioButton quick = new RadioButton("Quick (<30 min)");
        quick.setStyle(DarkTheme.CSS_LABEL);
        quick.setToggleGroup(timeGroup);
        
        RadioButton medium = new RadioButton("Medium (30-60 min)");
        medium.setStyle(DarkTheme.CSS_LABEL);
        medium.setToggleGroup(timeGroup);
        
        RadioButton long_ = new RadioButton("Long (>60 min)");
        long_.setStyle(DarkTheme.CSS_LABEL);
        long_.setToggleGroup(timeGroup);
        
        RadioButton allTimes = new RadioButton("All");
        allTimes.setStyle(DarkTheme.CSS_LABEL);
        allTimes.setToggleGroup(timeGroup);
        allTimes.setSelected(true);
        
        timeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selected = (RadioButton) newValue;
                currentTimeFilter = selected.getText().split(" ")[0]; // Get just the first word (Quick, Medium, Long, All)
                currentPage = 0;
                loadMealPosts();
                updatePaginationButtons();
            }
        });
        
        timeFilter.getChildren().addAll(timeLabel, quick, medium, long_, allTimes);

        // Buttons for dietary filter
        VBox dietaryFilter = new VBox(5);
        Label dietaryLabel = new Label("Dietary Type:");
        dietaryLabel.setStyle(DarkTheme.CSS_LABEL);

        ToggleGroup dietaryGroup = new ToggleGroup();

        RadioButton veganOption = new RadioButton("Vegan");
        veganOption.setStyle(DarkTheme.CSS_LABEL);
        veganOption.setToggleGroup(dietaryGroup);

        RadioButton vegetarianOption = new RadioButton("Vegetarian");
        vegetarianOption.setStyle(DarkTheme.CSS_LABEL);
        vegetarianOption.setToggleGroup(dietaryGroup);

        RadioButton noneOption = new RadioButton("None");
        noneOption.setStyle(DarkTheme.CSS_LABEL);
        noneOption.setToggleGroup(dietaryGroup);

        RadioButton allDietaryOption = new RadioButton("All");
        allDietaryOption.setStyle(DarkTheme.CSS_LABEL);
        allDietaryOption.setToggleGroup(dietaryGroup);
        allDietaryOption.setSelected(true);

        dietaryGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RadioButton selected = (RadioButton) newValue;
                currentDietaryFilter = selected.getText();
                currentPage = 0;
                loadMealPosts();
                updatePaginationButtons();
            }
        });

        dietaryFilter.getChildren().addAll(dietaryLabel, veganOption, vegetarianOption, noneOption, allDietaryOption);
        
        // Buttons for user actions
        VBox userActions = new VBox(10);
        userActions.setPadding(new Insets(20, 0, 0, 0));
        
        Button newPostButton = new Button("Create New Post");
        newPostButton.setStyle(DarkTheme.CSS_BUTTON);
        newPostButton.setMaxWidth(Double.MAX_VALUE);
        
        newPostButton.setOnAction(e -> {
            CreatePostPage createPostPage = new CreatePostPage(databaseHelper, currentUser);
            createPostPage.show(primaryStage);
        });
        
        // Add hover effect
        newPostButton.setOnMouseEntered(e -> newPostButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        newPostButton.setOnMouseExited(e -> newPostButton.setStyle(DarkTheme.CSS_BUTTON));
        
        userActions.getChildren().add(newPostButton);
        
        // Add all components to the sidebar
        sidebar.getChildren().addAll(filterTitle, difficultyFilter, timeFilter, dietaryFilter, userActions);

        return sidebar;
    }

    /**
     * Creates an "at-a-glance" card representation of a meal post as a VBox UI element.
     * The card includes sections such as the title, metadata, description,
     * action buttons, and footer information about the author and comments.
     *
     * @param post the MealPost object containing the data
     * @return a VBox representing the complete layout of the card UI component
     */
    private VBox createPostCard(MealPost post) {
        VBox postCard = new VBox(10);
        postCard.setStyle("-fx-background-color: " + DarkTheme.SECONDARY_COLOR +
                "; -fx-padding: 15; -fx-background-radius: 5;");

        // Post header with title and upvotes
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text(post.getTitle());
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setFill(Color.web(DarkTheme.TEXT_COLOR));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox upvotesBox = new HBox(5);
        upvotesBox.setAlignment(Pos.CENTER);

        // Upvote button/icon
        Button upvoteButton = new Button("▲");
        upvoteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: " + DarkTheme.ACCENT_COLOR + ";");

        Text upvotesCount = new Text(String.valueOf(post.getUpvotes()));
        upvotesCount.setFill(Color.web(DarkTheme.TEXT_COLOR));

        upvotesBox.getChildren().addAll(upvoteButton, upvotesCount);

        header.getChildren().addAll(title, spacer, upvotesBox);

        // Post metadata (preparation time, cooking time, difficulty)
        HBox metadata = new HBox(15);
        metadata.setAlignment(Pos.CENTER_LEFT);

        Label prepTime = new Label("Prep: " + post.getPreparationTime() + " min");
        prepTime.setStyle(DarkTheme.CSS_LABEL);

        Label cookTime = new Label("Cook: " + post.getCookingTime() + " min");
        cookTime.setStyle(DarkTheme.CSS_LABEL);

        Label difficulty = new Label("Difficulty: " + post.getDifficulty());
        difficulty.setStyle(DarkTheme.CSS_LABEL);

        Label servings = new Label("Servings: " + post.getServings());
        servings.setStyle(DarkTheme.CSS_LABEL);

        // Add dietary type label with specific colors
        Label dietaryType = new Label(post.getDietaryType());
        dietaryType.setStyle(DarkTheme.CSS_LABEL);

        metadata.getChildren().addAll(prepTime, cookTime, difficulty, servings, dietaryType);

        // Post description
        Text description = new Text(post.getDescription());
        description.setFill(Color.web(DarkTheme.TEXT_COLOR));
        description.setWrappingWidth(700);

        // Button container for recipe and delete buttons
        HBox buttonContainer = new HBox(10);
        buttonContainer.setAlignment(Pos.CENTER_LEFT);

        // Button to view full recipe
        Button viewRecipeButton = new Button("View Full Recipe");
        viewRecipeButton.setStyle(DarkTheme.CSS_BUTTON);

        // Add hover effect
        viewRecipeButton.setOnMouseEntered(e -> viewRecipeButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        viewRecipeButton.setOnMouseExited(e -> viewRecipeButton.setStyle(DarkTheme.CSS_BUTTON));

        viewRecipeButton.setOnAction(e -> {
            RecipeDetailPage detailPage = new RecipeDetailPage(databaseHelper, currentUser, post);
            detailPage.show((Stage) viewRecipeButton.getScene().getWindow());
        });

        buttonContainer.getChildren().add(viewRecipeButton);

        // Add delete button if the post belongs to the current user or if the user is an administrator
        if (post.getUserId() == currentUser.getId() || currentUser.isAdmin()) {
            Button deleteButton = new Button("Delete Post");
            deleteButton.setStyle(DarkTheme.CSS_BUTTON + "-fx-background-color: #a02020;");

            // Add hover effect for delete button
            deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(DarkTheme.CSS_BUTTON + "-fx-background-color: #c02020;"));
            deleteButton.setOnMouseExited(e -> deleteButton.setStyle(DarkTheme.CSS_BUTTON + "-fx-background-color: #a02020;"));

            // Add click functionality to the delete button
            deleteButton.setOnAction(e -> {
                // Show confirmation dialog
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Delete Post");
                confirmDialog.setHeaderText("Are you sure you want to delete this post?");
                confirmDialog.setContentText("This action cannot be undone.");

                // Apply dark theme to the dialog
                DarkTheme.styleDialog(confirmDialog);

                // Process the result
                confirmDialog.showAndWait().ifPresent(result -> {
                    if (result == ButtonType.OK) {
                        try {
                            boolean success = post.delete(databaseHelper);
                            if (success) {
                                // Refresh the posts list
                                loadMealPosts();
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Post deleted successfully.");
                            } else {
                                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete the post.");
                            }
                        } catch (SQLException ex) {
                            showAlert(Alert.AlertType.ERROR, "Error", "Could not delete post: " + ex.getMessage());
                        }
                    }
                });
            });

            buttonContainer.getChildren().add(deleteButton);
        }

        // Post footer with creation date and author
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        try {
            User postAuthor = databaseHelper.getUserById(post.getUserId());
            String authorName = postAuthor != null ? postAuthor.getUsername() : "Unknown";

            Text postInfo = new Text("Posted by " + authorName + " on " + post.getFormattedCreationDate());
            postInfo.setFill(Color.web(DarkTheme.TEXT_COLOR));
            postInfo.setFont(Font.font("System", 12));

            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);

            // Comment count
            List<Comment> comments = post.getComments(databaseHelper);
            Text commentCount = new Text(comments.size() + " comment" + (comments.size() != 1 ? "s" : ""));
            commentCount.setFill(Color.web(DarkTheme.TEXT_COLOR));
            commentCount.setFont(Font.font("System", 12));

            footer.getChildren().addAll(postInfo, footerSpacer, commentCount);

        } catch (SQLException e) {
            Text errorText = new Text("Error loading post details: " + e.getMessage());
            errorText.setFill(Color.web(DarkTheme.ERROR_COLOR));
            footer.getChildren().add(errorText);
        }

        // Add all components to the post card
        postCard.getChildren().addAll(header, metadata, description, buttonContainer, footer);

        // Add click functionality to the upvote button
        upvoteButton.setOnAction(e -> {
            try {
                boolean success = databaseHelper.upvoteMealPost(currentUser.getId(), post.getId());
                if (success) {
                    post.setUpvotes(post.getUpvotes() + 1);
                    upvotesCount.setText(String.valueOf(post.getUpvotes()));
                }
            } catch (SQLException ex) {
                // Show error
                showAlert(Alert.AlertType.ERROR, "Error", "Could not upvote: " + ex.getMessage());
            }
        });

        return postCard;
    }

    /**
     * Creates and configures a pagination bar for navigating through pages of meal posts.
     * The pagination bar includes "Previous" and "Next" buttons, as well as a text label
     * to display the current page number.
     *
     * @return an HBox representing the pagination bar with navigation functionality.
     */
    private HBox createPaginationBar() {
        HBox paginationBar = new HBox(10);
        paginationBar.setAlignment(Pos.CENTER);
        paginationBar.setPadding(new Insets(15));
        paginationBar.setStyle("-fx-background-color: " + DarkTheme.SECONDARY_COLOR + ";");
        
        prevButton = new Button("Previous");
        prevButton.setStyle(DarkTheme.CSS_BUTTON);
        
        // Add hover effect
        prevButton.setOnMouseEntered(e -> prevButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        prevButton.setOnMouseExited(e -> prevButton.setStyle(DarkTheme.CSS_BUTTON));
        
        pageText = new Text("Page " + (currentPage + 1));
        pageText.setFill(Color.web(DarkTheme.TEXT_COLOR));

        nextButton = new Button("Next");
        nextButton.setStyle(DarkTheme.CSS_BUTTON);
        
        // Add hover effect
        nextButton.setOnMouseEntered(e -> nextButton.setStyle(DarkTheme.CSS_BUTTON + DarkTheme.CSS_BUTTON_HOVER));
        nextButton.setOnMouseExited(e -> nextButton.setStyle(DarkTheme.CSS_BUTTON));
        
        // Update button states based on available content
        updatePaginationButtons();
        
        // Add pagination functionality
        prevButton.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                pageText.setText("Page " + (currentPage + 1));
                loadMealPosts();
                updatePaginationButtons(); // Update buttons after page change
            }
        });
        
        nextButton.setOnAction(e -> {
            currentPage++;
            pageText.setText("Page " + (currentPage + 1));
            loadMealPosts();
            updatePaginationButtons(); // Update buttons after page change
        });
        
        paginationBar.getChildren().addAll(prevButton, pageText, nextButton);
        
        return paginationBar;
    }
    
    /**
     * Displays an alert dialog with the specified type, title, and message.
     *
     * @param alertType the type of the alert (e.g., information, warning, error)
     * @param title the title of the alert dialog
     * @param message the content message displayed in the alert dialog
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Apply dark theme to the dialog
        DarkTheme.styleDialog(alert);
        
        alert.showAndWait();
    }

    private void loadMealPosts() {
        postsContainer.getChildren().clear();
        
        try {
            List<MealPost> posts = databaseHelper.searchAndFilterMealPosts(
                currentSearchQuery,
                currentDifficulty,
                currentTimeFilter,
                currentDietaryFilter,
                currentSortMode, // Pass the sort mode to the database helper
                currentPage,
                PAGE_SIZE
            );
            
            if (posts.isEmpty()) {
                Text noPostsText = new Text("No meal posts found.");
                noPostsText.setFill(Color.web(DarkTheme.TEXT_COLOR));
                postsContainer.getChildren().add(noPostsText);
                return;
            }
            
            for (MealPost post : posts) {
                VBox postCard = createPostCard(post);
                postsContainer.getChildren().add(postCard);
            }
            
        } catch (SQLException e) {
            Text errorText = new Text("Error loading posts: " + e.getMessage());
            errorText.setFill(Color.web(DarkTheme.ERROR_COLOR));
            postsContainer.getChildren().add(errorText);
        }
    }

    /**
     * Updates the state of the pagination buttons and page text based on the current page
     * and the total number of posts available after applying filters.
     */
    private void updatePaginationButtons() {
        try {
            int totalPosts = databaseHelper.getFilteredPostsCount(
                    currentSearchQuery,
                    currentDifficulty,
                    currentTimeFilter,
                    currentDietaryFilter
            );
            int totalPages = (int) Math.ceil((double) totalPosts / PAGE_SIZE);

            prevButton.setDisable(currentPage == 0);
            nextButton.setDisable(currentPage >= totalPages - 1);
            pageText.setText("Page " + (currentPage + 1));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not update pagination buttons: " + e.getMessage());
        }
    }
}