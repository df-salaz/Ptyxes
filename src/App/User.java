package App;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The User class represents a user in the Ptyxes meal planning system.
 * Users can create meal posts, upvote other posts, and comment on meals.
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private int role; // 0 = regular user, 1 = admin
    private int reputation;
    private String uuid;
    private LocalDateTime creationDate;
    
    // Constructor
    public User() {
        this.reputation = 0;
    }
    
    // Full constructor
    public User(int id, String username, String password, String email, int role, int reputation, String uuid, LocalDateTime creationDate) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.reputation = reputation;
        this.uuid = uuid;
        this.creationDate = creationDate;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public int getRole() {
        return role;
    }
    
    public void setRole(int role) {
        this.role = role;
    }
    
    public int getReputation() {
        return reputation;
    }
    
    public void setReputation(int reputation) {
        this.reputation = reputation;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    /**
     * Checks if the user is an admin
     * 
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        return role == 1;
    }
    
    /**
     * Formats the creation date in a user-friendly format
     * 
     * @return Formatted date string
     */
    public String getFormattedCreationDate() {
        if (creationDate == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        return creationDate.format(formatter);
    }
    
    /**
     * Gets user's membership duration in days
     * 
     * @return Number of days since account creation
     */
    public long getMembershipDays() {
        if (creationDate == null) {
            return 0;
        }
        return java.time.Duration.between(creationDate, LocalDateTime.now()).toDays();
    }
    
    /**
     * Creates a new meal post by this user
     * 
     * @param databaseHelper The database helper instance
     * @param title The meal title
     * @param description The meal description
     * @param instructions The cooking instructions
     * @param preparationTime Preparation time in minutes
     * @param cookingTime Cooking time in minutes
     * @param servings Number of servings
     * @param difficulty Difficulty level (e.g., "Easy", "Medium", "Hard")
     * @param ingredients List of ingredients
     * @return The created MealPost object, or null if creation failed
     */
    public MealPost createMealPost(
            DatabaseHelper databaseHelper,
            String title,
            String description,
            String instructions,
            int preparationTime,
            int cookingTime,
            int servings,
            String difficulty,
            List<MealIngredient> ingredients) throws SQLException {
        
        MealPost post = new MealPost();
        post.setUserId(this.id);
        post.setTitle(title);
        post.setDescription(description);
        post.setInstructions(instructions);
        post.setPreparationTime(preparationTime);
        post.setCookingTime(cookingTime);
        post.setServings(servings);
        post.setDifficulty(difficulty);
        post.setIngredients(ingredients != null ? ingredients : new ArrayList<>());
        
        return databaseHelper.createMealPost(post);
    }
    
    /**
     * Upvotes a meal post
     * 
     * @param databaseHelper The database helper instance
     * @param mealId The ID of the meal post to upvote
     * @return true if successful, false otherwise
     */
    public boolean upvoteMealPost(DatabaseHelper databaseHelper, int mealId) throws SQLException {
        return databaseHelper.upvoteMealPost(this.id, mealId);
    }
    
    /**
     * Removes an upvote from a meal post
     * 
     * @param databaseHelper The database helper instance
     * @param mealId The ID of the meal post to remove the upvote from
     * @return true if successful, false otherwise
     */
    public boolean removeUpvote(DatabaseHelper databaseHelper, int mealId) throws SQLException {
        return databaseHelper.removeUpvote(this.id, mealId);
    }
    
    /**
     * Adds a comment to a meal post
     * 
     * @param databaseHelper The database helper instance
     * @param mealId The ID of the meal post to comment on
     * @param content The comment content
     * @return true if successful, false otherwise
     */
    public boolean addComment(DatabaseHelper databaseHelper, int mealId, String content) throws SQLException {
        return databaseHelper.addComment(this.id, mealId, content);
    }
    
    /**
     * Deletes a comment
     * 
     * @param databaseHelper The database helper instance
     * @param commentId The ID of the comment to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteComment(DatabaseHelper databaseHelper, int commentId) throws SQLException {
        return databaseHelper.deleteComment(commentId, this.id);
    }
    
    /**
     * Gets all meal posts created by this user
     * 
     * @param databaseHelper The database helper instance
     * @param page The page number (0-based)
     * @param pageSize The number of posts per page
     * @return List of MealPost objects
     */
    public List<MealPost> getMyMealPosts(DatabaseHelper databaseHelper, int page, int pageSize) throws SQLException {
        return databaseHelper.getMealPostsByUser(this.id, page, pageSize);
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", reputation=" + reputation +
                ", joined=" + getFormattedCreationDate() +
                '}';
    }
}