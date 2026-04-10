package com.project.wms.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.wms.product.dto.ProductRequest;
import com.project.wms.product.dto.ProductResponse;
import com.project.wms.product.entity.Product;
import com.project.wms.product.repository.ProductRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest) {

        // kiểm tra sku có trùng hay không
        productRepository.findBySku(productRequest.sku())
                .ifPresent(product -> {
                    throw new RuntimeException("Sản phẩm đã tồn tại " + product.getSku());
                });

        // tạo sản phẩm
        Product product = Product.builder()
                .name(productRequest.name())
                .sku(productRequest.sku())
                .description(productRequest.description())
                .unit(productRequest.unit())
                .price(productRequest.price())
                .stock(productRequest.quantity())
                .build();

        // Lưu sản phẩm vào database
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    // tìm tất cả sản phẩm
    public List<ProductResponse> getAllProduct() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse) // method reference gọn hơn lambda
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        return mapToResponse(product);
    }

    // cap nhập product
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest productRequest) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        // cập nhập thông tin sản phẩm
        product.setName(productRequest.name());
        product.setSku(productRequest.sku());
        product.setDescription(productRequest.description());
        product.setUnit(productRequest.unit());
        product.setPrice(productRequest.price());
        product.setStock(productRequest.quantity());

        Product updatedProduct = productRepository.save(product);
        return mapToResponse(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        productRepository.delete(product);
    }

    // Hàm Helper chuyển đổi Entity -> Response DTO
    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getUnit(),
                product.getPrice(),
                0, // Tạm thời để stock = 0, sẽ xử lý ở module Inventory sau
                product.getCreatedAt());
    }
}
