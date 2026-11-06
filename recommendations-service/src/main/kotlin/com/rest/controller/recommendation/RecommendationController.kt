package com.rest.controller.recommendation

import com.rest.controller.recommendation.data.BuildRecommendationRequest
import com.service.recommendation.RecommendationService
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/recommendations/products")
class RecommendationController(private val recommendationService: RecommendationService) {

    @GetMapping("/{id}")
    fun getAll(@PathVariable id: UUID): List<UUID> =
        recommendationService.findAll(id)

    @GetMapping("/{id}/similar")
    fun getSimilar(@PathVariable id: UUID): List<UUID> =
        recommendationService.findSimilar(id)

    @GetMapping("/{id}/alternatives")
    fun getAlternatives(@PathVariable id: UUID): List<UUID> =
        recommendationService.findAlternatives(id)

    @GetMapping("/{id}/accessories")
    fun getAccessories(@PathVariable id: UUID): List<UUID> =
        recommendationService.findAccessories(id)

    @PostMapping("/build")
    fun buildForPc(@RequestBody request: BuildRecommendationRequest): Map<UUID, List<UUID>> =
        recommendationService.buildForPc(request.productIds)
}
