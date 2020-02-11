@DataSource[default@com.sonicle.webtop.tasks]

-- ----------------------------
-- Fix data for tasks
-- ----------------------------
UPDATE tasks.tasks AS ttsks
SET
href = ttsks.public_uid || '.ics'
FROM tasks.categories AS tcats
WHERE (ttsks.category_id = tcats.category_id)
AND (ttsks.href IS NULL);
