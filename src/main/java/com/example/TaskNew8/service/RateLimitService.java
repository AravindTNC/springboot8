package com.example.TaskNew8.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {

    private final LoadingCache<String, Bucket> rateLimitCache;

   
    public boolean isAllowed(String key) {
        Bucket bucket = rateLimitCache.get(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            log.debug("Request allowed for key: {}. Remaining: {}", key, probe.getRemainingTokens());
            return true;
        } else {
            log.warn("Rate limit exceeded for key: {}. Retry after: {} seconds", 
                    key, probe.getNanosToWaitForRefill() / 1_000_000_000);
            return false;
        }
    }

   
    public long getRemainingRequests(String key) {
        Bucket bucket = rateLimitCache.get(key);
        return bucket.getAvailableTokens();
    }


    public long getSecondsUntilReset(String key) {
        Bucket bucket = rateLimitCache.get(key);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(0);
        return probe.getNanosToWaitForRefill() / 1_000_000_000;
    }
}
