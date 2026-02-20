# Redis in This Project: spring-boot-starter-data-redis vs Jedis

**Short answer for your discussion:**  
We use **spring-boot-starter-data-redis**, which uses **Lettuce** as the Redis client by default (since Spring Boot 2.x). We are **not** using Jedis. The main difference is the **connection model**: Lettuce is non-blocking and uses a small number of shared connections; Jedis is blocking and typically uses one connection per thread (or a pool). Spring Boot prefers Lettuce for better scalability and async support.

---

## 1. What does spring-boot-starter-data-redis give you?

- **Spring Data Redis abstraction:** `RedisConnectionFactory`, `RedisTemplate`, and cache support (`RedisCacheManager`) work the same regardless of the underlying client.
- **A default Redis client:** Spring Boot does **not** bundle Jedis by default. It bundles **Lettuce**. So in our project, when we use `RedisConnectionFactory` or `RedisCacheManager`, the actual TCP connection to Redis is made by **Lettuce**, not Jedis.

So: **“We use spring-boot-starter-data-redis”** = we use Spring’s Redis support with the **default client, which is Lettuce**.

---

## 2. What is Jedis? What is Lettuce?

Both are **Java Redis clients** that talk to Redis over TCP. Spring Data Redis can use either; the difference is how they manage connections and threads.

| Aspect | **Lettuce** (what we use) | **Jedis** |
|--------|---------------------------|-----------|
| **Connection model** | **Non-blocking (async), netty-based**; a small pool of connections can serve many operations. | **Blocking**; one thread is blocked per operation. Often “one connection per thread” or a connection pool. |
| **Threading** | Few threads can handle many concurrent Redis calls (event loop). | Typically one thread per in-flight request; more threads under load. |
| **Async support** | Native async/reactive APIs. | Only sync by default; “async” is often just running sync calls on a thread pool. |
| **Connection pooling** | Built-in; connections are shared and multiplexed. | You often use a pool (e.g. JedisPool) to avoid creating a connection per request. |
| **Dependency** | Comes with `spring-boot-starter-data-redis`. | Must add `jedis` and usually **exclude** Lettuce to use Jedis. |

So the **main difference** is: **Lettuce = non-blocking, few connections, good for many concurrent requests; Jedis = blocking, more connections/threads under load.**

---

## 3. Why does our project use this mechanism (Lettuce via the starter)?

We use **spring-boot-starter-data-redis** and do **not** add Jedis. So we use **Lettuce**:

1. **Default in Spring Boot 2.x+**  
   No extra dependency or configuration; it works out of the box with `spring.data.redis.host` and `port`.

2. **Better scalability**  
   Non-blocking I/O means we don’t need one thread per Redis call. Under load, we need fewer threads and fewer connections than with a blocking, one-connection-per-thread model (typical with Jedis).

3. **Same API for us**  
   Our code uses `RedisConnectionFactory` and `RedisCacheManager`. We don’t touch Lettuce or Jedis directly. If we switched to Jedis, we’d only change the dependency and connection factory implementation; our `RedisConfig` and `@Cacheable` usage would stay the same.

4. **Async/reactive ready**  
   If we ever need reactive or async Redis (e.g. with WebFlux), Lettuce supports it natively. Jedis is primarily synchronous.

So in the discussion you can say: **“We use spring-boot-starter-data-redis, which uses Lettuce by default. We didn’t add Jedis. Lettuce is non-blocking and scales better with fewer connections and threads, and it’s the default recommended by Spring Boot.”**

---

## 4. How would “implementing with Jedis” be different?

**Conceptually:**  
We’d still use the **same** Spring APIs: `RedisConnectionFactory`, `RedisCacheManager`, `@Cacheable`, etc. So the **mechanism** (Spring Data Redis + cache abstraction) is the same; only the **underlying client** would change.

**Concretely:**

- **Current (Lettuce):**  
  - Dependencies: only `spring-boot-starter-data-redis` (brings Lettuce).  
  - Boot auto-configures `LettuceConnectionFactory` and injects it into our `RedisConfig.cacheManager(RedisConnectionFactory)`.

- **With Jedis:**  
  - Add `jedis` client dependency.  
  - Exclude Lettuce from `spring-boot-starter-data-redis` so only Jedis is on the classpath.  
  - Spring Boot would then auto-configure `JedisConnectionFactory` instead of Lettuce.  
  - Our `RedisConfig` and all `@Cacheable` / `@CacheEvict` code would **remain unchanged**; we’d still inject `RedisConnectionFactory` and build `RedisCacheManager` the same way.

So the **difference** is not in “how we use Redis in the app” (that’s the same), but in **which library actually opens the TCP connection and how it handles concurrency**: Lettuce = non-blocking, fewer connections; Jedis = blocking, more connections/threads.

---

## 5. When might someone choose Jedis?

- Legacy or team familiarity with Jedis.  
- Very simple, low-concurrency usage where blocking I/O is acceptable.  
- Some Redis features or tuning might be more familiar with Jedis (though Spring abstracts most of it).

For a typical Spring Boot app (like our loan booking app) that uses Redis for caching, **Lettuce via spring-boot-starter-data-redis is the recommended and default choice**, and we use that mechanism for better scalability and to align with Spring Boot’s defaults.

---

## 6. One-line answers for the discussion

- **“What Redis client do we use?”**  
  We use **Lettuce**, which comes by default with **spring-boot-starter-data-redis**. We don’t use Jedis.

- **“What’s the difference between using the starter and using Jedis?”**  
  The starter uses **Lettuce** (non-blocking, fewer connections, better for concurrency). Using Jedis means a **blocking** client, often one connection per thread or a pool. Our code (e.g. `RedisCacheManager`) stays the same; only the underlying connection implementation changes.

- **“Why do we use this mechanism?”**  
  We use Spring’s default: **spring-boot-starter-data-redis** with **Lettuce**. It’s non-blocking, scales better, and requires no extra configuration. Our application code only depends on `RedisConnectionFactory` and the cache abstraction, not on a specific client.

Use this doc to explain the difference and why your project uses the current mechanism in the discussion.
