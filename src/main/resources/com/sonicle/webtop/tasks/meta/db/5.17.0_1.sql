@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Migrate legacy color to the lightest one from default palette (tailwind)
-- ----------------------------
UPDATE "contacts"."categories" SET "color" = '#F3F4F6' WHERE "color" = '#FFFFFF';
UPDATE "contacts"."category_props" SET "color" = '#F3F4F6' WHERE "color" = '#FFFFFF';
