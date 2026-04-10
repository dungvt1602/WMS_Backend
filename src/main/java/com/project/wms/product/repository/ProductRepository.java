package com.project.wms.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.wms.product.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);
}
