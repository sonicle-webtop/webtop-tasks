@DataSource[default@com.sonicle.webtop.tasks]

CREATE OR REPLACE FUNCTION "tasks"."update_categories_history"()
  RETURNS TRIGGER AS $BODY$BEGIN
	IF TG_OP = 'DELETE' THEN
		INSERT INTO "tasks"."history_categories" ("domain_id", "user_id", "category_id", "change_timestamp", "change_type")
		VALUES (OLD."domain_id", OLD."user_id", OLD."category_id", NOW(), 'D');
		RETURN OLD;
	ELSIF TG_OP = 'INSERT' THEN
		INSERT INTO "tasks"."history_categories" ("domain_id", "user_id", "category_id", "change_timestamp", "change_type")
		VALUES (NEW."domain_id", NEW."user_id", NEW."category_id", NEW."revision_timestamp", 'C');
		RETURN NEW;
	ELSIF TG_OP = 'UPDATE' THEN
		INSERT INTO "tasks"."history_categories" ("domain_id", "user_id", "category_id", "change_timestamp", "change_type")
		VALUES (NEW."domain_id", NEW."user_id", NEW."category_id", NEW."revision_timestamp", 'U');
		RETURN NEW;
	END IF;
END$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;