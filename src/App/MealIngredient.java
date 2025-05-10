package App;

/**
 * The MealIngredient class represents an ingredient used in a meal post.
 * It contains information about the ingredient, including its name, quantity, and unit.
 */
public class MealIngredient {
    private int id;
    private String name;
    private String category;
    private float quantity;
    private String unit;
    
    // Constructor
    public MealIngredient() {
    }
    
    // Full constructor
    public MealIngredient(int id, String name, String category, float quantity, String unit) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Constructor without ID (for new ingredients)
    public MealIngredient(String name, String category, float quantity, String unit) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public float getQuantity() {
        return quantity;
    }
    
    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    /**
     * Gets a formatted representation of the quantity and unit
     * 
     * @return Formatted string (e.g., "2 tablespoons")
     */
    public String getFormattedAmount() {
        // Handle special cases for better readability
        if (quantity == 0) {
            return "to taste";
        }
        
        if (unit == null || unit.isEmpty()) {
            if (quantity == 1) {
                return String.format("%.0f", quantity);
            } else {
                return String.format("%.1f", quantity).replaceAll("\\.0$", "");
            }
        }
        
        // Format based on whether it's a whole number or has decimals
        String quantityStr = getQuantityStr();

        // Handle plural for units if necessary
        String unitStr = unit;
        if (quantity > 1 && 
            !unit.equalsIgnoreCase("pinch") && 
            !unit.endsWith("s") && 
            !unit.equalsIgnoreCase("oz") && 
            !unit.equalsIgnoreCase("lb")) {
            unitStr = unit + "s";
        }
        
        return quantityStr + " " + unitStr;
    }

    /**
     * Generates a string representation of the quantity field, formatting it as either an integer or
     * a decimal with up to two significant digits. Also converts specific quantities into their
     * corresponding fractional representations (e.g., "0.25" to "¼").
     *
     * @return The formatted quantity as a string, or its fractional representation if applicable.
     */
    private String getQuantityStr() {
        String quantityStr;
        if (quantity == (int) quantity) {
            quantityStr = String.format("%.0f", quantity);
        } else {
            quantityStr = String.format("%.2f", quantity).replaceAll("0+$", "").replaceAll("\\.$", "");
        }

        // Special handling for fractions
        if (quantity == 0.25f) {
            quantityStr = "¼";
        } else if (quantity == 0.5f) {
            quantityStr = "½";
        } else if (quantity == 0.75f) {
            quantityStr = "¾";
        } else if (quantity == 0.33f || quantity == 0.333f) {
            quantityStr = "⅓";
        } else if (quantity == 0.67f || quantity == 0.666f) {
            quantityStr = "⅔";
        }
        return quantityStr;
    }

    @Override
    public String toString() {
        return getFormattedAmount() + " " + name;
    }
}