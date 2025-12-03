package com.example.demo.controller.bonus;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Annotation này báo cho Spring biết đây là API trả về JSON
@RequestMapping("/api/products") // Đường dẫn gốc cho tất cả các API trong class này
public class ProductRestController {

    @Autowired
    private ProductService productService;

    // 1. Lấy danh sách tất cả sản phẩm (GET /api/products)
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products); // Trả về HTTP 200 OK + List JSON
    }

    // 2. Lấy chi tiết 1 sản phẩm (GET /api/products/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(product)) // Nếu tìm thấy -> 200 OK
                .orElse(ResponseEntity.notFound().build()); // Nếu không thấy -> 404 Not Found
    }

    // 3. Tạo mới sản phẩm (POST /api/products)
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // @RequestBody giúp chuyển JSON gửi lên thành Java Object
        Product savedProduct = productService.saveProduct(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED); // Trả về 201 Created
    }

    // 4. Cập nhật sản phẩm (PUT /api/products/{id})
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        // Kiểm tra xem sản phẩm có tồn tại không trước khi update
        return productService.getProductById(id)
                .map(existingProduct -> {
                    product.setId(id); // Đảm bảo ID trùng khớp với đường dẫn
                    Product updatedProduct = productService.saveProduct(product);
                    return ResponseEntity.ok(updatedProduct); // Trả về 200 OK
                })
                .orElse(ResponseEntity.notFound().build()); // Không tìm thấy ID -> 404
    }

    // 5. Xóa sản phẩm (DELETE /api/products/{id})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.getProductById(id).isPresent()) {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build(); // Trả về 204 No Content (Xóa thành công, không trả về dữ liệu)
        }
        return ResponseEntity.notFound().build(); // Không tìm thấy ID -> 404
    }
}