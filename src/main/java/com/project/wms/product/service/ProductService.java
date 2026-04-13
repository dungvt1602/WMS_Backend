package com.project.wms.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.stringtemplate.v4.compiler.STParser.template_return;

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
    @CacheEvict(value = "products", allEntries = true)
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
    @CacheEvict(value = "products", allEntries = true)
    public Page<ProductResponse> getAllProduct(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        return mapToResponse(product);
    }

    // cap nhập product
    @Transactional
    @CachePut(value = "products", key = "#id")
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
        throw new RuntimeException(
                "【Chính sách Enterprise】: Cấm xóa cứng (Hard Delete) Sản phẩm để bảo toàn lịch sử Kho. Vui lòng gọi API Disable Sản phẩm!");
    }

    // Tắt kinh doanh sản phẩm
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void disableProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));
        product.setActive(false);
        productRepository.save(product);
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
                product.isActive(),
                product.getCreatedAt());
    }
}
