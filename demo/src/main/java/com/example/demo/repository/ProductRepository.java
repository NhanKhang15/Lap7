package com.example.demo.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(String category);

    List<Product> findByNameContaining(String keyword);

    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    List<Product> findByCategoryOrderByPriceAsc(String category);

    boolean existsByProductCode(String productCode);

    // Task 5.1: Multi-Criteria Search
    @Query("SELECT p FROM Product p WHERE " +
            "(:name IS NULL OR p.name LIKE %:name%) AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> searchProducts(@Param("name") String name,
                                    @Param("category") String category,
                                    @Param("minPrice") BigDecimal minPrice,
                                    @Param("maxPrice") BigDecimal maxPrice,
                                    Sort sort);

    // Task 5.2: Get Unique Categories
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllCategories();

    // Task 5.3: Search with Pagination (Tìm theo tên)
    Page<Product> findByNameContaining(String keyword, Pageable pageable);

    // --- TASK 8.1: STATISTICS ---
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
    long countByCategory(@Param("category") String category);

    @Query("SELECT SUM(p.price * p.quantity) FROM Product p")
    BigDecimal calculateTotalValue();

    @Query("SELECT AVG(p.price) FROM Product p")
    Double calculateAveragePrice(); // Đổi BigDecimal sang Double cho dễ hiển thị AVG

    @Query("SELECT p FROM Product p WHERE p.quantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
    
    // Thêm cái này để hiển thị "Recent products (last 5 added)" cho Task 8.3
    List<Product> findTop5ByOrderByCreatedAtDesc();

}

