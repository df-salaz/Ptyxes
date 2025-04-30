package App;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * The MealPost class represents a meal post in the Ptyxes system.
 * It contains details about the meal, including title, description,
 * instructions, preparation and cooking times, ingredients, and upvotes.
 */
public class MealPost {
    private int id;
    private String title;
    private int userId;
    private String description;
    private String instructions;
    private int preparationTime; // in minutes
    private int cookingTime; // in minutes
    private int servings;
    private String difficulty;
    private String imageUrl;
    private int upvotes;
    private LocalDateTime creationDate;
    private LocalDateTime lastModified;
    private List<MealIngredient> ingredients;
    
    // Constructor
    public MealPost() {
        this.ingredients = new ArrayList<>();
        this.upvotes = 0;
    }
    
    // Full constructor
    public MealPost(int id, String title, int userId, String description, String instructions,
                   int preparationTime, int cookingTime, int servings, String difficulty,
                   String imageUrl, int upvotes, LocalDateTime creationDate, LocalDateTime lastModified) {
        this.id = id;
        this.title = title;
        this.userId = userId;
        this.description = description;
        this.instructions = instructions;
        this.preparationTime = preparationTime;
        this.cookingTime = cookingTime;
        this.servings = servings;
        this.difficulty = difficulty;
        this.imageUrl = imageUrl;
        this.upvotes = upvotes;
        this.creationDate = creationDate;
        this.lastModified = lastModified;
        this.ingredients = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public int getPreparationTime() {
        return preparationTime;
    }
    
    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }
    
    public int getCookingTime() {
        return cookingTime;
    }
    
    public void setCookingTime(int cookingTime) {
        this.cookingTime = cookingTime;
    }
    
    public int getServings() {
        return servings;
    }
    
    public void setServings(int servings) {
        this.servings = servings;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public int getUpvotes() {
        return upvotes;
    }
    
    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    public List<MealIngredient> getIngredients() {
        return ingredients;
    }
    
    public void setIngredients(List<MealIngredient> ingredients) {
        this.ingredients = ingredients;
    }
    
    /**
     * Adds an ingredient to this meal post
     * 
     * @param ingredient The MealIngredient to add
     */
    public void addIngredient(MealIngredient ingredient) {
        if (this.ingredients == null) {
            this.ingredients = new ArrayList<>();
        }
        this.ingredients.add(ingredient);
    }
    
    /**
     * Gets the total preparation and cooking time
     * 
     * @return Total time in minutes
     */
    public int getTotalTime() {
        return preparationTime + cookingTime;
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
     * Formats the last modified date in a user-friendly format
     * 
     * @return Formatted date string
     */
    public String getFormattedLastModified() {
        if (lastModified == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        return lastModified.format(formatter);
    }
    
    /**
     * Updates this meal post in the database
     * 
     * @param databaseHelper The database helper instance
     * @return true if successful, false otherwise
     */
    public boolean update(DatabaseHelper databaseHelper) throws SQLException {
        return databaseHelper.updateMealPost(this);
    }
    
    /**
     * Deletes this meal post from the database
     * 
     * @param databaseHelper The database helper instance
     * @return true if successful, false otherwise
     */
    public boolean delete(DatabaseHelper databaseHelper) throws SQLException {
        return databaseHelper.deleteMealPost(this.id);
    }
    
    /**
     * Gets comments for this meal post
     * 
     * @param databaseHelper The database helper instance
     * @return List of Comment objects
     */
    public List<Comment> getComments(DatabaseHelper databaseHelper) throws SQLException {
        return databaseHelper.getCommentsForMeal(this.id);
    }
    
    @Override
    public String toString() {
        return "MealPost{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", userId=" + userId +
                ", preparationTime=" + preparationTime +
                ", cookingTime=" + cookingTime +
                ", servings=" + servings +
                ", difficulty='" + difficulty + '\'' +
                ", upvotes=" + upvotes +
                ", ingredients=" + (ingredients != null ? ingredients.size() : 0) +
                ", created=" + getFormattedCreationDate() +
                '}';
    }
}