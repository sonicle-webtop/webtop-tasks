@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Cleanup any migrated data for testing purposes
-- ----------------------------
DELETE FROM "core"."shares" WHERE "service_id" = 'com.sonicle.webtop.tasks' AND "key" = 'CATEGORY';
DELETE FROM "core"."roles_permissions" WHERE "service_id" = 'com.sonicle.webtop.tasks' AND "key" = 'CATEGORY_FOLDERITEMS@SHARE';
DELETE FROM "core"."roles_permissions" WHERE "service_id" = 'com.sonicle.webtop.tasks' AND "key" = 'CATEGORY_FOLDER@SHARE';

-- ----------------------------
-- Add new cols for data migration
-- ----------------------------
ALTER TABLE "core"."shares" ADD COLUMN "old_share_id" int4;
ALTER TABLE "core"."shares" ADD COLUMN "old_wildcard_share_id" int4;

-- ----------------------------
-- Convert shares on ROOT to new row wildcard instance, remembering old share_id
-- ----------------------------
INSERT INTO "core"."shares" ("user_uid", "service_id", "key", "instance", "old_share_id")
SELECT "user_uid", "service_id", 'CATEGORY', '*', "share_id"
FROM "core"."shares"
WHERE "service_id" = 'com.sonicle.webtop.tasks' AND "key" = 'CATEGORY@ROOT';

-- ----------------------------
-- Convert shares on FOLDER to new row, remembering old share_id
-- ----------------------------
INSERT INTO "core"."shares" ("user_uid", "service_id", "key", "instance", "old_share_id")
SELECT "user_uid", "service_id", 'CATEGORY', "instance", "share_id"
FROM "core"."shares"
WHERE "service_id" = 'com.sonicle.webtop.tasks' AND "key" = 'CATEGORY@FOLDER' AND "instance" <> '*';

-- ----------------------------
-- Merges shares on FOLDER with wildcard into wildcard rows (done before), remembering old wildcard_share_id
-- ----------------------------
UPDATE "core"."shares" SET
"old_wildcard_share_id" = sub."share_id"
FROM (
SELECT "user_uid", "share_id"
FROM "core"."shares"
WHERE "service_id" = 'com.sonicle.webtop.tasks' AND "key" = 'CATEGORY@FOLDER' AND "instance" = '*'
) AS sub
WHERE "shares"."user_uid" = sub."user_uid" AND "service_id" = 'com.sonicle.webtop.tasks' AND "key" = 'CATEGORY' AND "instance" = '*';

-- ----------------------------
-- Consolidate permissions (CATEGORY@SHARE_ROOT, CATEGORY@SHARE_FOLDER, CATEGORY@SHARE_ELEMENTS keys are deprecated)
-- ----------------------------
INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CATEGORY_FOLDERITEMS@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CATEGORY' AND rp."instance"::integer = sh."old_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.tasks' AND rp."key" = 'CATEGORY@SHARE_ELEMENTS';

INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CATEGORY_FOLDERITEMS@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CATEGORY' AND rp."instance"::integer = sh."old_wildcard_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.tasks' AND rp."key" = 'CATEGORY@SHARE_ELEMENTS';

INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CATEGORY_FOLDER@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CATEGORY' AND rp."instance"::integer = sh."old_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.tasks' AND rp."key" = 'CATEGORY@SHARE_FOLDER';

INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CATEGORY_FOLDER@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CATEGORY' AND rp."instance"::integer = sh."old_wildcard_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.tasks' AND rp."key" = 'CATEGORY@SHARE_FOLDER';

INSERT INTO "core"."roles_permissions" ("role_uid", "service_id", "key", "action", "instance")
SELECT rp."role_uid", rp."service_id", 'CATEGORY_FOLDER@SHARE', rp."action", sh."share_id"
FROM "core"."roles_permissions" AS rp
INNER JOIN "core"."shares" AS sh
ON rp."service_id" = sh."service_id" AND sh."key" = 'CATEGORY' AND rp."instance"::integer = sh."old_share_id"
WHERE rp."service_id" = 'com.sonicle.webtop.tasks' AND rp."key" = 'CATEGORY@SHARE_ROOT';

-- ----------------------------
-- Drop new cols for data migration
-- ----------------------------
ALTER TABLE "core"."shares" DROP COLUMN "old_share_id", DROP COLUMN "old_wildcard_share_id";

-- ----------------------------
-- Migrate settings (drop old settings in upcoming release)
-- ----------------------------
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.tasks') AND ("user_settings"."key" = 'category.origins.inactive');
INSERT INTO "core"."user_settings" ("domain_id", "user_id", "service_id", "key", "value")
SELECT
aggr."setting_domain_id",
aggr."setting_user_id",
'com.sonicle.webtop.tasks' as service_id,
'category.origins.inactive' as "key",
'[' || string_agg('"' || aggr."origin_user_id" || '@' || aggr."setting_domain_id" || '"', ',') || ']' AS "value"
FROM (
SELECT
cus_right."setting_domain_id",
cus_right."setting_user_id",
(CASE cus_right."setting_share_id" WHEN 0 THEN cus_right."setting_user_id" ELSE cu."user_id" END) AS origin_user_id
FROM "core"."shares" cs
RIGHT JOIN (
SELECT
cus."domain_id" as setting_domain_id,
cus."user_id" as setting_user_id,
UNNEST(string_to_array(REPLACE(SUBSTRING(cus."value" FROM 2 FOR (LENGTH( cus."value" ) -2)), '"', ''), ','))::int AS setting_share_id 
FROM "core"."user_settings" cus 
WHERE cus."service_id" = 'com.sonicle.webtop.tasks' AND cus."key" = 'category.roots.inactive'
) cus_right ON cs."share_id" = cus_right."setting_share_id" AND cs."service_id" = 'com.sonicle.webtop.tasks' AND cs."key" = 'CATEGORY@ROOT'
LEFT JOIN "core"."users" cu ON cs."user_uid" = cu."user_uid"
) aggr
WHERE aggr."origin_user_id" IS NOT NULL
GROUP BY aggr."setting_domain_id", aggr."setting_user_id";
