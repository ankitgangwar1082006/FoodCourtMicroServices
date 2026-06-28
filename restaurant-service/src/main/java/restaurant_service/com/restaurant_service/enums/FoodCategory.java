package restaurant_service.com.restaurant_service.enums;

public enum FoodCategory {
    THALI_INDIAN(1, "Thali / Indian"),
    FAST_FOOD(2, "Fast Food"),
    CHINESE(3, "Chinese"),
    BEVERAGES(4, "Beverages"),
    DESSERTS(5, "Desserts");

    private final int id;
    private final String label;

    FoodCategory(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(Integer categoryId) {
        if (categoryId == null) {
            return false;
        }
        return categoryId >= THALI_INDIAN.id && categoryId <= DESSERTS.id;
    }
}
