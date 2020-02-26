@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- New table: tasks_tags
-- ----------------------------
CREATE TABLE "tasks"."tasks_tags" (
"task_id" int4 NOT NULL,
"tag_id" varchar(22) NOT NULL
)
WITH (OIDS=FALSE)

;

CREATE INDEX "tasks_tags_ak1" ON "tasks"."tasks_tags" USING btree ("tag_id");
ALTER TABLE "tasks"."tasks_tags" ADD PRIMARY KEY ("task_id", "tag_id");
ALTER TABLE "tasks"."tasks_tags" ADD FOREIGN KEY ("tag_id") REFERENCES "core"."tags" ("tag_id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- New table: tasks_custom_values
-- ----------------------------
CREATE TABLE "tasks"."tasks_custom_values" (
"task_id" int4 NOT NULL,
"custom_field_id" varchar(22) NOT NULL,
"string_value" varchar(255),
"number_value" float8,
"boolean_value" bool,
"date_value" timestamptz(6),
"text_value" text
)
WITH (OIDS=FALSE)

;

ALTER TABLE "tasks"."tasks_custom_values" ADD PRIMARY KEY ("task_id", "field_id");
ALTER TABLE "tasks"."tasks_custom_values" ADD FOREIGN KEY ("task_id") REFERENCES "tasks"."tasks" ("task_id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "tasks"."tasks_custom_values" ADD FOREIGN KEY ("custom_field_id") REFERENCES "core"."custom_fields" ("custom_field_id") ON DELETE CASCADE ON UPDATE CASCADE;
