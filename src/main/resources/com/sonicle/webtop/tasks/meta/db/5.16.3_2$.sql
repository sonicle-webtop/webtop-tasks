@DataSource[default@com.sonicle.webtop.tasks]

CREATE OR REPLACE FUNCTION "tasks"."prune_tasks_history_on"()
  RETURNS "pg_catalog"."trigger" AS $BODY$BEGIN
	IF TG_OP = 'INSERT' THEN
		DELETE FROM "tasks"."history_tasks" WHERE "task_id" = NEW."task_id" AND "change_timestamp" < NEW."change_timestamp" AND "change_type" = NEW."change_type" AND "category_id" = NEW."category_id";
		RETURN NEW;
	END IF;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100