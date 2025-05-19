@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Add triggers to keep history tables clean
-- ----------------------------
DROP TRIGGER IF EXISTS "history_categories_trg_prune" ON "tasks"."history_categories";
CREATE TRIGGER "history_categories_trg_prune" AFTER INSERT ON "tasks"."history_categories"
FOR EACH ROW
EXECUTE PROCEDURE "tasks"."prune_categories_history_on"();

DROP TRIGGER IF EXISTS "history_tasks_trg_prune" ON "tasks"."history_contacts";
CREATE TRIGGER "history_tasks_trg_prune" AFTER INSERT ON "tasks"."history_contacts"
FOR EACH ROW
EXECUTE PROCEDURE "tasks"."prune_tasks_history_on"();