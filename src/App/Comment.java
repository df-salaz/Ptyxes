package App;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * The Comment class represents a user comment on a meal post.
 */
public class Comment {
    private int id;
    private int userId;
    private int mealId;
    private String content;
    private LocalDateTime creationDate;
    private String username; // Username of the commenter (for display purposes)
    
    // Constructor
    public Comment() {
    }
    
    // Full constructor
    public Comment(int id, int userId, int mealId, String content, LocalDateTime creationDate, String username) {
        this.id = id;
        this.userId = userId;
        this.mealId = mealId;
        this.content = content;
        this.creationDate = creationDate;
        this.username = username;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getMealId() {
        return mealId;
    }
    
    public void setMealId(int mealId) {
        this.mealId = mealId;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Returns a formatted string representing when this comment was created
     * relative to the current time (e.g., "5 minutes ago", "2 hours ago", "3 days ago")
     * 
     * @return A string indicating how long ago the comment was created
     */
    public String getTimeAgo() {
        if (creationDate == null) {
            return "unknown time";
        }
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(creationDate, now);
        
        if (minutes < 1) {
            return "just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (minutes < 24 * 60) {
            long hours = minutes / 60;
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (minutes < 30 * 24 * 60) {
            long days = minutes / (60 * 24);
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            return creationDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        }
    }
    
    /**
     * Returns a formatted string with the date and time the comment was created
     * 
     * @return A formatted date and time string
     */
    public String getFormattedDateTime() {
        if (creationDate == null) {
            return "unknown time";
        }
        return creationDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"));
    }
    
    /**
     * Returns a preview of the comment content
     * 
     * @param maxLength The maximum length of the preview
     * @return A preview of the comment content
     */
    public String getContentPreview(int maxLength) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        if (content.length() <= maxLength) {
            return content;
        }
        
        return content.substring(0, maxLength) + "...";
    }
    
    /**
     * Returns a string representation of this Comment object
     * 
     * @return A string representation of this Comment
     */
    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", mealId=" + mealId +
                ", username='" + username + '\'' +
                ", content='" + getContentPreview(30) + '\'' +
                ", creationDate=" + (creationDate != null ? getFormattedDateTime() : "null") +
                '}';
    }
    
    /**
     * Checks if this Comment is equal to another object
     * 
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Comment comment = (Comment) o;
        return id == comment.id;
    }
    
    /**
     * Returns a hash code for this Comment
     * 
     * @return A hash code value for this Comment
     */
    @Override
    public int hashCode() {
        return id;
    }
}