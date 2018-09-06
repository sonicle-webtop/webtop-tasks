@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Table structure for tasks_attachments
-- ----------------------------
DROP TABLE IF EXISTS "tasks"."tasks_attachments";
CREATE TABLE "tasks"."tasks_attachments" (
"task_attachment_id" varchar(32) NOT NULL,
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
-- Table structure for tasks_attachments_data
-- ----------------------------
CREATE TABLE "tasks"."tasks_attachments_data" (
"task_attachment_id" varchar(32) NOT NULL,
"bytes" bytea NOT NULL
)
WITH (OIDS=FALSE)

;

-- ----------------------------
-- Primary Key structure for table tasks_attachments_data
-- ----------------------------
ALTER TABLE "tasks"."tasks_attachments_data" ADD PRIMARY KEY ("task_attachment_id");

-- ----------------------------
-- Foreign Key structure for table "tasks"."tasks_attachments_data"
-- ----------------------------
ALTER TABLE "tasks"."tasks_attachments_data" ADD FOREIGN KEY ("task_attachment_id") REFERENCES "tasks"."tasks_attachments" ("task_attachment_id") ON DELETE CASCADE ON UPDATE CASCADE;
