package order_service.com.order_service.repository;

import order_service.com.order_service.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {


    Page<Order> findByUserId(
            Long userId,
            Pageable pageable
    );
}
