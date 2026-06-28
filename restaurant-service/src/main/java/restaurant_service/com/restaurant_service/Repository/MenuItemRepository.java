package restaurant_service.com.restaurant_service.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import restaurant_service.com.restaurant_service.Entity.MenuItem;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem,Long> {

    Page<MenuItem> findByCategoryId(Integer categoryId, Pageable pageable);
}
