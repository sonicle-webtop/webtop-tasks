@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Fix data for tasks
-- ----------------------------
UPDATE "tasks"."tasks" SET "creation_timestamp" = "revision_timestamp" WHERE "creation_timestamp" IS NULL;

-- ----------------------------
-- Update structure for tasks
-- ----------------------------
ALTER TABLE "tasks"."tasks"
ALTER COLUMN "creation_timestamp" SET DEFAULT now(), 
ALTER COLUMN "creation_timestamp" SET NOT NULL;
