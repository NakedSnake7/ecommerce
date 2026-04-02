package com.ecomerce.store.repository;

import com.ecomerce.store.dto.ProductSalesDTO;  
import com.ecomerce.store.model.Order;
import com.ecomerce.store.model.OrderStatus;
import com.ecomerce.store.model.PaymentStatus;
import com.ecomerce.store.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // =========================
    // SINGLE ORDER
    // =========================
    @Query("SELECT o FROM Order o JOIN FETCH o.user WHERE o.id = :id")
    Optional<Order> findByIdWithUser(@Param("id") Long id);

    @Query("""
        SELECT o 
        FROM Order o 
        JOIN FETCH o.user 
        JOIN FETCH o.items i 
        JOIN FETCH i.producto 
        WHERE o.id = :id
    """)
    Optional<Order> findByIdWithUserAndItems(@Param("id") Long id);



    @Query("SELECT o FROM Order o JOIN FETCH o.user")
    List<Order> findAllWithUser();

    // =========================
    // 🔥 ÓRDENES PENDIENTES (CREATED) PARA EXPIRAR
    // =========================
    @Query("""
    	    SELECT DISTINCT o
    	    FROM Order o
    	    LEFT JOIN FETCH o.items i
    	    LEFT JOIN FETCH i.producto
    	    WHERE o.orderStatus = com.ecomerce.store.model.OrderStatus.CREATED
    	      AND o.paymentMethod = com.ecomerce.store.model.Order.PaymentMethod.TRANSFER
    	""")
    	List<Order> findPendingOrdersWithItems();


    // =========================
    // STRIPE
    // =========================
    Optional<Order> findByStripeSessionId(String stripeSessionId);

 

    @Query("""
    	    SELECT DISTINCT o
    	    FROM Order o
    	    JOIN FETCH o.user
    	    WHERE (:status IS NULL OR o.orderStatus = :status)
    	      AND (:payment IS NULL OR o.paymentStatus = :payment)
    	      AND (:from IS NULL OR o.orderDate >= :from)
    	      AND (:to IS NULL OR o.orderDate <= :to)
    	""")
    	List<Order> findFilteredWithUser(
    	    @Param("status") OrderStatus status,
    	    @Param("payment") PaymentStatus payment,
    	    @Param("from") LocalDateTime from,
    	    @Param("to") LocalDateTime to
    	);
    
    @Query("""
    	    SELECT new com.ecomerce.store.dto.ProductSalesDTO(
    	        p.productName,
    	        SUM(oi.quantity)
    	    )
    	    FROM OrderItem oi
    	    JOIN oi.order o
    	    JOIN oi.producto p
    	    WHERE o.paymentStatus = com.ecomerce.store.model.PaymentStatus.PAID
    	    GROUP BY p.productName
    	    ORDER BY SUM(oi.quantity) DESC
    	""")
    	List<ProductSalesDTO> getPaidProductSales();

    @Query("""
    	    SELECT new com.ecomerce.store.dto.ProductSalesDTO(
    	        p.productName,
    	        SUM(i.quantity)
    	    )
    	    FROM Order o
    	    JOIN o.items i
    	    JOIN i.producto p
    	    WHERE o.paymentStatus = com.ecomerce.store.model.PaymentStatus.PAID
    	      AND (:from IS NULL OR o.paidAt >= :from)
    	      AND (:to IS NULL OR o.paidAt <= :to)
    	    GROUP BY p.productName
    	    ORDER BY SUM(i.quantity) DESC
    	""")
    	List<ProductSalesDTO> getPaidProductSalesByDate(
    	    @Param("from") LocalDateTime from,
    	    @Param("to") LocalDateTime to
    	);

    List<Order> findByCustomerEmailOrderByOrderDateDesc(String customerEmail);
    
    Optional<Order> findTopByUserOrderByOrderDateDesc(User user);

    List<Order> findByCustomerEmailIgnoreCaseAndUserIsNull(String email);


}