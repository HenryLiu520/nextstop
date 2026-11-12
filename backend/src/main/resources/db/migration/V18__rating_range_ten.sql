-- 评价改成 10 分制：总分 1 到 10，分项也是 1 到 10
ALTER TABLE room_reviews DROP CONSTRAINT IF EXISTS rating_range;
ALTER TABLE room_reviews ADD CONSTRAINT rating_range CHECK (rating >= 1 AND rating <= 10);
