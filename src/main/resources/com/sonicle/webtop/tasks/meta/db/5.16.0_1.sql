@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Add revision_timestamp and creation_timestamp columns
-- ----------------------------
ALTER TABLE "tasks"."categories" 
ADD COLUMN "revision_timestamp" timestamptz,
ADD COLUMN "creation_timestamp" timestamptz;

UPDATE "tasks"."categories" SET
"revision_timestamp" = '1970-01-01 00:00:00+00'::timestamptz,
"creation_timestamp" = '1970-01-01 00:00:00+00'::timestamptz
WHERE "revision_timestamp" IS NULL;

ALTER TABLE "tasks"."categories" 
ALTER COLUMN "revision_timestamp" SET NOT NULL,
ALTER COLUMN "revision_timestamp" SET DEFAULT now(),
ALTER COLUMN "creation_timestamp" SET NOT NULL,
ALTER COLUMN "creation_timestamp" SET DEFAULT now();

-- ----------------------------
-- New table: history_categories
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."history_categories";
DROP SEQUENCE IF EXISTS "tasks"."seq_history_categories";

CREATE SEQUENCE "tasks"."seq_history_categories";
CREATE TABLE "tasks"."history_categories" (
"id" int8 NOT NULL DEFAULT nextval('"tasks".seq_history_categories'::regclass),
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"category_id" int4 NOT NULL,
"change_timestamp" timestamptz NOT NULL DEFAULT '1970-01-01 00:00:00+00'::timestamptz,
"change_type" char(1) NOT NULL
);

ALTER TABLE "tasks"."history_categories" ADD PRIMARY KEY ("id");
CREATE INDEX "history_categories_ak1" ON "tasks"."history_categories" USING btree ("category_id", "change_timestamp");
CREATE INDEX "history_categories_ak2" ON "tasks"."history_categories" USING btree ("domain_id", "user_id", "category_id", "change_timestamp");

-- ----------------------------
-- New table: history_tasks
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."history_tasks";
DROP SEQUENCE IF EXISTS "tasks"."history_tasks";

CREATE SEQUENCE "tasks"."seq_history_tasks";
CREATE TABLE "tasks"."history_tasks" (
"id" int8 NOT NULL DEFAULT nextval('"tasks".seq_history_tasks'::regclass),
"category_id" int4 NOT NULL,
"task_id" varchar(32) NOT NULL,
"change_timestamp" timestamptz NOT NULL DEFAULT '1970-01-01 00:00:00+00'::timestamptz,
"change_type" char(1) NOT NULL
);

ALTER TABLE "tasks"."history_tasks" ADD PRIMARY KEY ("id");
CREATE INDEX "history_tasks_ak1" ON "tasks"."history_tasks" USING btree ("category_id", "change_timestamp");
CREATE INDEX "history_tasks_ak2" ON "tasks"."history_tasks" USING btree ("task_id", "change_timestamp");
