# How to Explain Different Cache TTLs in an Interview

## What we have in the project

| Cache    | TTL    | Where it's set |
|----------|--------|-----------------|
| **offers**  | 5 min  | `cache.redis.offers-ttl-minutes=5` |
| **lenders** | 15 min | `cache.redis.lenders-ttl-minutes=15` |
| **default** (any other cache) | 10 min | `cache.redis.default-ttl-minutes=10` |

---

## 1. Why different TTLs for different caches?

**Short answer:**  
Because the **data changes at different rates**. We want a balance: **fresh enough** for the business, but **long enough** to reduce DB and Redis load.

- Data that **changes more often** → **shorter TTL** (e.g. offers: 5 min).
- Data that **changes less often** → **longer TTL** (e.g. lenders: 15 min).

So we didn’t pick one TTL for everything; we tuned **per cache** based on how often that data is updated.

---

## 2. Why 5 minutes for offers?

**Interview answer:**

- **Offers change more frequently:** New offers are created, interest rates or status can change, offers can be activated/deactivated. In a loan app, offer data directly affects what the user sees (rates, tenures, eligibility).
- **Stale = bad:** Showing an old interest rate or an offer that’s no longer active is a real problem. So we want this cache to expire **sooner**.
- **5 minutes** is a practical choice: we still get cache benefit (repeated calls within 5 min don’t hit DB), but data is refreshed often enough that we don’t serve badly outdated offers. We also evict the offers cache on `createOffer`, so after a new offer, the next read repopulates; TTL is a safety net for other updates.

**One line:** “Offers change more often and affect user-facing rates and eligibility, so we use a shorter TTL of 5 minutes to keep data fresh while still reducing DB load.”

---

## 3. Why 15 minutes for lenders?

**Interview answer:**

- **Lenders change less often:** Adding a new lender is relatively rare compared to listing offers. Lender names and types are fairly static.
- **Stale is less critical:** A new lender not showing for a few extra minutes is usually acceptable; it’s not as sensitive as wrong interest rates.
- **Longer TTL = more cache benefit:** Fewer DB hits and fewer Redis reads for the same data. So we can afford a **longer** TTL.
- **15 minutes** fits that: we still evict on `registerLender`, so new lenders show up immediately; TTL mainly limits how long other kinds of staleness can last and keeps memory usage bounded.

**One line:** “Lenders are relatively static, so we use a longer TTL of 15 minutes to reduce load while keeping data acceptable. We still invalidate on new lender registration.”

---

## 4. Why 10 minutes for default?

**Interview answer:**

- We only configure **specific** TTLs for caches we know well: **offers** and **lenders**.
- Any **new cache** we add later (e.g. `users_list`, or some other list) will use the **default** TTL if we don’t give it its own config.
- **10 minutes** is a **middle ground**: not as aggressive as 5 (offers), not as long as 15 (lenders). It’s a safe default until we decide that a new cache needs a shorter or longer TTL based on its update frequency and business impact.

**One line:** “Default TTL is 10 minutes as a balanced fallback for any cache we add later, until we tune it based on how often that data changes.”

---

## 5. How did we “define” the timing? (Process you can describe)

**Interview answer:**

1. **Identify how often data changes**  
   Offers: often (new offers, rate/status changes). Lenders: rarely (new lenders occasionally).

2. **Consider impact of staleness**  
   Stale offers = wrong rates/eligibility → high impact → prefer shorter TTL. Stale lender list = new lender appears a bit late → lower impact → can use longer TTL.

3. **Choose a range**  
   We didn’t do complex maths; we picked a **reasonable range**: 5 min (frequently changing), 10 min (default), 15 min (slowly changing). In production we could tune further using metrics (e.g. cache hit rate, frequency of evictions).

4. **Make it configurable**  
   We didn’t hardcode 5 or 15 in Java; we used **properties** (`cache.redis.offers-ttl-minutes`, etc.) so we can change TTLs per environment (e.g. 2 min in prod for offers, 5 in dev) without code change.

So we “defined” the timing by **business sense + impact of staleness + configurability**, not by a single formula.

---

## 6. Ready-to-say interview answers

**Q: “You have different TTLs for different caches. Why?”**

**A:** “Because the data in each cache changes at different rates. Offers change more often—new offers, rate updates—and showing stale offers affects user decisions, so we use a shorter TTL of 5 minutes. Lenders change less often, so we use 15 minutes to get more cache benefit. The default of 10 minutes is for any other cache we add later, as a balanced starting point.”

---

**Q: “How did you decide on 5 minutes vs 10 vs 15?”**

**A:** “We looked at how often each type of data is updated and how bad it is if it’s stale. Offers are time-sensitive, so we chose the shortest TTL, 5 minutes. Lenders are relatively static, so we went with 15 minutes. We also made these values configurable in properties so we can tune them per environment without changing code.”

---

**Q: “What if 5 minutes is too long or too short for offers?”**

**A:** “We’ve made the TTL configurable—`cache.redis.offers-ttl-minutes` in application properties. So we can reduce it to 2 or 3 minutes if we see staleness issues, or increase it if the DB is under load and we’re okay with slightly older data. We also evict the offers cache when a new offer is created, so critical updates are reflected immediately; TTL is for the rest of the updates.”

---

## 7. Summary table (for quick revision)

| Cache   | TTL   | Reason in one line |
|---------|-------|--------------------|
| offers  | 5 min | Changes often; stale offers = wrong rates → keep short. |
| lenders | 15 min| Changes rarely; longer TTL = less DB/Redis load. |
| default | 10 min| Safe middle ground for any new cache until we tune it. |

Use this doc to explain **why** you defined different timings and **how** you’d explain it in an interview.
