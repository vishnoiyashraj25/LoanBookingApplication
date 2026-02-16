# Redis in Loan Booking Application – Review Q&A

This document lists likely questions in a project review about Redis usage, with answers specific to this codebase.

---

## 1. Why Redis for caching? Why not in-memory (e.g. Caffeine/ConcurrentHashMap)?

**Answer:**  
We use Redis so that cache is **shared across application instances** and **survives restarts**.

- **In-memory (e.g. Spring’s default Simple cache):** Each JVM has its own cache. With multiple instances (e.g. behind a load balancer), each instance would have different data and we’d hit the DB more. Restart clears the cache.
- **Redis:** Single (or clustered) cache store. All instances read/write the same cache, so we get consistent caching and fewer DB hits. Data persists until TTL or eviction.

For a loan-booking app that may scale horizontally, shared cache is important so that offers, lenders, and user lists are consistent and we don’t overload MySQL.

---

## 2. Why did you choose different TTLs for different caches (5 min offers, 15 min lenders, 10 min users)?

**Answer:**  
We tuned TTLs by how often the underlying data changes and how critical freshness is.

- **Offers (5 min):** Change more often (new offers, status/rate changes). Shorter TTL keeps data fresher and avoids serving stale offers.
- **Lenders (15 min):** Relatively static (new lenders are rare). Longer TTL reduces DB and Redis load.
- **Users list (10 min):** Changes on registration and KYC verification. 10 min is a balance between freshness and load; we also invalidate on write (see below).

Default TTL (10 min) is for any cache we add later without a specific config.

---

## 3. Why `StringRedisSerializer` for keys and `GenericJackson2JsonRedisSerializer` for values?

**Answer:**

- **Keys – `StringRedisSerializer`:**  
  Cache keys are strings (e.g. `lenders::123-null-BANK`). String keys are readable in Redis (e.g. in `redis-cli`), avoid binary serialization issues, and work well with key patterns if we ever do bulk eviction or debugging.

- **Values – `GenericJackson2JsonRedisSerializer`:**  
  We cache DTOs and lists (e.g. `List<LenderResponse>`). JSON gives:
  - Human-readable values in Redis.
  - No Java serialization (avoids issues when class versions change).
  - Type metadata in JSON so collections and nested objects deserialize correctly.

We don’t use Java serialization because it’s brittle across versions and not readable.

---

## 4. How do cache keys work? Why this key expression for `getAllLendersJson`?

**Answer:**  
Spring builds a **cache key** from the SpEL expression in `@Cacheable(key = "...")`. That key is unique per “logical query.”

Example in `LenderService`:

```java
@Cacheable(value = "lenders", key = "#lenderId + '-' + #lenderName + '-' + #lenderType")
```

- **Cache name:** `lenders` (so we get cache `lenders` from `RedisCacheManager`).
- **Key:** e.g. `123-null-BANK` when `lenderId=123`, `lenderName=null`, `lenderType=BANK`.

So: same arguments → same key → cache hit; different arguments → different key → separate cache entries. That way we cache each filter combination (e.g. “all lenders”, “lender 5”, “BANK type”) separately.

We use `+ '-' +` so nulls become the string `"null"` and don’t break the key.

---

## 5. Why `@CacheEvict(..., allEntries = true)` instead of evicting a single key?

**Answer:**  
Our “read” methods return **filtered lists** (e.g. all lenders, or filtered by id/name/type). The cache key is the filter parameters, not a single entity id.

When we **add or change** one lender (e.g. `registerLender`), that new lender can appear in **many** cached lists (e.g. “all”, “by type”, etc.). We don’t know all those keys, so we evict the **entire** `lenders` cache with `allEntries = true`. Same idea for `offers` and `users_list`: one write can affect many cached queries, so we clear the whole cache for that domain. Next read repopulates the cache for the requested filters.

Evicting by single key would only make sense if we had one cache entry per entity (e.g. `@Cacheable(key = "#id")` for “get lender by id”).

---

## 6. Where do you evict cache and why there?

**Answer:**  
We evict whenever we **change** data that could make the cached list stale:

| Cache       | Evicted in                          | Reason |
|------------|--------------------------------------|--------|
| `offers`   | `OfferService.createOffer()`         | New offer must appear in “get all offers” and filtered queries. |
| `lenders`  | `LenderService.registerLender()`     | New lender must appear in lender lists. |
| `users_list` | `UserService.registerUser()`      | New user must appear in user list. |
| `users_list` | `KYCService.verifyKYC()`          | User’s `kycVerified` changes; list filters by KYC status, so we must refresh. |

So: every **write path** that affects a cached list has a corresponding `@CacheEvict` on that cache.

---

## 7. What is `@EnableCaching` and what does it do?

**Answer:**  
`@EnableCaching` enables Spring’s **cache abstraction**: it turns on the processing of `@Cacheable`, `@CacheEvict`, and `@CachePut`.

Under the hood, Spring creates proxies around beans that have cache annotations. When you call a `@Cacheable` method, the proxy runs first: it checks the cache; if there’s a hit it returns the cached value and never calls the real method; if there’s a miss it calls the method and then stores the result in the cache. So without `@EnableCaching`, those annotations would do nothing.

---

## 8. Where does `RedisConnectionFactory` come from? Did you define it?

**Answer:**  
We **did not** define it. It comes from **Spring Boot auto-configuration**.

Because we have `spring-boot-starter-data-redis` on the classpath and set `spring.data.redis.host` and `spring.data.redis.port` (and `spring.cache.type=redis`), Boot creates a `RedisConnectionFactory` (typically Lettuce) and connects to that host/port. Our `RedisConfig` only **uses** that factory in `RedisCacheManager.builder(connectionFactory)` so that the cache manager uses the same Redis as the rest of the app.

---

## 9. What is `cacheDefaults` and why do you use it?

**Answer:**  
`cacheDefaults(createCacheConfig(DEFAULT_TTL))` sets the **default configuration** for any cache that is **not** listed in `withInitialCacheConfigurations`.

- Caches we explicitly configure: `offers`, `lenders`, `users_list` (with their specific TTLs).
- If someone later adds `@Cacheable("something_else")`, Spring will create a cache named `something_else` on first use. Without `cacheDefaults`, that cache might get Spring’s global defaults (which might not match our serialization or TTL). With `cacheDefaults`, it gets our 10-minute TTL and the same key/value serialization, so behavior is consistent and we avoid surprises.

---

## 10. What happens if Redis is down or unreachable?

**Answer:**  
If Redis is unavailable at runtime, cache **reads/writes will fail**. Spring’s cache abstraction does not fall back to “no cache” by default; the operation (e.g. a `@Cacheable` method call) can throw (e.g. connection exception), and the request may fail.

Mitigations we could add:

- **Graceful degradation:** Implement a custom `CacheManager` or use a library that catches Redis errors and falls back to calling the real method (no cache) so the app keeps working with DB only.
- **Health checks:** Use Spring Boot Actuator with Redis health to monitor and alert when Redis is down.
- **Resilience:** Use connection pooling and timeouts (e.g. in Lettuce) so we don’t hang forever.

In review, you can say: “Currently we assume Redis is available; for production we’d add health checks and consider a fallback so the app still works without cache.”

---

## 11. Why is KYC verification evicting `users_list`?

**Answer:**  
`getAllUsersJson` can filter by `kycVerified`. When we call `verifyKYC(userId)`, that user’s `kycVerified` changes from `false` to `true`, so any cached list that was filtered by KYC status could now be stale. Rather than tracking which keys contain that user, we evict the whole `users_list` cache so the next list call gets fresh data from the DB. So `@CacheEvict(value = "users_list", allEntries = true)` in `KYCService.verifyKYC()` keeps user lists consistent with KYC state.

---

## 12. What about caching inside the same class (self-invocation)?

**Answer:**  
Cache annotations are applied by **Spring proxies**. Only **external** calls to the bean go through the proxy. A call from one method to another method in the **same** class (e.g. `this.getAllLendersJson(...)`) does **not** go through the proxy, so `@Cacheable` / `@CacheEvict` would not run.

In this project we use cache only on **public** service methods that are called from controllers or other services, so we don’t have self-invocation. If we ever called a cached method from the same service, we’d need to inject the same service (or use `ApplicationContext` to get the proxy) and call the method on that proxy.

---

## 13. How do you handle `null` in cache keys (e.g. optional filters)?

**Answer:**  
Key SpEL uses string concatenation, e.g. `#lenderId + '-' + #lenderName + '-' + #lenderType`. If a parameter is `null`, it becomes the string `"null"` in the key (e.g. `null-null-BANK`). So we don’t get a null key; we get a unique key per combination of (id, name, type), including “all nulls” for “no filter.” That’s acceptable for our filter-based caching. For more control we could use a custom key generator that substitutes a placeholder for nulls.

---

## 14. Could we use a single cache for everything?

**Answer:**  
We could use one cache name and different keys, but we **chose separate caches** (e.g. `offers`, `lenders`, `users_list`) because:

- **Different TTLs:** Offers need shorter TTL than lenders; that’s only possible with separate cache configs.
- **Clear eviction:** We can evict “all lenders” or “all users” without touching offers.
- **Clarity:** Cache name reflects the domain; debugging and monitoring are easier.

So separate caches are a design choice for TTL, eviction, and maintainability.

---

## 15. Is Redis used only for caching in this project?

**Answer:**  
Yes. In this project Redis is used **only** as a cache backend via Spring’s cache abstraction (`RedisCacheManager`). We do not use `RedisTemplate` or direct Redis APIs for session store, pub/sub, or rate limiting. Connection is configured in `application.properties` and used by the cache manager.

---

## 16. How would you test that caching works?

**Answer:**  
We could:

1. **Unit tests:** Mock `CacheManager` / `Cache` and verify that the service is called only on cache miss and that eviction is triggered on write.
2. **Integration tests:** With an embedded or test Redis (e.g. Testcontainers), call the same read twice and assert the second call doesn’t hit the DB (e.g. via query count or repository mock), then call a write and assert the next read does hit the DB again.
3. **Manual:** Call an API twice with same params, check Redis (e.g. `redis-cli KEYS *`) for the key and TTL; after eviction or TTL, key should be gone and next request repopulates.

---

## 17. Production considerations (security, high availability)

**Answer:**  
Things a reviewer might expect you to mention:

- **Security:** Redis should not be exposed to the internet; use firewall/VPC. If needed, use Redis AUTH (`spring.data.redis.password`) and TLS.
- **HA:** For production, Redis Sentinel or Redis Cluster for failover and scalability; connection settings in properties.
- **Monitoring:** Use Actuator Redis health and metrics; monitor cache hit/miss if we add custom metrics.
- **Config:** Move host/port/password to environment or a secret store, not hardcoded.

---

## Quick reference – What is where

| Topic              | Where in project |
|--------------------|------------------|
| Redis connection   | Boot auto-config + `application.properties` (host, port, cache type) |
| Cache manager      | `RedisConfig.cacheManager(RedisConnectionFactory)` |
| TTL & serialization| `RedisConfig.createCacheConfig(Duration)` and per-cache config map |
| Enable caching     | `RedisConfig` class – `@EnableCaching` |
| Cache usage        | `OfferService`, `LenderService`, `UserService` – `@Cacheable` on list methods |
| Cache eviction     | `OfferService.createOffer`, `LenderService.registerLender`, `UserService.registerUser`, `KYCService.verifyKYC` – `@CacheEvict` |

Use this doc to prepare for “why you use Redis”, “how it’s configured”, and “how cache keys and eviction work” in your project review.
