package restaurant_service.com.restaurant_service.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import restaurant_service.com.restaurant_service.Entity.MenuItem;
import restaurant_service.com.restaurant_service.Entity.Restaurant;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant,Long> {
}
