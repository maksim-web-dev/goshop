package com.rest.controller.recommendation

import com.rest.controller.recommendation.data.BuildRecommendationRequest
import com.service.recommendation.RecommendationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/recommendations/products")
class RecommendationController(private val recommendationService: RecommendationService) {

    @GetMapping("/{id}")
    fun getAll(@PathVariable id: Long): List<Long> =
        recommendationService.findAll(id)

    @GetMapping("/{id}/similar")
    fun getSimilar(@PathVariable id: Long): List<Long> =
        recommendationService.findSimilar(id)

    @GetMapping("/{id}/alternatives")
    fun getAlternatives(@PathVariable id: Long): List<Long> =
        recommendationService.findAlternatives(id)

    @GetMapping("/{id}/accessories")
    fun getAccessories(@PathVariable id: Long): List<Long> =
        recommendationService.findAccessories(id)

    @PostMapping("/build")
    fun buildForPc(@Valid @RequestBody request: BuildRecommendationRequest): Map<Long, List<Long>> =
        recommendationService.buildForPc(request.productIds)
}
