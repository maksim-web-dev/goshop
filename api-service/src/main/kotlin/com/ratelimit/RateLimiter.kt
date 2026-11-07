package com.ratelimit

interface RateLimiter {
    fun tryAcquire(storeId: Long, requestsPerSecond: Int, burstLimit: Int): Boolean
}
