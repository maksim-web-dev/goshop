package com.rest.controller.compatibility

import com.rest.controller.compatibility.adapter.CheckResponseAdapter
import com.rest.controller.compatibility.data.CheckRequest
import com.rest.controller.compatibility.data.CheckResponse
import com.rest.controller.compatibility.data.PairCheckRequest
import com.service.compatibilityCheck.CompatibilityCheckService
import com.service.compatibilityCheck.dto.CheckCompatibilityDto
import com.service.compatibilityCheck.dto.PairCheckDto
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/compatibility")
class CompatibilityController(private val compatibilityCheckService: CompatibilityCheckService) {

    @PostMapping("/check")
    fun check(@Valid @RequestBody request: CheckRequest): CheckResponse {
        val result = compatibilityCheckService.check(CheckCompatibilityDto(request.productIds))
        return with(CheckResponseAdapter) { result.toApiModel() }
    }

    @PostMapping("/pairs/check")
    fun checkPair(@RequestBody request: PairCheckRequest): CheckResponse {
        val result = compatibilityCheckService.checkPair(PairCheckDto(request.sourceProductId, request.targetProductId))
        return with(CheckResponseAdapter) { result.toApiModel() }
    }

    @GetMapping("/products/{id}")
    fun findCompatible(@PathVariable id: Long): List<Long> =
        compatibilityCheckService.findCompatibleProducts(id)
}
