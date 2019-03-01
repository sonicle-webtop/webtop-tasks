@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Update structure for tasks
-- ----------------------------
ALTER TABLE "tasks"."tasks"
ADD COLUMN "href" varchar(2048),
ADD COLUMN "etag" varchar(2048),
ADD COLUMN "creation_timestamp" timestamptz DEFAULT now();

CREATE INDEX "tasks_ak2" ON "tasks"."tasks" USING btree ("category_id", "revision_status", "href");

-- ----------------------------
-- Fix data for tasks
-- ----------------------------
UPDATE "tasks"."tasks" SET "creation_timestamp" = "revision_timestamp";

UPDATE tasks.tasks AS ttsks
SET
public_uid = md5(ttsks.public_uid || '.' || ttsks.task_id) || '@' || cdoms.internet_name
FROM tasks.categories AS tcats, core.domains AS cdoms
WHERE (ttsks.category_id = tcats.category_id)
AND (tcats.domain_id = cdoms.domain_id)
AND (ttsks.href IS NULL);

UPDATE tasks.tasks AS ttsks
SET
href = ttsks.public_uid || '.ics'
FROM tasks.categories AS tcats
WHERE (ttsks.category_id = tcats.category_id)
AND (ttsks.href IS NULL);

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
-- Primary Key structure for table tasks_icalendars
-- ----------------------------
ALTER TABLE "tasks"."tasks_icalendars" ADD PRIMARY KEY ("task_id");
