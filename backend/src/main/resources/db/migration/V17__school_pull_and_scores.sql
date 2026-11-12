-- V17: 学校系统的拉取落进自己的表；房间携带评分维度
-- 每次从学校系统拉取的课表原始数据落在 school_slots（学校侧房号、星期、起止、课程号），
-- pulled_at 记录拉取批次；class_slots 作为服务读取的工作课表，由拉取刷新。

CREATE TABLE school_slots (
  id          BIGSERIAL PRIMARY KEY,
  room_code   VARCHAR(32)  NOT NULL,          -- 学校系统自己的房号（如 CPD-LG.01）
  day_of_week INT          NOT NULL,          -- 1=Mon ... 7=Sun
  start_time  TIME         NOT NULL,
  end_time    TIME         NOT NULL,
  course_code VARCHAR(32)  NOT NULL,
  pulled_at   TIMESTAMP    NOT NULL DEFAULT now(),
  CONSTRAINT school_slot_order CHECK (end_time > start_time)
);
CREATE INDEX idx_school_room_day ON school_slots (room_code, day_of_week);

-- 评分维度：1-10 分制（安静、桌椅舒适度……），供客户端评分使用；NULL = 未核实，按中性处理
ALTER TABLE rooms ADD COLUMN quiet_score   INT CHECK (quiet_score   BETWEEN 1 AND 10);
ALTER TABLE rooms ADD COLUMN comfort_score INT CHECK (comfort_score BETWEEN 1 AND 10);
