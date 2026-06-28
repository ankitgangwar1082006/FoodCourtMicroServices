package restaurant_service.com.restaurant_service.Entity;


import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Double price;

    @Column(name = "is_vegetarian", nullable = false)
    @Builder.Default
    private Boolean vegetarian = false;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean available = false;

    private String imageUrl;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    @JsonIgnore // IMPORTANT: Yeh JSON banate time Infinite Loop (Restaurant->Item->Restaurant) ko rokega
    private Restaurant restaurant;

    @PrePersist
    @PreUpdate
    @PostLoad
    private void applyBooleanDefaults() {
        if (vegetarian == null) {
            vegetarian = false;
        }
        if (available == null) {
            available = false;
        }
    }
}