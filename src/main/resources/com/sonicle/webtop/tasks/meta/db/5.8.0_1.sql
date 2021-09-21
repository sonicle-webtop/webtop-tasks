@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Historicize schemas
-- ----------------------------
ALTER SCHEMA "tasks" RENAME TO "tasks_old";
CREATE SCHEMA "tasks";
ALTER TABLE "tasks_old"."tasks" ADD COLUMN "task_id_NEW" varchar(32);

-- ----------------------------
-- Prepare new tasks IDs
-- ----------------------------
UPDATE "tasks_old"."tasks"
SET "task_id_NEW" = replace(CAST(uuid_in(md5(random()::text || clock_timestamp()::text)::cstring) as VARCHAR), '-', '');

-- ----------------------------
-- Sequence seq_categories
-- ----------------------------
CREATE SEQUENCE "tasks"."seq_categories";
SELECT setval('"tasks".seq_categories'::regclass, (SELECT MAX("category_id") FROM "tasks_old"."categories")+1);

-- ----------------------------
-- Table categories
-- ----------------------------
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
"is_default" bool DEFAULT false NOT NULL
);

ALTER TABLE "tasks"."categories" ADD PRIMARY KEY ("category_id");
CREATE INDEX "categories_ak1" ON "tasks"."categories" USING btree ("domain_id", "user_id", "built_in");
CREATE UNIQUE INDEX "categories_ak2" ON "tasks"."categories" USING btree ("domain_id", "user_id", "name");

-- ----------------------------
-- Table structure for category_props
-- ----------------------------
CREATE TABLE "tasks"."category_props" (
"domain_id" varchar(20) NOT NULL,
"user_id" varchar(100) NOT NULL,
"category_id" int4 NOT NULL,
"hidden" bool,
"color" varchar(20),
"sync" varchar(1)
);

ALTER TABLE "tasks"."category_props" ADD PRIMARY KEY ("domain_id", "user_id", "category_id");
CREATE INDEX "category_props_ak1" ON "tasks"."category_props" USING btree ("category_id");
ALTER TABLE "tasks"."category_props" ADD FOREIGN KEY ("category_id") REFERENCES "tasks"."categories" ("category_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
CREATE TABLE "tasks"."tasks" (
"task_id" varchar(32) NOT NULL,
"category_id" int4 NOT NULL,
"series_task_id" varchar(32),
"series_instance_id" varchar(8),
"parent_task_id" varchar(32),
"public_uid" varchar(255),
"revision_status" varchar(1) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int4 DEFAULT 0 NOT NULL,
"creation_timestamp" timestamptz(6) DEFAULT now() NOT NULL,
"organizer" varchar(650),
"organizer_id" varchar(100),
"subject" varchar(100),
"location" varchar(255),
"description_type" varchar(4) NOT NULL,
"description" text,
"timezone" varchar(50) NOT NULL,
"start" timestamptz(6),
"due" timestamptz(6),
"completed_on" timestamptz(6),
"progress" int2 DEFAULT 0 NOT NULL,
"status" varchar(2) DEFAULT 'NA' NOT NULL,
"importance" int2 DEFAULT 0 NOT NULL,
"is_private" bool DEFAULT false NOT NULL,
"document_ref" varchar(100),
"href" varchar(2048),
"etag" varchar(255),
"reminder" int4,
"reminded_on" timestamptz(6),
"contact" varchar(255),
"contact_id" varchar(255),
"company" varchar(255),
"company_id" varchar(255)
);

ALTER TABLE "tasks"."tasks" ADD PRIMARY KEY ("task_id");
ALTER TABLE "tasks"."tasks" ADD FOREIGN KEY ("parent_task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE SET NULL ON UPDATE CASCADE;
CREATE UNIQUE INDEX "tasks_ak1" ON "tasks"."tasks" ("series_task_id", "series_instance_id");
CREATE INDEX "tasks_ak2" ON "tasks"."tasks" ("category_id", "revision_status", "href");
CREATE INDEX "tasks_ak3" ON "tasks"."tasks" ("revision_status", "start", "reminder", "reminded_on");

/*
CREATE INDEX "tasks_ak1" ON "tasks"."tasks" ("category_id", "revision_status", "recurrence_id", "start_date", "end_date");
CREATE INDEX "tasks_ak3" ON "tasks"."tasks" ("category_id", "revision_status", "recurrence_id", "title");
CREATE INDEX "tasks_ak2" ON "tasks"."tasks" ("category_id", "revision_status", "start_date", "end_date", "reminder", "reminded_on");
*/

/*
ALTER TABLE "tasks"."tasks"
ADD COLUMN "owner_id" varchar(100),
ADD COLUMN "owner" varchar(320),
ADD COLUMN "public_uid" varchar(255),
ADD COLUMN "subject" varchar(100),
ADD COLUMN "body" text,
ADD COLUMN "body_type" varchar(4) NOT NULL,
ADD COLUMN "start" timestamptz(6),
ADD COLUMN "due" timestamptz(6),
ADD COLUMN "completed_on" timestamptz(6),
ADD COLUMN "completion_perc" int2 DEFAULT 0 NOT NULL,
ADD COLUMN "status" varchar(15) DEFAULT 'NS' NOT NULL,
ADD COLUMN "importance" int2 DEFAULT 0 NOT NULL,
ADD COLUMN "is_private" bool DEFAULT false NOT NULL,
ADD COLUMN "href" varchar(2048),
ADD COLUMN "etag" varchar(255),
ADD COLUMN "reminder" timestamptz(6),
ADD COLUMN "reminded_on" timestamptz(6)
*/

-- ----------------------------
-- Table structure for tasks_assignees
-- ----------------------------
CREATE TABLE "tasks"."tasks_assignees" (
"assignee_id" varchar(32) NOT NULL,
"task_id" varchar(32) NOT NULL,
"recipient" varchar(650),
"recipient_user_id" varchar(100),
"response_status" varchar(2) DEFAULT 'NA' NOT NULL
);

ALTER TABLE "tasks"."tasks_assignees" ADD PRIMARY KEY ("assignee_id");
ALTER TABLE "tasks"."tasks_assignees" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "tasks_assignees_ak1" ON "tasks"."tasks_assignees" ("task_id");

-- ----------------------------
-- Table structure for tasks_attachments
-- ----------------------------
CREATE TABLE "tasks"."tasks_attachments" (
"task_attachment_id" varchar(32) NOT NULL,
"task_id" varchar(32) NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int2 NOT NULL,
"filename" varchar(255) NOT NULL,
"size" int8 NOT NULL,
"media_type" varchar(255) NOT NULL
);

ALTER TABLE "tasks"."tasks_attachments" ADD PRIMARY KEY ("task_attachment_id");
CREATE INDEX "tasks_attachments_ak1" ON "tasks"."tasks_attachments" ("task_id");
ALTER TABLE "tasks"."tasks_attachments" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table structure for tasks_attachments_data
-- ----------------------------
CREATE TABLE "tasks"."tasks_attachments_data" (
"task_attachment_id" varchar(32) NOT NULL,
"bytes" bytea NOT NULL
);

ALTER TABLE "tasks"."tasks_attachments_data" ADD PRIMARY KEY ("task_attachment_id");
ALTER TABLE "tasks"."tasks_attachments_data" ADD FOREIGN KEY ("task_attachment_id") REFERENCES "tasks"."tasks_attachments" ("task_attachment_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table structure for tasks_custom_values
-- ----------------------------
CREATE TABLE "tasks"."tasks_custom_values" (
"task_id" varchar(32) NOT NULL,
"custom_field_id" varchar(22) NOT NULL,
"string_value" varchar(255),
"number_value" float8,
"boolean_value" bool,
"date_value" timestamptz(6),
"text_value" text
);

ALTER TABLE "tasks"."tasks_custom_values" ADD PRIMARY KEY ("task_id", "custom_field_id");
ALTER TABLE "tasks"."tasks_custom_values" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "tasks"."tasks_custom_values" ADD FOREIGN KEY ("custom_field_id") REFERENCES "core"."custom_fields" ("custom_field_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table structure for tasks_icalendars
-- ----------------------------
CREATE TABLE "tasks"."tasks_icalendars" (
"task_id" varchar(32) NOT NULL,
"raw_data" text
);

ALTER TABLE "tasks"."tasks_icalendars" ADD PRIMARY KEY ("task_id");
ALTER TABLE "tasks"."tasks_icalendars" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table structure for tasks_recurrences
-- ----------------------------
CREATE TABLE "tasks"."tasks_recurrences" (
"task_id" varchar(32) NOT NULL,
"start" timestamptz(6) NOT NULL,
"until" timestamptz(6) NOT NULL,
"rule" varchar(255) NOT NULL
);

ALTER TABLE "tasks"."tasks_recurrences" ADD PRIMARY KEY ("task_id");
ALTER TABLE "tasks"."tasks_recurrences" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;
CREATE INDEX "tasks_recurrences_ak1" ON "tasks"."tasks_recurrences" ("start", "until");

-- ----------------------------
-- Table structure for tasks_recurrences_ex
-- ----------------------------
CREATE TABLE "tasks"."tasks_recurrences_ex" (
"task_id" varchar(32) NOT NULL,
"date" date NOT NULL
);

ALTER TABLE "tasks"."tasks_recurrences_ex" ADD PRIMARY KEY ("task_id", "date");
ALTER TABLE "tasks"."tasks_recurrences_ex" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Table structure for tasks_tags
-- ----------------------------
CREATE TABLE "tasks"."tasks_tags" (
"task_id" varchar(32) NOT NULL,
"tag_id" varchar(22) NOT NULL
);

ALTER TABLE "tasks"."tasks_tags" ADD PRIMARY KEY ("task_id", "tag_id");
CREATE INDEX "tasks_tags_ak1" ON "tasks"."tasks_tags" USING btree ("tag_id");
ALTER TABLE "tasks"."tasks_tags" ADD FOREIGN KEY ("tag_id") REFERENCES "core"."tags" ("tag_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Copy data from OLD categories
-- ----------------------------
INSERT INTO "tasks"."categories" ("category_id", "domain_id", "user_id", "built_in", "name", "description", "color", "sync", "is_private")
SELECT "category_id", "domain_id", "user_id", "built_in", "name", "description", "color", "sync", "is_private"
FROM "tasks_old"."categories";

-- ----------------------------
-- Copy data from OLD category_props
-- ----------------------------
INSERT INTO "tasks"."category_props" ("domain_id", "user_id", "category_id", "hidden", "color", "sync")
SELECT "domain_id", "user_id", "category_id", "hidden", "color", "sync"
FROM "tasks_old"."category_props";

-- ----------------------------
-- Copy data from OLD tasks
-- ----------------------------
INSERT INTO "tasks"."tasks" ("task_id", "category_id", "series_task_id", "series_instance_id", "parent_task_id", "public_uid", "revision_status", "revision_timestamp", "revision_sequence", "creation_timestamp", "organizer", "organizer_id", "subject", "location", "description_type", "description", "timezone", "start", "due", "completed_on", "progress", "status", "importance", "is_private", "href", "etag", "reminder", "reminded_on", "contact", "contact_id", "company", "company_id")
SELECT ot1."task_id_NEW", ot1."category_id", NULL, NULL, NULL, ot1."public_uid", ot1."revision_status", ot1."revision_timestamp", ot1."revision_sequence", ot1."creation_timestamp", 
NULL, ot2."user_id", 
ot1."subject", NULL, 'text', ot1."description", 
coalesce((SELECT "value" FROM "core"."settings" WHERE "service_id" = 'com.sonicle.webtop.core' AND "key" = 'default.timezone'), 'Europe/Rome'), 
ot1."start_date", ot1."due_date", ot1."completed_date", ot1."completion_percentage", 
(CASE WHEN ot1."status" = 'completed' THEN 'CO' WHEN ot1."status" = 'inprogress' THEN 'IP' WHEN ot1."status" = 'waiting' THEN 'WA' WHEN ot1."status" = 'deferred' THEN 'CA' ELSE 'NA' END), 
ot1."importance", ot1."is_private", ot1."href", ot1."etag", 
NULL, NULL, 
NULL, NULL, NULL, NULL
FROM "tasks_old"."tasks" AS ot1
INNER JOIN "tasks_old"."categories" AS ot2 ON (ot1."category_id" = ot2."category_id");

-- ----------------------------
-- Fix data copied from OLD tasks
-- ----------------------------
UPDATE "tasks"."tasks" 
SET "completed_on" = "due" 
WHERE "tasks"."status" = 'CO' AND "tasks"."due" IS NOT NULL AND "tasks"."completed_on" IS NULL;

-- ----------------------------
-- Copy data from OLD tasks_attachments
-- ----------------------------
INSERT INTO "tasks"."tasks_attachments" ("task_attachment_id", "task_id", "revision_timestamp", "revision_sequence", "filename", "size", "media_type")
SELECT replace(ot1."task_attachment_id", '-', ''), ot2."task_id_NEW", ot1."revision_timestamp", ot1."revision_sequence", ot1."filename", ot1."size", ot1."media_type"
FROM "tasks_old"."tasks_attachments" AS ot1
INNER JOIN "tasks_old"."tasks" AS ot2 ON (ot1."task_id" = ot2."task_id");

-- ----------------------------
-- Copy data from OLD tasks_attachments_data
-- ----------------------------
INSERT INTO "tasks"."tasks_attachments_data" ("task_attachment_id", "bytes")
SELECT replace("task_attachment_id", '-', ''), "bytes"
FROM "tasks_old"."tasks_attachments_data";

-- ----------------------------
-- Copy data from OLD tasks_custom_values
-- ----------------------------
INSERT INTO "tasks"."tasks_custom_values" ("task_id", "custom_field_id", "string_value", "number_value", "boolean_value", "date_value", "text_value")
SELECT ot2."task_id_NEW", ot1."custom_field_id", ot1."string_value", ot1."number_value", ot1."boolean_value", ot1."date_value", ot1."text_value"
FROM "tasks_old"."tasks_custom_values" AS ot1
INNER JOIN "tasks_old"."tasks" AS ot2 ON (ot1."task_id" = ot2."task_id");

-- ----------------------------
-- Copy data from OLD tasks_tags
-- ----------------------------
INSERT INTO "tasks"."tasks_tags" ("task_id", "tag_id")
SELECT ot2."task_id_NEW", ot1."tag_id"
FROM "tasks_old"."tasks_tags" AS ot1
INNER JOIN "tasks_old"."tasks" AS ot2 ON (ot1."task_id" = ot2."task_id");


@DataSource[default@com.sonicle.webtop.core]

-- ----------------------------
-- Deprecate is_default: move values to user_settings
-- ----------------------------
INSERT INTO "core"."user_settings" ("domain_id", "user_id", "service_id", "key", "value")
(
SELECT DISTINCT ON ("oldcats"."domain_id", "oldcats"."user_id") "oldcats"."domain_id", "oldcats"."user_id", 'com.sonicle.webtop.tasks', 'category.folder.default', "oldcats"."category_id"
FROM "tasks_old"."categories" AS "oldcats"
WHERE "oldcats"."is_default" IS TRUE
)
