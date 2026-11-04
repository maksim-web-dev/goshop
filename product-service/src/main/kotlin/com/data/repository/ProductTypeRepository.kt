package com.data.repository

import com.data.entity.ProductType
import org.springframework.data.jpa.repository.JpaRepository

interface ProductTypeRepository : JpaRepository<ProductType, Long>