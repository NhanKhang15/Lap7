package com.example.demo.controller.bonus;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ProductService productService;

    @GetMapping("/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        // 1. Cấu hình response header để trình duyệt biết đây là file tải về
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=products_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        // 2. Lấy danh sách sản phẩm
        List<Product> listProducts = productService.getAllProducts();

        // 3. Khởi tạo Workbook (File Excel) và Sheet
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Products");

            // --- TẠO HEADER (DÒNG TIÊU ĐỀ) ---
            Row headerRow = sheet.createRow(0);
            
            // Tạo Style cho Header (In đậm, chữ to hơn xíu)
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 12);
            headerStyle.setFont(font);

            // Các cột tiêu đề
            String[] headers = {"ID", "Code", "Name", "Category", "Price", "Quantity", "Total Value"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- ĐIỀN DỮ LIỆU (DATA ROWS) ---
            int rowIdx = 1;
            for (Product product : listProducts) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getProductCode());
                row.createCell(2).setCellValue(product.getName());
                row.createCell(3).setCellValue(product.getCategory());
                row.createCell(4).setCellValue(product.getPrice().doubleValue());
                row.createCell(5).setCellValue(product.getQuantity());
                
                // Tính tổng tiền (Price * Quantity)
                double total = product.getPrice().doubleValue() * product.getQuantity();
                row.createCell(6).setCellValue(total);
            }

            // Tự động điều chỉnh độ rộng cột cho đẹp
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 4. Ghi ra luồng output của response
            workbook.write(response.getOutputStream());
        }
    }
}
