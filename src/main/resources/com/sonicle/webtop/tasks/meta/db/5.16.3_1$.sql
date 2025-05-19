@DataSource[default@com.sonicle.webtop.tasks]

CREATE OR REPLACE FUNCTION "tasks"."prune_categories_history_on"()
  RETURNS "pg_catalog"."trigger" AS $BODY$BEGIN
	IF TG_OP = 'INSERT' THEN
		DELETE FROM "tasks"."history_categories" WHERE "category_id" = NEW."category_id" AND "change_timestamp" < NEW."change_timestamp" AND "change_type" = NEW."change_type";
		RETURN NEW;
	END IF;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100