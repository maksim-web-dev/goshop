package com.service.discount

import com.data.entity.Discount
import com.data.entity.DiscountStatus
import com.data.repository.DiscountRepository
import com.service.discount.dto.CreateDiscountDto
import com.service.discount.dto.UpdateDiscountDto
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class DiscountService(private val discountRepository: DiscountRepository) {

    fun findAll(): List<Discount> = discountRepository.findAll()

    fun findById(id: UUID): Discount =
        discountRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Discount $id not found") }

    @Transactional
    fun create(dto: CreateDiscountDto): Discount =
        discountRepository.save(
            Discount(
                storeId = dto.storeId,
                productId = dto.productId,
                discountType = dto.discountType,
                discountValue = dto.discountValue,
                startsAt = dto.startsAt,
                endsAt = dto.endsAt
            )
        )

    @Transactional
    fun update(id: UUID, dto: UpdateDiscountDto): Discount {
        val existing = findById(id)
        return discountRepository.save(
            Discount(
                id = existing.id,
                storeId = existing.storeId,
                productId = existing.productId,
                discountType = existing.discountType,
                discountValue = dto.discountValue ?: existing.discountValue,
                startsAt = dto.startsAt ?: existing.startsAt,
                endsAt = dto.endsAt ?: existing.endsAt,
                status = dto.status ?: existing.status
            )
        )
    }

    @Transactional
    fun disable(id: UUID) {
        val existing = findById(id)
        discountRepository.save(
            Discount(
                id = existing.id,
                storeId = existing.storeId,
                productId = existing.productId,
                discountType = existing.discountType,
                discountValue = existing.discountValue,
                startsAt = existing.startsAt,
                endsAt = existing.endsAt,
                status = DiscountStatus.INACTIVE
            )
        )
    }
}
