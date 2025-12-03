package com.example.demo.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;  
import java.util.UUID;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/products")
public class ProductController {
    
    private final ProductService productService;
    
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    // List all products
    @GetMapping
    public String listProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product-list";  // Returns product-list.html
    }
    
    // Show form for new product
    @GetMapping("/new")
    public String showNewForm(Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        return "product-form";
    }
    
    // Show form for editing product
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productService.getProductById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Product not found");
                    return "redirect:/products";
                });
    }
    
    // Delete product
    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/products";
    }
    
// Task 5.1 & 5.2: Advanced Search (Trả về List thường)
    // --- TASK 7.1 & 7.3: SORTING + ADVANCED SEARCH ---
    @GetMapping("/advanced-search")
    public String advancedSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            // Thêm tham số sorting
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            Model model) {

        // 1. Tạo đối tượng Sort
        Sort sort = sortDir.equalsIgnoreCase("asc") ? 
                    Sort.by(sortBy).ascending() : 
                    Sort.by(sortBy).descending();

        // 2. Gọi Service kèm Sort
        List<Product> products = productService.searchProducts(name, category, minPrice, maxPrice, sort);
        
        // 3. Đẩy dữ liệu ra View
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllCategories());
        
        // 4. Giữ lại giá trị Search (để khi sort không bị mất filter)
        model.addAttribute("selectedName", name);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        
        // 5. Giữ lại giá trị Sort (để hiển thị mũi tên và tạo link đảo chiều)
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // 6. Config view
        model.addAttribute("isPaginated", false);

        return "product-list";
    }

    // Task 5.3: Search with Pagination (Trả về Page)
    @GetMapping("/search")
    public String searchProductsPaginated(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productService.searchProductsPaginated(keyword, pageable);

        // Task 5.2: Vẫn cần load categories nếu dùng chung view
        model.addAttribute("categories", productService.getAllCategories());

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        
        // Cờ để view biết đang dùng chế độ phân trang
        model.addAttribute("isPaginated", true); 

        return "product-list";
    }

    // // Task 6.2: Update Save Method with Validation
    // @PostMapping("/save")
    // public String saveProduct(
    //         @Valid @ModelAttribute("product") Product product, 
    //         BindingResult result,                              
    //         Model model,
    //         RedirectAttributes redirectAttributes) {
        
    //     // Nếu có lỗi validation
    //     if (result.hasErrors()) {
    //         // Trả về lại trang form để hiển thị lỗi
    //         return "product-form";
    //     }
        
    //     try {
    //         productService.saveProduct(product);
    //         redirectAttributes.addFlashAttribute("message", 
    //                 product.getId() == null ? "Product added successfully!" : "Product updated successfully!");
    //     } catch (Exception e) {
    //         redirectAttributes.addFlashAttribute("error", "Error saving product: " + e.getMessage());
    //         return "redirect:/products/new"; 
    //     }
        
    //     return "redirect:/products/advanced-search";
    // }

    // BONUS IMAGE UPLAOD
    @PostMapping("/save")
    public String saveProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam("imageFile") MultipartFile multipartFile, // Nhận file từ form
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "product-form";
        }

        try {
            // Xử lý upload ảnh
            if (!multipartFile.isEmpty()) {
                String fileName = org.springframework.util.StringUtils.cleanPath(multipartFile.getOriginalFilename());
                // Tạo tên file duy nhất để tránh trùng lặp
                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                
                // Thư mục lưu trữ
                String uploadDir = "uploads";
                Path uploadPath = Paths.get(uploadDir);

                // Tạo thư mục nếu chưa tồn tại
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Lưu file
                try (var inputStream = multipartFile.getInputStream()) {
                    Path filePath = uploadPath.resolve(uniqueFileName);
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    
                    // Lưu tên file vào đối tượng product
                    product.setImagePath(uniqueFileName);
                } catch (IOException ioe) {
                    throw new IOException("Could not save image file: " + uniqueFileName, ioe);
                }
            } else {
                // Nếu người dùng không upload ảnh mới khi edit, giữ nguyên ảnh cũ
                // (Logic này giả định form có hidden field chứa imagePath cũ, hoặc bạn phải query lại từ DB)
                if (product.getId() != null) {
                   Product existingProduct = productService.getProductById(product.getId()).orElse(null);
                   if (existingProduct != null) {
                       // Nếu imagePath trong form gửi lên bị null (do input file trống), lấy lại cái cũ
                       if (product.getImagePath() == null || product.getImagePath().isEmpty()) {
                           product.setImagePath(existingProduct.getImagePath());
                       }
                   }
                }
            }

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("message", "Product saved successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return "redirect:/products/advanced-search";
    }
    
    // Trang chủ mặc định
    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/advanced-search";
    }
}
