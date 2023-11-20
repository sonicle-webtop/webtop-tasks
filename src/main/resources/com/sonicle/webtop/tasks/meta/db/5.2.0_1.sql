@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- New table: tasks_attachments
-- ----------------------------
CREATE TABLE "tasks"."tasks_attachments" (
"task_attachment_id" varchar(36) NOT NULL,
"task_id" int4 NOT NULL,
"revision_timestamp" timestamptz(6) NOT NULL,
"revision_sequence" int2 NOT NULL,
"filename" varchar(255) NOT NULL,
"size" int8 NOT NULL,
"media_type" varchar(255) NOT NULL
);

CREATE INDEX "tasks_attachments_ak1" ON "tasks"."tasks_attachments" USING btree ("task_id");
ALTER TABLE "tasks"."tasks_attachments" ADD PRIMARY KEY ("task_attachment_id");
ALTER TABLE "tasks"."tasks_attachments" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- New table: tasks_attachments_data
-- ----------------------------
CREATE TABLE "tasks"."tasks_attachments_data" (
"task_attachment_id" varchar(36) NOT NULL,
"bytes" bytea NOT NULL
);

ALTER TABLE "tasks"."tasks_attachments_data" ADD PRIMARY KEY ("task_attachment_id");
ALTER TABLE "tasks"."tasks_attachments_data" ADD FOREIGN KEY ("task_attachment_id") REFERENCES "tasks"."tasks_attachments" ("task_attachment_id") ON DELETE CASCADE ON UPDATE CASCADE;
