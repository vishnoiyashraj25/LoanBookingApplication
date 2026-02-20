package com.project.LoanBookingApplication.config;

/**
 * Redis cache names used by @Cacheable and @CacheEvict.
 */
public final class CacheNames {

    public static final String OFFERS = "offers";
    public static final String LENDERS = "lenders";

    private CacheNames() {
    }
}
