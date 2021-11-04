@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Fix priority values
-- ----------------------------
UPDATE "tasks"."tasks" SET "importance" = 9 WHERE "importance" = 0;
UPDATE "tasks"."tasks" SET "importance" = 5 WHERE "importance" = 1;
UPDATE "tasks"."tasks" SET "importance" = 1 WHERE "importance" = 2;
