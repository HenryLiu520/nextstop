-- 评价的六个因子拆成独立列，便于按房间求平均、算综合分
ALTER TABLE room_reviews ADD COLUMN IF NOT EXISTS quiet SMALLINT;
ALTER TABLE room_reviews ADD COLUMN IF NOT EXISTS desk SMALLINT;
ALTER TABLE room_reviews ADD COLUMN IF NOT EXISTS sockets SMALLINT;
ALTER TABLE room_reviews ADD COLUMN IF NOT EXISTS aircon SMALLINT;
ALTER TABLE room_reviews ADD COLUMN IF NOT EXISTS space SMALLINT;
ALTER TABLE room_reviews ADD COLUMN IF NOT EXISTS light SMALLINT;

-- 已有数据从 dimensions 文本回填一次（格式 quiet=9,desk=8,...）
UPDATE room_reviews SET
  quiet   = NULLIF(split_part(split_part(dimensions, 'quiet=',   2), ',', 1), '')::smallint,
  desk    = NULLIF(split_part(split_part(dimensions, 'desk=',    2), ',', 1), '')::smallint,
  sockets = NULLIF(split_part(split_part(dimensions, 'sockets=', 2), ',', 1), '')::smallint,
  aircon  = NULLIF(split_part(split_part(dimensions, 'aircon=',  2), ',', 1), '')::smallint,
  space   = NULLIF(split_part(split_part(dimensions, 'space=',   2), ',', 1), '')::smallint,
  light   = NULLIF(split_part(split_part(dimensions, 'light=',   2), ',', 1), '')::smallint
WHERE dimensions IS NOT NULL;
