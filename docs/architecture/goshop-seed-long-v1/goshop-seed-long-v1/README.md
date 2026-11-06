# GoShop — aligned local seed dataset (Sprint A)

This archive is a **proposed replacement seed dataset**, not a verified drop-in import. All entity and cross-service identifiers (except language codes and natural keys) are **Long / JSON integer**. Stable shared IDs are listed in `id-map.json`.

## Contents
- Seven directories matching the existing GoShop microservices.
- 21 JSON seed files (20 original filenames plus `product_product_attribute.json`).
- `id-map.json` for stable cross-service references.

## Important importer changes required
1. Several current loaders resolve names/slugs and auto-generate IDs. To preserve cross-service references, ensure explicit IDs are stored exactly as provided, or implement an ID mapping step. Reset sequences appropriately after import.
2. Update Kotlin/JPA entity IDs and DTOs referencing Product, ProductType, Category, Brand and Store (and all other UUID IDs) to Long, including database migrations, Kafka contracts and OpenAPI; JSON alone does not modify schemas.
3. Add or adapt import support for `product_product_attribute.json` (new file). It supplies the structured characteristics needed by compatibility checks.
4. New fields (`id`, `brand_id`, `category_id`, `product_type_id`, `parent_id`, `code`, `category_parameter_id`, and various dates) may not be accepted by current seed loaders. Reconcile these formats with actual importer code before applying.
5. The `compatibility_rule.parameters` and recommendation-rule `parameters` values remain JSON-encoded strings, following the original seed convention; parsers may instead expect objects.
6. Search documents are **derived data**: ideally rebuild from product-service events rather than import independently. The JSON is a deterministic integration fixture.
7. Pricing exchange rates are **synthetic test values**, not production or current market rates. Product descriptions/prices are illustrative.
8. Store webhooks are **disabled** and use `example.invalid` with a fake secret reference; no real webhook credentials are provided.
9. Compatibility checks are only meaningful after rules and real attribute lookups are implemented. A passing JSON reference validator does not prove service runtime behavior.
10. This dataset assumes fresh isolated databases. Do not import into existing populated DBs without checking primary-key collisions and existing unique constraints.

## Integration scenarios
- Compatible CPU + motherboard: products 1001 (AM5) + 1002 (AM5).
- Incompatible CPU + motherboard: 1001 (AM5) + 1012 (LGA1700).
- Compatible RAM + board: 1003 (DDR5) + 1002 (DDR5).
- Incompatible RAM + board: 1013 (DDR4) + 1002 (DDR5).
- GPU fits case: 1004 (304 mm) + 1006 (360 mm).
- GPU does not fit case: 1004 (304 mm) + 1014 (280 mm).
- GPU power scenario: 1004 (450 W) with PSU 1005 (1000 W) versus 1015 (550 W); full PC power calculations need all components.
- Product detail aggregation: product 1004, store 2001, language `uk`, USD price plus 5% demo discount.
- Search: query `RTX` returns GPU products 1004 and 1007.

## Import dependency order
Product: brands → categories → product types → parameter groups/items → category parameters → components → products → product attributes.
Store: stores → settings/webhooks. Localization: languages → translations. Pricing: stores/products must already exist logically (different databases) → prices/discounts. Search and recommendations: products first. Compatibility: product types and attributes first.

## Validation
All JSON documents were parsed, and the cross-service ID references were checked for membership in these seed fixtures. This does not validate database migrations or importer implementation.
