-- 熟悉度与拒绝：Challenge 1 的七因子里，这两项以前只写在文档上
CREATE TABLE room_visits (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(64) NOT NULL,
    room_id     BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    visits      INTEGER NOT NULL DEFAULT 0 CHECK (visits >= 0),
    last_at     TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (username, room_id)
);

CREATE TABLE room_rejections (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(64) NOT NULL,
    room_id     BIGINT NOT NULL REFERENCES rooms(id) ON DELETE CASCADE,
    kind        NUMERIC(3,1) NOT NULL DEFAULT 1.0 CHECK (kind > 0 AND kind <= 1.0),
    created_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_visit_user ON room_visits (username);
CREATE INDEX idx_reject_user_room ON room_rejections (username, room_id, created_at DESC);
