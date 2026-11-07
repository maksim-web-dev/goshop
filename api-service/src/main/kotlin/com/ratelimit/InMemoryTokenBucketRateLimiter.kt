package com.ratelimit

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Per-store in-memory token bucket rate limiter.
 * NOTE: does not enforce a global limit across multiple service replicas.
 * Use a distributed solution (e.g. Redis) for multi-instance deployments.
 */
@Component
class InMemoryTokenBucketRateLimiter : RateLimiter {

    private data class BucketState(
        val tokens: AtomicLong,
        val lastRefillNanos: AtomicLong
    )

    private val buckets = ConcurrentHashMap<Long, BucketState>()

    override fun tryAcquire(storeId: Long, requestsPerSecond: Int, burstLimit: Int): Boolean {
        val bucket = buckets.computeIfAbsent(storeId) {
            BucketState(
                tokens = AtomicLong(burstLimit.toLong()),
                lastRefillNanos = AtomicLong(System.nanoTime())
            )
        }
        refill(bucket, requestsPerSecond, burstLimit)
        return tryConsume(bucket)
    }

    private fun refill(bucket: BucketState, requestsPerSecond: Int, burstLimit: Int) {
        val now = System.nanoTime()
        val last = bucket.lastRefillNanos.get()
        val elapsedNanos = now - last
        if (elapsedNanos < 1_000_000_000L / requestsPerSecond) return
        val tokensToAdd = (elapsedNanos.toDouble() / 1_000_000_000.0 * requestsPerSecond).toLong()
        if (tokensToAdd > 0 && bucket.lastRefillNanos.compareAndSet(last, now)) {
            bucket.tokens.updateAndGet { current ->
                minOf(burstLimit.toLong(), current + tokensToAdd)
            }
        }
    }

    private fun tryConsume(bucket: BucketState): Boolean {
        while (true) {
            val current = bucket.tokens.get()
            if (current <= 0) return false
            if (bucket.tokens.compareAndSet(current, current - 1)) return true
        }
    }
}
