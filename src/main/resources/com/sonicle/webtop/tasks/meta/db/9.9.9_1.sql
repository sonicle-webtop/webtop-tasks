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
