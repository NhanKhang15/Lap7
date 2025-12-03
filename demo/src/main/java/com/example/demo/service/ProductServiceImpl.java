package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    
    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
    
    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    @Override
    public Product saveProduct(Product product) {
        // Validation logic can go here
        return productRepository.save(product);
    }
    
    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Override
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }
    

    @Override
    public List<Product> searchProducts(String name, String category, BigDecimal minPrice, BigDecimal maxPrice, Sort sort) {
        if (name != null && name.trim().isEmpty()) name = null;
        if (category != null && category.trim().isEmpty()) category = null;
        
        // Truyền sort xuống repository
        return productRepository.searchProducts(name, category, minPrice, maxPrice, sort);
    }

    // Task 8
    @Override
    public Page<Product> searchProductsPaginated(String keyword, Pageable pageable) {
        return productRepository.findByNameContaining(keyword, pageable);
    }

    @Override
    public long countByCategory(String category) {
        return productRepository.countByCategory(category);
    }

    @Override
    public BigDecimal calculateTotalValue() {
        BigDecimal total = productRepository.calculateTotalValue();
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Double calculateAveragePrice() {
        Double avg = productRepository.calculateAveragePrice();
        return avg != null ? avg : 0.0;
    }

    @Override
    public List<Product> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    @Override
    public List<Product> getRecentProducts() {
        return productRepository.findTop5ByOrderByCreatedAtDesc();
    }
    
}
