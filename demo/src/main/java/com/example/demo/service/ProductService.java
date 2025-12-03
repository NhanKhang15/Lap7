package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.demo.entity.Product;

public interface ProductService {
    
    List<Product> getAllProducts();
    
    Optional<Product> getProductById(Long id);

    List<String> getAllCategories();
    
    Product saveProduct(Product product);
    
    void deleteProduct(Long id);

    List<Product> getProductsByCategory(String category);
    
    // Task 5.1
    List<Product> searchProducts(String name, String category, BigDecimal minPrice, BigDecimal maxPrice, Sort sort);
    
    // Task 5.3
    Page<Product> searchProductsPaginated(String keyword, Pageable pageable);

    // Task 8
    long countByCategory(String category);

    BigDecimal calculateTotalValue();

    Double calculateAveragePrice();

    List<Product> getLowStockProducts(int threshold);

    List<Product> getRecentProducts();

}
