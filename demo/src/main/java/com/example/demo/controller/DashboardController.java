package com.example.demo.controller;

import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public String showDashboard(Model model) {
        // 1. Các thẻ thống kê tổng quan (Cards)
        model.addAttribute("totalProducts", productService.getAllProducts().size());
        model.addAttribute("totalValue", productService.calculateTotalValue());
        model.addAttribute("averagePrice", productService.calculateAveragePrice());
        
        // 2. Dữ liệu cho Biểu đồ tròn (Pie Chart) - Products by Category
        List<String> categories = productService.getAllCategories();
        List<Long> categoryCounts = new ArrayList<>();
        
        for (String category : categories) {
            categoryCounts.add(productService.countByCategory(category));
        }
        
        model.addAttribute("categories", categories);       // Labels
        model.addAttribute("categoryCounts", categoryCounts); // Data

        // 3. Cảnh báo sắp hết hàng (Low Stock)
        model.addAttribute("lowStockProducts", productService.getLowStockProducts(10)); // Threshold = 10

        // 4. Sản phẩm mới nhập gần đây
        model.addAttribute("recentProducts", productService.getRecentProducts());

        return "dashboard";
    }
}