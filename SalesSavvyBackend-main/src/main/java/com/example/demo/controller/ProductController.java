package com.example.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProducts(
            @RequestParam(required = false) String category,
            HttpServletRequest request) {

        try {

            // Get user from filter (if exists)
            User authenticatedUser = (User) request.getAttribute("authenticatedUser");

            // ✅ Allow guest user
            if (authenticatedUser == null) {
                authenticatedUser = new User();
                authenticatedUser.setUsername("Guest");
            }

            // Fetch products
            List<Product> products = productService.getProductsByCategory(category);

            Map<String, Object> response = new HashMap<>();

            // ✅ Safe user info
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("name", authenticatedUser.getUsername());
            userInfo.put("role",
                    authenticatedUser.getRole() != null
                            ? authenticatedUser.getRole().name()
                            : "GUEST");

            response.put("user", userInfo);

            // Product list
            List<Map<String, Object>> productList = new ArrayList<>();

            for (Product product : products) {
                Map<String, Object> productDetails = new HashMap<>();

                productDetails.put("product_id", product.getProductId());
                productDetails.put("name", product.getName());
                productDetails.put("description", product.getDescription());
                productDetails.put("price", product.getPrice());
                productDetails.put("stock", product.getStock());

                List<String> images = productService.getProductImages(product.getProductId());
                productDetails.put("images", images);

                productList.add(productDetails);
            }

            response.put("products", productList);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}