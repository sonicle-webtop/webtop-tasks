
CREATE SCHEMA "tasks";

-- ----------------------------
-- Sequence structure for seq_categories
-- ----------------------------
DROP SEQUENCE IF EXISTS "tasks"."seq_categories";
CREATE SEQUENCE "tasks"."seq_categories";

-- ----------------------------
-- Sequence structure for seq_tasks
-- ----------------------------
DROP SEQUENCE IF EXISTS "tasks"."seq_tasks";
CREATE SEQUENCE "tasks"."seq_tasks";

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."categories";
CREATE TABLE "tasks"."categories" (
"category_id" int4 DEFAULT nextval('"tasks".seq_categories'::regclass) NOT NULL,
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"built_in" bool NOT NULL,
"name" varchar(100) NOT NULL,
"description" varchar(255),
"color" varchar(20),
"sync" varchar(1) NOT NULL,
"is_private" bool NOT NULL,
"is_default" bool NOT NULL
)
WITH (OIDS=FALSE)

;

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
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."tasks";
CREATE TABLE "tasks"."tasks" (
"task_id" int4 DEFAULT nextval('"tasks".seq_tasks'::regclass) NOT NULL,
"category_id" int4 NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int4 DEFAULT 0 NOT NULL,
"creation_timestamp" timestamptz DEFAULT now() NOT NULL,
"public_uid" varchar(255) NOT NULL,
"subject" varchar(100) NOT NULL,
"description" text,
"start_date" timestamptz(6),
"due_date" timestamptz(6),
"completed_date" timestamptz(6),
"importance" int2 NOT NULL,
"is_private" bool DEFAULT false NOT NULL,
"status" varchar(15) NOT NULL,
"completion_percentage" int2 NOT NULL,
"reminder_date" timestamptz(6),
"reminded_on" timestamptz(6),
"href" varchar(2048),
"etag" varchar(2048)
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for tasks_attachments
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."tasks_attachments";
CREATE TABLE "tasks"."tasks_attachments" (
"task_attachment_id" varchar(36) NOT NULL,
"task_id" int4 NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int2 NOT NULL,
"filename" varchar(255) NOT NULL,
"size" int8 NOT NULL,
"media_type" varchar(255) NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for tasks_attachments_data
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."tasks_attachments_data";
CREATE TABLE "tasks"."tasks_attachments_data" (
"task_attachment_id" varchar(36) NOT NULL,
"bytes" bytea NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Table structure for tasks_icalendars
-- ----------------------------
CREATE TABLE "tasks"."tasks_icalendars" (
"task_id" int4 NOT NULL,
"raw_data" text
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Indexes structure for table categories
-- ----------------------------
CREATE INDEX "categories_ak1" ON "tasks"."categories" USING btree ("domain_id", "user_id", "built_in");
CREATE UNIQUE INDEX "categories_ak2" ON "tasks"."categories" USING btree ("domain_id", "user_id", "name");

-- ----------------------------
-- Primary Key structure for table categories
-- ----------------------------
ALTER TABLE "tasks"."categories" ADD PRIMARY KEY ("category_id");

-- ----------------------------
-- Indexes structure for table category_props
-- ----------------------------
CREATE INDEX "category_props_ak1" ON "tasks"."category_props" USING btree ("category_id");

-- ----------------------------
-- Primary Key structure for table category_props
-- ----------------------------
ALTER TABLE "tasks"."category_props" ADD PRIMARY KEY ("domain_id", "user_id", "category_id");

-- ----------------------------
-- Indexes structure for table tasks
-- ----------------------------
CREATE INDEX "tasks_ak1" ON "tasks"."tasks" USING btree ("category_id", "revision_status", "revision_timestamp");
CREATE INDEX "tasks_ak2" ON "tasks"."tasks" USING btree ("category_id", "revision_status", "href");

-- ----------------------------
-- Primary Key structure for table tasks
-- ----------------------------
ALTER TABLE "tasks"."tasks" ADD PRIMARY KEY ("task_id");

-- ----------------------------
-- Indexes structure for table tasks_attachments
-- ----------------------------
CREATE INDEX "tasks_attachments_ak1" ON "tasks"."tasks_attachments" USING btree ("task_id");

-- ----------------------------
-- Primary Key structure for table tasks_attachments
-- ----------------------------
ALTER TABLE "tasks"."tasks_attachments" ADD PRIMARY KEY ("task_attachment_id");

-- ----------------------------
-- Foreign Key structure for table "tasks"."tasks_attachments"
-- ----------------------------
ALTER TABLE "tasks"."tasks_attachments" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Primary Key structure for table tasks_attachments_data
-- ----------------------------
ALTER TABLE "tasks"."tasks_attachments_data" ADD PRIMARY KEY ("task_attachment_id");

-- ----------------------------
-- Foreign Key structure for table "tasks"."tasks_attachments_data"
-- ----------------------------
ALTER TABLE "tasks"."tasks_attachments_data" ADD FOREIGN KEY ("task_attachment_id") REFERENCES "tasks"."tasks_attachments" ("task_attachment_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Primary Key structure for table tasks_icalendars
-- ----------------------------
ALTER TABLE "tasks"."tasks_icalendars" ADD PRIMARY KEY ("task_id");

-- ----------------------------
-- Align service version
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
DELETE FROM "core"."settings" WHERE ("settings"."service_id" = 'com.sonicle.webtop.tasks') AND ("settings"."key" = 'manifest.version');
INSERT INTO "core"."settings" ("service_id", "key", "value") VALUES ('com.sonicle.webtop.tasks', 'manifest.version', '5.3.0');
