-- ============================================================================
--  将 word 表删减到 3000 个单词（默认：按 id 升序保留最早的 3000 个）
--  目标库：Render english-learning-db  (english_learning_db_mcen, PostgreSQL)
--
--  执行前请先看「第 1 步」的输出，确认数量符合预期，再继续往下执行。
--  整个文件可以在 psql / DBeaver / Navicat / 任意 query 窗口里整体粘贴执行。
--
--  若想改成别的保留规则，替换第 3 步里的 SELECT 即可（示例见文件末尾）。
-- ============================================================================

-- 1) 预检：总量 + 将删除的数量 ------------------------------------------------
SELECT COUNT(*) AS total_words FROM word;
SELECT COUNT(*) AS will_delete
FROM (SELECT id FROM word ORDER BY id ASC OFFSET 3000) t;
-- 若 will_delete = 0，说明本来不足 3000，无需删除。

-- 2) 备份将被删除的单词（万一删错可回滚）-------------------------------------
DROP TABLE IF EXISTS word_backup_prune;
CREATE TABLE word_backup_prune AS
SELECT * FROM word
WHERE id NOT IN (SELECT id FROM word ORDER BY id ASC LIMIT 3000);
SELECT COUNT(*) AS backed_up_rows FROM word_backup_prune;

-- 3) 选出要删除的 word.id（保留 id 最小的 3000 个）----------------------------
CREATE TEMP TABLE _del_word_ids AS
SELECT id FROM word ORDER BY id ASC OFFSET 3000;

-- 4) 先清理 5 张关联表里对这些单词的引用（避免孤儿数据）----------------------
DELETE FROM example_sentence WHERE word_id IN (SELECT id FROM _del_word_ids);
DELETE FROM favorite         WHERE word_id IN (SELECT id FROM _del_word_ids);
DELETE FROM learning_record  WHERE word_id IN (SELECT id FROM _del_word_ids);
DELETE FROM wrong_word       WHERE word_id IN (SELECT id FROM _del_word_ids);
DELETE FROM word_book_word   WHERE word_id IN (SELECT id FROM _del_word_ids);

-- 5) 删除多余单词 ------------------------------------------------------------
DELETE FROM word WHERE id IN (SELECT id FROM _del_word_ids);

-- 6) 校验结果 ----------------------------------------------------------------
SELECT
  (SELECT COUNT(*) FROM word)               AS words_left,
  (SELECT COUNT(*) FROM word_backup_prune)  AS backed_up_rows;

-- 7) 回滚 / 清理 -------------------------------------------------------------
-- 万一删多了，恢复被删单词：
--   INSERT INTO word SELECT * FROM word_backup_prune;
-- 确认无误后，丢弃备份表以释放空间：
--   DROP TABLE word_backup_prune;

-- ============================================================================
--  其他保留规则示例（替换第 3 步的 SELECT）
--  · 按难度优先保留 EASY/MEDIUM（删 HARD）：
--      SELECT id FROM word
--      WHERE difficulty_level = 'HARD'
--      ORDER BY id ASC OFFSET 1000;          -- 例：HARD 只留 1000 个
--  · 随机保留 3000（每次结果不同）：
--      SELECT id FROM word ORDER BY random() OFFSET 3000;
-- ============================================================================
