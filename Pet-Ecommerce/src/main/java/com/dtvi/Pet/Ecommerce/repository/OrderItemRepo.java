package com.dtvi.Pet.Ecommerce.repository;

import com.dtvi.Pet.Ecommerce.entity.OrderItem;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface OrderItemRepo extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {


    boolean existsByProductId(Long productId);

    @Query("SELECT SUM(oi.price) FROM OrderItem oi WHERE MONTH(oi.order.createdAt) = :month AND YEAR(oi.order.createdAt) = :year")
    Double calculateMonthlyRevenue(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(DISTINCT oi.order.id) FROM OrderItem oi WHERE MONTH(oi.order.createdAt) = :month AND YEAR(oi.order.createdAt) = :year")
    Long countMonthlyOrders(@Param("month") int month, @Param("year") int year);

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) AS totalQuantity " +
       "FROM OrderItem oi WHERE MONTH(oi.order.createdAt) = :month AND YEAR(oi.order.createdAt) = :year " +
       "GROUP BY oi.product.id, oi.product.name " +
       "ORDER BY totalQuantity DESC")
    List<Object[]> findBestSellingProducts(@Param("month") int month, @Param("year") int year);

}
