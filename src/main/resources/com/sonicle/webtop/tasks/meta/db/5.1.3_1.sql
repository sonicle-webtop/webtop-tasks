@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Table structure for category_props
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."category_props";
CREATE TABLE "tasks"."category_props" (
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
-- Indexes structure for table category_props
-- ----------------------------
CREATE INDEX "category_props_ak1" ON "tasks"."category_props" USING btree ("category_id");

-- ----------------------------
-- Primary Key structure for table category_props
-- ----------------------------
ALTER TABLE "tasks"."category_props" ADD PRIMARY KEY ("domain_id", "user_id", "category_id");

-- ----------------------------
-- Data move: user_settings -> category_props
-- ----------------------------
INSERT INTO "tasks"."category_props" 
("domain_id", "user_id", "category_id", "hidden", "color") 
SELECT domain_id, user_id, 
CAST (replace(replace("key",'category.folder.data@',''),',','') AS int4), 
CASE WHEN split_part(split_part(substr("value",2,length("value")-2),',',1),':',2)='true' THEN TRUE ELSE NULL END, 
CASE WHEN replace(split_part(split_part(substr("value",2,length("value")-2),',',2),':',2),'"','')='null' THEN NULL ELSE replace(split_part(split_part(substr("value",2,length("value")-2),',',2),':',2),'"','') END 
FROM "core"."user_settings" 
WHERE "service_id" = 'com.sonicle.webtop.tasks' AND "key" LIKE 'category.folder.data@%';

-- ----------------------------
-- Cleanup possibly orphans
-- ----------------------------
DELETE FROM "tasks"."category_props" WHERE "category_id" NOT IN (SELECT "category_id" from "tasks"."categories");

-- ----------------------------
-- Cleanup old settings
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.tasks') AND ("user_settings"."key" LIKE 'category.folder.data@%');
