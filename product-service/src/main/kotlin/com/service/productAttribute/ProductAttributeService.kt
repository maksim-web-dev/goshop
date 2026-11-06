package com.service.productAttribute

import com.data.entity.ProductAttribute
import com.data.repository.CategoryParameterRepository
import com.data.repository.ProductAttributeRepository
import com.data.repository.ProductRepository
import com.service.productAttribute.dto.SetAttributesDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ProductAttributeService(
    private val productAttributeRepository: ProductAttributeRepository,
    private val productRepository: ProductRepository,
    private val categoryParameterRepository: CategoryParameterRepository
) {

    fun findByProductId(productId: Long): List<ProductAttribute> =
        productAttributeRepository.findByProductId(productId)

    @Transactional
    fun setProductAttributes(productId: Long, dto: SetAttributesDto): List<ProductAttribute> {
        val product = productRepository.findById(productId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Product $productId not found") }

        productAttributeRepository.deleteByProductId(productId)

        val newAttributes = dto.attributes.map { av ->
            val categoryParameter = categoryParameterRepository.findById(av.categoryParameterId)
                .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "CategoryParameter ${av.categoryParameterId} not found") }
            ProductAttribute(
                product = product,
                categoryParameter = categoryParameter,
                value = av.value
            )
        }
        return productAttributeRepository.saveAll(newAttributes)
    }

    @Transactional
    fun deleteByProductId(productId: Long) =
        productAttributeRepository.deleteByProductId(productId)
}