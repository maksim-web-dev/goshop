package com.data.repository

import com.data.entity.Product
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface ProductRepository : JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>