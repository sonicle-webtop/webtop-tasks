@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Fill history_categories retrieving data from last-saved status
-- ----------------------------
INSERT INTO "tasks"."history_categories" ("domain_id", "user_id", "category_id", "change_type")
SELECT "categories"."domain_id", "categories"."user_id", "categories"."category_id", 'C'
FROM "tasks"."categories";

-- ----------------------------
-- Fill history_tasks retrieving data from last-saved status
-- ----------------------------
INSERT INTO "tasks"."history_tasks" ("category_id", "task_id", "change_timestamp", "change_type")
SELECT "tasks"."category_id", "tasks"."task_id", "tasks"."creation_timestamp", 'C'
FROM "tasks"."tasks"
INNER JOIN "tasks"."categories" ON "tasks"."category_id" = "categories"."category_id"
WHERE "tasks"."revision_status" IN ('N','M')
UNION
SELECT "tasks"."category_id", "tasks"."task_id", "tasks"."revision_timestamp", 'U'
FROM "tasks"."tasks"
INNER JOIN "tasks"."categories" ON "tasks"."category_id" = "categories"."category_id"
WHERE "tasks"."revision_status" IN ('M');

INSERT INTO "tasks"."history_tasks" ("category_id", "task_id", "change_timestamp", "change_type")
SELECT "tasks"."category_id", "tasks"."task_id", "tasks"."creation_timestamp", 'D'
FROM "tasks"."tasks"
INNER JOIN "tasks"."categories" ON "tasks"."category_id" = "categories"."category_id"
WHERE "tasks"."revision_status" IN ('D');

-- ----------------------------
-- Add categories triggers
-- ----------------------------
DROP TRIGGER IF EXISTS "trg_categories_onafter_1" ON "tasks"."categories";
CREATE TRIGGER "trg_categories_onafter_1" AFTER INSERT OR UPDATE ON "tasks"."categories"
FOR EACH ROW
EXECUTE PROCEDURE "tasks"."update_categories_history"();

DROP TRIGGER IF EXISTS "trg_categories_onafter_2" ON "tasks"."categories";
CREATE TRIGGER "trg_categories_onafter_2" AFTER DELETE ON "tasks"."categories"
FOR EACH ROW
EXECUTE PROCEDURE "tasks"."update_categories_history"();

-- ----------------------------
-- Add tasks triggers
-- ----------------------------
DROP TRIGGER IF EXISTS "trg_tasks_onafter_1" ON "tasks"."tasks";
CREATE TRIGGER "trg_tasks_onafter_1" AFTER INSERT OR DELETE ON "tasks"."tasks"
FOR EACH ROW
EXECUTE PROCEDURE "tasks"."update_tasks_history"();

DROP TRIGGER IF EXISTS "trg_tasks_onafter_2" ON "tasks"."tasks";
CREATE TRIGGER "trg_tasks_onafter_2" AFTER UPDATE OF "revision_timestamp" ON "tasks"."tasks"
FOR EACH ROW
EXECUTE PROCEDURE "tasks"."update_tasks_history"();
