package com.service.brand

import com.data.entity.Brand
import com.data.repository.BrandRepository
import com.service.brand.dto.CreateBrandDto
import com.service.brand.dto.UpdateBrandDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class BrandService(private val brandRepository: BrandRepository) {

    fun findAll(): List<Brand> = brandRepository.findAll()

    fun findById(id: Long): Brand =
        brandRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Brand $id not found") }

    @Transactional
    fun create(dto: CreateBrandDto): Brand =
        brandRepository.save(Brand(name = dto.name, websiteUrl = dto.websiteUrl))

    @Transactional
    fun update(id: Long, dto: UpdateBrandDto): Brand {
        val existing = findById(id)
        return brandRepository.save(
            Brand(
                id = existing.id,
                name = dto.name ?: existing.name,
                websiteUrl = dto.websiteUrl ?: existing.websiteUrl
            )
        )
    }

    @Transactional
    fun delete(id: Long) {
        if (!brandRepository.existsById(id))
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Brand $id not found")
        brandRepository.deleteById(id)
    }
}