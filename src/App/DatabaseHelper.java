package App;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The DatabaseHelper class is responsible for managing the connection to the
 * database, performing operations such as user registration, login validation,
 * and handling meal posts with ingredients and recipes.
 */
public class DatabaseHelper {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static String DB_URL = "jdbc:h2:./Ptyxes";

    // Database credentials
    static final String USER = "sa";
    static final String PASS = "";

    private Connection connection = null;
    private Statement statement = null;

    public DatabaseHelper() {
        try {
            connectToDatabase();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public DatabaseHelper(String inputDataBase) {
        DB_URL = inputDataBase;
        try {
            connectToDatabase();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public int ResetHard(String URL) {
        if (URL.equals(DB_URL)) {
            System.out.println("WARNING: Attempting to reset the database using DROP ALL OBJECTS...");
            try {
                // Use H2's specific command for dropping everything
                statement.execute("DROP ALL OBJECTS");
                System.out.println("Database reset successfully. All objects dropped.");
                createTables();
            } catch (SQLException e) {
                System.err.println("Error during database reset: " + e.getMessage());
            }
            return 0;
        }
        return 1;
    }

    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            System.out.println("Connecting to database...");
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement();
            // You can use this command to clear the database and restart from fresh.
            // statement.execute("DROP ALL OBJECTS");

            createTables();
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        // Users table
        String userTable = "CREATE TABLE IF NOT EXISTS users (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, " 
                + "password VARCHAR(255), " 
                + "role INT, " 
                + "reputation INT, "
                + "email VARCHAR(255), "
                + "creationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "uuid VARCHAR(36) UNIQUE)";
        statement.execute(userTable);

        // Meal posts table
        String mealPostTable = "CREATE TABLE IF NOT EXISTS meal_posts ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "title VARCHAR(255), "
                + "userId INT, "
                + "description TEXT, "
                + "instructions TEXT, "
                + "preparationTime INT, "
                + "cookingTime INT, "
                + "servings INT, " 
                + "difficulty VARCHAR(50), "
                + "dietaryType VARCHAR(50), "
                + "imageUrl VARCHAR(255), "
                + "creationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "lastModified TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "upvotes INT DEFAULT 0, "
                + "FOREIGN KEY (userId) REFERENCES users(id))";
        statement.execute(mealPostTable);

        // Ingredients table
        String ingredientsTable = "CREATE TABLE IF NOT EXISTS ingredients (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "name VARCHAR(255), " 
                + "category VARCHAR(100))";
        statement.execute(ingredientsTable);

        // Meal ingredients junction table
        String mealIngredientsTable = "CREATE TABLE IF NOT EXISTS meal_ingredients (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "mealId INT, " 
                + "ingredientId INT, " 
                + "quantity FLOAT, " 
                + "unit VARCHAR(50), "
                + "FOREIGN KEY (mealId) REFERENCES meal_posts(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (ingredientId) REFERENCES ingredients(id))";
        statement.execute(mealIngredientsTable);

        // Upvotes table (to track who upvoted what)
        String upvotesTable = "CREATE TABLE IF NOT EXISTS upvotes (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userId INT, " 
                + "mealId INT, " 
                + "upvoteDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (userId) REFERENCES users(id), "
                + "FOREIGN KEY (mealId) REFERENCES meal_posts(id) ON DELETE CASCADE, "
                + "UNIQUE(userId, mealId))";
        statement.execute(upvotesTable);

        // Comments table
        String commentsTable = "CREATE TABLE IF NOT EXISTS comments (" 
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userId INT, " 
                + "mealId INT, " 
                + "content TEXT, " 
                + "creationDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (userId) REFERENCES users(id), "
                + "FOREIGN KEY (mealId) REFERENCES meal_posts(id) ON DELETE CASCADE)";
        statement.execute(commentsTable);
    }

    // User Management Methods

    /**
     * Authenticates a user based on username and password
     * 
     * @param username The username
     * @param password The password
     * @return User object if authenticated, null otherwise
     */
    public User authenticateUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE userName = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In a real app, use password hashing
            
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
        }
        return null;
    }

    /**
     * Creates a new user account
     * 
     * @param username The username
     * @param password The password
     * @param email The email address
     * @param role User role (e.g., 0 for regular user, 1 for admin)
     * @return The created User object, or null if creation failed
     */
    public User createUser(String username, String password, String email, int role) throws SQLException {
        String uuid = UUID.randomUUID().toString();
        String query = "INSERT INTO users (userName, password, email, role, reputation, uuid) VALUES (?, ?, ?, ?, 0, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In a real app, use password hashing
            pstmt.setString(3, email);
            pstmt.setInt(4, role);
            pstmt.setString(5, uuid);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int userId = generatedKeys.getInt(1);
                        return getUserById(userId);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Deletes a user account and all associated data
     * 
     * @param userId The ID of the user to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteUser(int userId) throws SQLException {
        // Start a transaction to ensure data integrity
        connection.setAutoCommit(false);
        try {
            // First remove upvotes by this user
            String deleteUpvotes = "DELETE FROM upvotes WHERE userId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteUpvotes)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
            
            // Remove comments by this user
            String deleteComments = "DELETE FROM comments WHERE userId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteComments)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
            
            // Get all meal posts by this user to handle their deletion
            List<Integer> userMealIds = new ArrayList<>();
            String getMealIds = "SELECT id FROM meal_posts WHERE userId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getMealIds)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    userMealIds.add(rs.getInt("id"));
                }
            }
            
            // Delete meal ingredients for each meal post
            for (Integer mealId : userMealIds) {
                String deleteMealIngredients = "DELETE FROM meal_ingredients WHERE mealId = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(deleteMealIngredients)) {
                    pstmt.setInt(1, mealId);
                    pstmt.executeUpdate();
                }
            }
            
            // Delete the meal posts
            String deleteMeals = "DELETE FROM meal_posts WHERE userId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteMeals)) {
                pstmt.setInt(1, userId);
                pstmt.executeUpdate();
            }
            
            // Finally, delete the user
            String deleteUser = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteUser)) {
                pstmt.setInt(1, userId);
                int affectedRows = pstmt.executeUpdate();
                
                // Commit the transaction if everything succeeded
                connection.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            // Rollback the transaction if anything fails
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Updates user profile information
     * 
     * @param userId User ID
     * @param password New password (can be null if not updating)
     * @param email New email (can be null if not updating)
     * @return true if successful, false otherwise
     */
    public boolean updateUser(int userId, String password, String email) throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE users SET ");
        List<Object> params = new ArrayList<>();
        
        boolean hasPassword = password != null && !password.isEmpty();
        boolean hasEmail = email != null && !email.isEmpty();
        
        if (hasPassword) {
            query.append("password = ?");
            params.add(password); // In a real app, use password hashing
        }
        
        if (hasPassword && hasEmail) {
            query.append(", ");
        }
        
        if (hasEmail) {
            query.append("email = ?");
            params.add(email);
        }
        
        query.append(" WHERE id = ?");
        params.add(userId);
        
        try (PreparedStatement pstmt = connection.prepareStatement(query.toString())) {
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Retrieves a user by ID
     * 
     * @param userId The user ID
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
        }
        return null;
    }

    /**
     * Retrieves a user by username
     * 
     * @param username The username
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) throws SQLException {
        String query = "SELECT * FROM users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, username);
            
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
        }
        return null;
    }

    /**
     * Updates a user's reputation
     * 
     * @param userId The user ID
     * @param reputationChange The change in reputation (positive or negative)
     * @return true if successful, false otherwise
     */
    public boolean updateUserReputation(int userId, int reputationChange) throws SQLException {
        String query = "UPDATE users SET reputation = reputation + ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, reputationChange);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Helper method to construct User objects from ResultSet
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("userName"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getInt("role"));
        user.setReputation(rs.getInt("reputation"));
        user.setUuid(rs.getString("uuid"));
        if (rs.getTimestamp("creationDate") != null) {
            user.setCreationDate(rs.getTimestamp("creationDate").toLocalDateTime());
        }
        return user;
    }

    // Meal Post Management Methods

    /**
     * Creates a new meal post
     * 
     * @param post The MealPost object to create
     * @return The created MealPost with ID set, or null if creation failed
     */
    public MealPost createMealPost(MealPost post) throws SQLException {
        String sql = "INSERT INTO meal_posts (title, userId, description, instructions, " +
                "preparationTime, cookingTime, servings, difficulty, dietaryType, " +
                "imageUrl, upvotes, creationDate, lastModified) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW())";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, post.getTitle());
            pstmt.setInt(2, post.getUserId());
            pstmt.setString(3, post.getDescription());
            pstmt.setString(4, post.getInstructions());
            pstmt.setInt(5, post.getPreparationTime());
            pstmt.setInt(6, post.getCookingTime());
            pstmt.setInt(7, post.getServings());
            pstmt.setString(8, post.getDifficulty());
            pstmt.setString(9, post.getDietaryType());
            pstmt.setString(10, post.getImageUrl());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        post.setId(generatedKeys.getInt(1));

                        // Save ingredients if any
                        if (post.getIngredients() != null && !post.getIngredients().isEmpty()) {
                            for (MealIngredient ingredient : post.getIngredients()) {
                                addIngredientToMeal(post.getId(), ingredient);
                            }
                        }

                        return post;
                    }
                }
            }
            return null;
        }
    }

    /**
     * Adds an ingredient to a meal post
     * 
     * @param mealId The meal post ID
     * @param ingredient The ingredient to add
     * @return true if successful, false otherwise
     */
    private boolean addIngredientToMeal(int mealId, MealIngredient ingredient) throws SQLException {
        // First check if the ingredient exists, if not create it
        int ingredientId = getOrCreateIngredient(ingredient.getName(), ingredient.getCategory());
        
        // Now add the connection in the junction table
        String query = "INSERT INTO meal_ingredients (mealId, ingredientId, quantity, unit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, mealId);
            pstmt.setInt(2, ingredientId);
            pstmt.setFloat(3, ingredient.getQuantity());
            pstmt.setString(4, ingredient.getUnit());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Gets or creates an ingredient
     * 
     * @param name The ingredient name
     * @param category The ingredient category
     * @return The ingredient ID
     */
    private int getOrCreateIngredient(String name, String category) throws SQLException {
        // First check if the ingredient exists
        String checkQuery = "SELECT id FROM ingredients WHERE name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkQuery)) {
            pstmt.setString(1, name);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        
        // If not, create it
        String insertQuery = "INSERT INTO ingredients (name, category) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        
        throw new SQLException("Failed to get or create ingredient");
    }

    /**
     * Updates an existing meal post
     * 
     * @param post The MealPost object with updated data
     * @return true if successful, false otherwise
     */
    public boolean updateMealPost(MealPost post) throws SQLException {
        // Start a transaction to ensure data integrity
        connection.setAutoCommit(false);
        try {
            // Update the meal post
            String sql = "UPDATE meal_posts SET title = ?, description = ?, instructions = ?, " +
                    "preparationTime = ?, cookingTime = ?, servings = ?, difficulty = ?, " +
                    "dietaryType = ?, imageUrl = ?, lastModified = NOW() " +
                    "WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, post.getTitle());
                pstmt.setString(2, post.getDescription());
                pstmt.setString(3, post.getInstructions());
                pstmt.setInt(4, post.getPreparationTime());
                pstmt.setInt(5, post.getCookingTime());
                pstmt.setInt(6, post.getServings());
                pstmt.setString(7, post.getDifficulty());
                pstmt.setString(8, post.getDietaryType());
                pstmt.setString(9, post.getImageUrl());
                pstmt.setInt(10, post.getId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    // Clear existing ingredients
                    String deleteIngredients = "DELETE FROM meal_ingredients WHERE mealId = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteIngredients)) {
                        deleteStmt.setInt(1, post.getId());
                        deleteStmt.executeUpdate();
                    }
                    
                    // Add updated ingredients
                    for (MealIngredient ingredient : post.getIngredients()) {
                        addIngredientToMeal(post.getId(), ingredient);
                    }
                    
                    // Commit the transaction
                    connection.commit();
                    return true;
                }
            }
            // If we get here, something went wrong
            connection.rollback();
            return false;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Deletes a meal post and all associated data
     * 
     * @param mealId The ID of the meal post to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteMealPost(int mealId) throws SQLException {
        // Start a transaction to ensure data integrity
        connection.setAutoCommit(false);
        try {
            // Delete upvotes for this meal
            String deleteUpvotes = "DELETE FROM upvotes WHERE mealId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteUpvotes)) {
                pstmt.setInt(1, mealId);
                pstmt.executeUpdate();
            }
            
            // Delete comments for this meal
            String deleteComments = "DELETE FROM comments WHERE mealId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteComments)) {
                pstmt.setInt(1, mealId);
                pstmt.executeUpdate();
            }
            
            // Delete meal ingredients
            String deleteIngredients = "DELETE FROM meal_ingredients WHERE mealId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteIngredients)) {
                pstmt.setInt(1, mealId);
                pstmt.executeUpdate();
            }
            
            // Delete the meal post
            String deleteMeal = "DELETE FROM meal_posts WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteMeal)) {
                pstmt.setInt(1, mealId);
                int affectedRows = pstmt.executeUpdate();
                
                // Commit the transaction
                connection.commit();
                return affectedRows > 0;
            }
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Retrieves a meal post by ID with all its ingredients
     * 
     * @param mealId The meal post ID
     * @return MealPost object if found, null otherwise
     */
    public MealPost getMealPostById(int mealId) throws SQLException {
        String query = "SELECT * FROM meal_posts WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, mealId);
            
            ResultSet resultSet = pstmt.executeQuery();
            if (resultSet.next()) {
                MealPost post = extractMealPostFromResultSet(resultSet);
                
                // Load ingredients for this meal
                post.setIngredients(getIngredientsForMeal(mealId));
                
                return post;
            }
        }
        return null;
    }

    /**
     * Gets all ingredients for a meal
     * 
     * @param mealId The meal ID
     * @return List of MealIngredient objects
     */
    private List<MealIngredient> getIngredientsForMeal(int mealId) throws SQLException {
        List<MealIngredient> ingredients = new ArrayList<>();
        
        String query = "SELECT mi.quantity, mi.unit, i.id, i.name, i.category " 
                + "FROM meal_ingredients mi "
                + "JOIN ingredients i ON mi.ingredientId = i.id "
                + "WHERE mi.mealId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, mealId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MealIngredient ingredient = new MealIngredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setCategory(rs.getString("category"));
                ingredient.setQuantity(rs.getFloat("quantity"));
                ingredient.setUnit(rs.getString("unit"));
                
                ingredients.add(ingredient);
            }
        }
        
        return ingredients;
    }

    /**
     * Gets all meal posts, with pagination support
     * 
     * @param page The page number (0-based)
     * @param pageSize The number of posts per page
     * @return List of MealPost objects
     */
    public List<MealPost> getAllMealPosts(int page, int pageSize) throws SQLException {
        List<MealPost> posts = new ArrayList<>();
        
        String query = "SELECT * FROM meal_posts ORDER BY creationDate DESC LIMIT ? OFFSET ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, page * pageSize);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MealPost post = extractMealPostFromResultSet(rs);
                // Optionally load ingredients if needed
                // post.setIngredients(getIngredientsForMeal(post.getId()));
                posts.add(post);
            }
        }
        
        return posts;
    }

    /**
     * Gets the total number of meal posts
     *
     * @return Number of MealPosts
     */
    public int getTotalPostsCount() throws SQLException {
        String query = "SELECT COUNT(*) FROM meal_posts";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Gets meal posts by a specific user
     * 
     * @param userId The user ID
     * @param page The page number (0-based)
     * @param pageSize The number of posts per page
     * @return List of MealPost objects
     */
    public List<MealPost> getMealPostsByUser(int userId, int page, int pageSize) throws SQLException {
        List<MealPost> posts = new ArrayList<>();
        
        String query = "SELECT * FROM meal_posts WHERE userId = ? ORDER BY creationDate DESC LIMIT ? OFFSET ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, pageSize);
            pstmt.setInt(3, page * pageSize);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MealPost post = extractMealPostFromResultSet(rs);
                // Optionally load ingredients if needed
                // post.setIngredients(getIngredientsForMeal(post.getId()));
                posts.add(post);
            }
        }
        
        return posts;
    }

    /**
     * Searches for meal posts by title, description, or ingredients
     * 
     * @param searchTerm The search term
     * @param page The page number (0-based)
     * @param pageSize The number of posts per page
     * @return List of MealPost objects
     */
    public List<MealPost> searchMealPosts(String searchTerm, int page, int pageSize) throws SQLException {
        List<MealPost> posts = new ArrayList<>();
        
        String query = "SELECT DISTINCT mp.* FROM meal_posts mp " 
                + "LEFT JOIN meal_ingredients mi ON mp.id = mi.mealId "
                + "LEFT JOIN ingredients i ON mi.ingredientId = i.id "
                + "WHERE mp.title LIKE ? OR mp.description LIKE ? OR i.name LIKE ? "
                + "ORDER BY mp.upvotes DESC, mp.creationDate DESC LIMIT ? OFFSET ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            String term = "%" + searchTerm + "%";
            pstmt.setString(1, term);
            pstmt.setString(2, term);
            pstmt.setString(3, term);
            pstmt.setInt(4, pageSize);
            pstmt.setInt(5, page * pageSize);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                MealPost post = extractMealPostFromResultSet(rs);
                // Optionally load ingredients if needed
                // post.setIngredients(getIngredientsForMeal(post.getId()));
                posts.add(post);
            }
        }
        
        return posts;
    }

    // Helper method to construct MealPost objects from ResultSet
    private MealPost extractMealPostFromResultSet(ResultSet rs) throws SQLException {
        MealPost post = new MealPost();
        post.setId(rs.getInt("id"));
        post.setTitle(rs.getString("title"));
        post.setUserId(rs.getInt("userId"));
        post.setDescription(rs.getString("description"));
        post.setInstructions(rs.getString("instructions"));
        post.setPreparationTime(rs.getInt("preparationTime"));
        post.setCookingTime(rs.getInt("cookingTime"));
        post.setServings(rs.getInt("servings"));
        post.setDifficulty(rs.getString("difficulty"));
        post.setImageUrl(rs.getString("imageUrl"));
        post.setUpvotes(rs.getInt("upvotes"));
        post.setDietaryType(rs.getString("dietaryType"));
        if (rs.getTimestamp("creationDate") != null) {
            post.setCreationDate(rs.getTimestamp("creationDate").toLocalDateTime());
        }
        if (rs.getTimestamp("lastModified") != null) {
            post.setLastModified(rs.getTimestamp("lastModified").toLocalDateTime());
        }
        return post;
    }

    /**
     * Searches and filters meal posts based on provided criteria such as search query, difficulty level, time filters, and pagination settings.
     *
     * @param query The search text to filter meal posts by title or description (case-insensitive).
     * @param difficulty The difficulty level to filter meal posts (e.g., "Easy", "Medium", "Hard"). Use "All" to ignore this filter.
     * @param timeFilter The time category for filtering based on total preparation and cooking time ("Quick", "Medium", "Long"). Use "All" to ignore this filter.
     * @param page The page number for paginated results (0-based).
     * @param pageSize The number of meal posts per page.
     * @return A list of filtered and paginated MealPost objects matching the criteria.
     */
    public List<MealPost> searchAndFilterMealPosts(
            String query, String difficulty, String timeFilter, String dietaryFilter,
            String sortMode, int page, int pageSize) throws SQLException {

        List<MealPost> results = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.*, u.reputation FROM meal_posts p ");
        sql.append("LEFT JOIN users u ON p.userId = u.id ");
        sql.append("WHERE 1=1 ");

        // Add search condition if query is not empty
        if (query != null && !query.isEmpty()) {
            sql.append("AND (LOWER(p.title) LIKE ? OR LOWER(p.description) LIKE ?) ");
        }

        // Add difficulty filter if not "All"
        if (difficulty != null && !difficulty.equals("All")) {
            sql.append("AND p.difficulty = ? ");
        }

        // Add time filter
        if (timeFilter != null && !timeFilter.equals("All")) {
            switch (timeFilter) {
                case "Quick" -> sql.append("AND (p.preparationTime + p.cookingTime) < 30 ");
                case "Medium" -> sql.append("AND (p.preparationTime + p.cookingTime) BETWEEN 30 AND 60 ");
                case "Long" -> sql.append("AND (p.preparationTime + p.cookingTime) > 60 ");
            }
        }

        if (dietaryFilter != null && !dietaryFilter.equals("All")) {
            if (dietaryFilter.equals("Vegetarian")) {
                sql.append("AND p.dietaryType LIKE ? OR p.dietaryType LIKE 'Vegan' ");
            } else {
                sql.append("AND p.dietaryType LIKE ? ");
            }
        }

        // Add sorting
        if (sortMode != null) {
            switch (sortMode) {
                case "Reputation":
                    sql.append("ORDER BY u.reputation DESC ");
                    break;
                case "Preparation Time":
                    sql.append("ORDER BY p.preparationTime ASC, p.creationDate DESC ");
                    break;
                case "Cooking Time":
                    sql.append("ORDER BY p.cookingTime ASC, p.creationDate DESC ");
                    break;
                case "Date":
                default:
                    sql.append("ORDER BY p.creationDate DESC ");
                    break;
            }
        } else {
            // Default sort by creation date (newest first)
            sql.append("ORDER BY p.creationDate DESC ");
        }

        // Add pagination
        sql.append("LIMIT ? OFFSET ?");

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            // Set search parameters
            if (query != null && !query.isEmpty()) {
                String likeParam = "%" + query.toLowerCase() + "%";
                statement.setString(paramIndex++, likeParam);
                statement.setString(paramIndex++, likeParam);
            }

            // Set difficulty parameter
            if (difficulty != null && !difficulty.equals("All")) {
                statement.setString(paramIndex++, difficulty);
            }

            // Set dietary filter parameter
            if (dietaryFilter != null && !dietaryFilter.equals("All")) {
                statement.setString(paramIndex++, dietaryFilter);
            }

            // Set pagination parameters
            statement.setInt(paramIndex++, pageSize);
            statement.setInt(paramIndex, page * pageSize);

            ResultSet rs = statement.executeQuery();

            while (rs.next()) {
                MealPost post = extractMealPostFromResultSet(rs);
                post.setIngredients(getIngredientsForMeal(post.getId()));
                results.add(post);
            }
        }

        return results;
    }

    public int getFilteredPostsCount(
            String query, String difficulty, String timeFilter, String dietaryFilter) throws SQLException {

        String sql = "SELECT COUNT(*) FROM meal_posts WHERE 1=1";

        if (query != null && !query.isEmpty()) {
            sql += " AND (title LIKE ? OR description LIKE ?)";
        }

        if (difficulty != null && !"All".equals(difficulty)) {
            sql += " AND difficulty = ?";
        }

        if (timeFilter != null && !"All".equals(timeFilter)) {
            switch (timeFilter) {
                case "Quick":
                    sql += " AND (preparationTime + cookingTime) < 30";
                    break;
                case "Medium":
                    sql += " AND (preparationTime + cookingTime) BETWEEN 30 AND 60";
                    break;
                case "Long":
                    sql += " AND (preparationTime + cookingTime) > 60";
                    break;
            }
        }

        if (dietaryFilter != null && !"All".equals(dietaryFilter)) {
            sql += " AND dietaryType = ?";
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;

            if (query != null && !query.isEmpty()) {
                String likeQuery = "%" + query + "%";
                pstmt.setString(paramIndex++, likeQuery);
                pstmt.setString(paramIndex++, likeQuery);
            }

            if (difficulty != null && !"All".equals(difficulty)) {
                pstmt.setString(paramIndex++, difficulty);
            }

            if (dietaryFilter != null && !"All".equals(dietaryFilter)) {
                pstmt.setString(paramIndex++, dietaryFilter);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }


    /**
     * Upvotes a meal post
     * 
     * @param userId The user ID who is upvoting
     * @param mealId The meal post ID being upvoted
     * @return true if successful, false otherwise
     */
    public boolean upvoteMealPost(int userId, int mealId) throws SQLException {
        // Start a transaction to ensure data integrity
        connection.setAutoCommit(false);
        try {
            // Check if user already upvoted this post
            String checkQuery = "SELECT COUNT(*) FROM upvotes WHERE userId = ? AND mealId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkQuery)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, mealId);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // User already upvoted this post
                    connection.rollback();
                    return false;
                }
            }
            
            // Record the upvote
            String upvoteQuery = "INSERT INTO upvotes (userId, mealId) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(upvoteQuery)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, mealId);
                pstmt.executeUpdate();
            }
            
            // Increment the upvotes count in the meal_posts table
            String updateQuery = "UPDATE meal_posts SET upvotes = upvotes + 1 WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                pstmt.setInt(1, mealId);
                pstmt.executeUpdate();
            }
            
            // Get the user ID of the meal post creator
            int creatorId = 0;
            String getCreatorQuery = "SELECT userId FROM meal_posts WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getCreatorQuery)) {
                pstmt.setInt(1, mealId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    creatorId = rs.getInt("userId");
                }
            }
            
            // Update the reputation of the meal post creator
            if (creatorId > 0) {
                updateUserReputation(creatorId, 1);
            }
            
            // Commit the transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Removes an upvote from a meal post
     * 
     * @param userId The user ID who is removing the upvote
     * @param mealId The meal post ID to remove the upvote from
     * @return true if successful, false otherwise
     */
    public boolean removeUpvote(int userId, int mealId) throws SQLException {
        // Start a transaction to ensure data integrity
        connection.setAutoCommit(false);
        try {
            // Check if user has upvoted this post
            String checkQuery = "SELECT COUNT(*) FROM upvotes WHERE userId = ? AND mealId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(checkQuery)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, mealId);
                
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // User hasn't upvoted this post
                    connection.rollback();
                    return false;
                }
            }
            
            // Remove the upvote
            String removeUpvoteQuery = "DELETE FROM upvotes WHERE userId = ? AND mealId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(removeUpvoteQuery)) {
                pstmt.setInt(1, userId);
                pstmt.setInt(2, mealId);
                pstmt.executeUpdate();
            }
            
            // Decrement the upvotes count in the meal_posts table
            String updateQuery = "UPDATE meal_posts SET upvotes = upvotes - 1 WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                pstmt.setInt(1, mealId);
                pstmt.executeUpdate();
            }
            
            // Get the user ID of the meal post creator
            int creatorId = 0;
            String getCreatorQuery = "SELECT userId FROM meal_posts WHERE id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(getCreatorQuery)) {
                pstmt.setInt(1, mealId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    creatorId = rs.getInt("userId");
                }
            }
            
            // Update the reputation of the meal post creator
            if (creatorId > 0) {
                updateUserReputation(creatorId, -1);
            }
            
            // Commit the transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Adds a comment to a meal post
     * 
     * @param userId The user ID adding the comment
     * @param mealId The meal post ID being commented on
     * @param content The comment content
     * @return true if successful, false otherwise
     */
    public boolean addComment(int userId, int mealId, String content) throws SQLException {
        String query = "INSERT INTO comments (userId, mealId, content) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, mealId);
            pstmt.setString(3, content);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Deletes a comment
     * 
     * @param commentId The comment ID to delete
     * @param userId The user ID (to verify ownership)
     * @return true if successful, false otherwise
     */
    public boolean deleteComment(int commentId, int userId) throws SQLException {
        // Check if the user is the owner of the comment or an admin
        String checkQuery = "SELECT userId FROM comments WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(checkQuery)) {
            pstmt.setInt(1, commentId);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int commentUserId = rs.getInt("userId");
                if (commentUserId != userId) {
                    // Check if the user is an admin
                    User user = getUserById(userId);
                    if (user == null || user.getRole() != 1) {
                        // Not the owner and not an admin
                        return false;
                    }
                }
            } else {
                // Comment not found
                return false;
            }
        }
        
        // Delete the comment
        String deleteQuery = "DELETE FROM comments WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, commentId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    /**
     * Gets comments for a meal post
     * 
     * @param mealId The meal post ID
     * @return List of Comment objects
     */
    public List<Comment> getCommentsForMeal(int mealId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        
        String query = "SELECT c.*, u.userName FROM comments c " 
                + "JOIN users u ON c.userId = u.id " 
                + "WHERE c.mealId = ? " 
                + "ORDER BY c.creationDate DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, mealId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setUserId(rs.getInt("userId"));
                comment.setMealId(rs.getInt("mealId"));
                comment.setContent(rs.getString("content"));
                comment.setUsername(rs.getString("userName"));
                if (rs.getTimestamp("creationDate") != null) {
                    comment.setCreationDate(rs.getTimestamp("creationDate").toLocalDateTime());
                }
                
                comments.add(comment);
            }
        }
        
        return comments;
    }

    // Check if the database is empty
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    // Closes the database connection and statement.
    public void closeConnection() {
        try {
            if (statement != null)
                statement.close();
        } catch (SQLException se2) {
            se2.printStackTrace();
        }
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}