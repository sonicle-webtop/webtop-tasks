@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Table structure for category_property_sets
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."category_property_sets";
CREATE TABLE "tasks"."category_property_sets" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"category_id" int4 NOT NULL,
"hidden" bool,
"color" varchar(20),
"sync" varchar(1)
)
WITH (OIDS=FALSE)
;

-- ----------------------------
-- Indexes structure for table category_property_sets
-- ----------------------------
CREATE INDEX "category_property_sets_ak1" ON "tasks"."category_property_sets" USING btree ("category_id");

-- ----------------------------
-- Primary Key structure for table category_property_sets
-- ----------------------------
ALTER TABLE "tasks"."category_property_sets" ADD PRIMARY KEY ("domain_id", "user_id", "category_id");

-- ----------------------------
-- Cleanup old settings
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.tasks') AND ("user_settings"."key" LIKE 'category.folder.data@%');
