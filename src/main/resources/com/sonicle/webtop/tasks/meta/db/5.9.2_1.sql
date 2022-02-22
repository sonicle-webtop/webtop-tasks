@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Add new reminder column
-- ----------------------------
ALTER TABLE "tasks"."categories" ADD COLUMN "reminder" int4;
