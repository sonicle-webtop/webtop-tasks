
-- ----------------------------
-- Structures for categories
-- ----------------------------
DROP SEQUENCE IF EXISTS "tasks"."seq_categories" CASCADE;
CREATE SEQUENCE "tasks"."seq_categories";

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
WITH (OIDS=FALSE);

ALTER TABLE "tasks"."categories" ADD PRIMARY KEY ("category_id");
CREATE INDEX "categories_ak1" ON "tasks"."categories" USING btree ("domain_id", "user_id", "built_in");
CREATE UNIQUE INDEX "categories_ak2" ON "tasks"."categories" USING btree ("domain_id", "user_id", "name");

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP SEQUENCE IF EXISTS "tasks"."seq_tasks" CASCADE;
CREATE SEQUENCE "tasks"."seq_tasks";

DROP TABLE IF EXISTS "tasks"."tasks";
CREATE TABLE "tasks"."tasks" (
"task_id" int4 NOT NULL,
"category_id" int4 NOT NULL,
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
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
"reminded_on" timestamptz(6)
)
WITH (OIDS=FALSE);

ALTER TABLE "tasks"."tasks" ADD PRIMARY KEY ("task_id");
CREATE INDEX "tasks_ak1" ON "tasks"."tasks" USING btree ("category_id", "revision_status", "revision_timestamp");
