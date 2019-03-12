@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Remove obsolete setting keys
-- ----------------------------
@DataSource[default@com.sonicle.webtop.core]
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.tasks') AND ("user_settings"."key" = 'category.roots.checked');
DELETE FROM "core"."user_settings" WHERE ("user_settings"."service_id" = 'com.sonicle.webtop.tasks') AND ("user_settings"."key" = 'category.folders.checked');
