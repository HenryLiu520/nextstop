-- 报告 5.4：六个因子分与总分同尺度（1–10）。之前只有总分有约束，六个列没有。
ALTER TABLE room_reviews
    ADD CONSTRAINT factor_quiet_range   CHECK (quiet   IS NULL OR quiet   BETWEEN 1 AND 10),
    ADD CONSTRAINT factor_desk_range    CHECK (desk    IS NULL OR desk    BETWEEN 1 AND 10),
    ADD CONSTRAINT factor_sockets_range CHECK (sockets IS NULL OR sockets BETWEEN 1 AND 10),
    ADD CONSTRAINT factor_aircon_range  CHECK (aircon  IS NULL OR aircon  BETWEEN 1 AND 10),
    ADD CONSTRAINT factor_space_range   CHECK (space   IS NULL OR space   BETWEEN 1 AND 10),
    ADD CONSTRAINT factor_light_range   CHECK (light   IS NULL OR light   BETWEEN 1 AND 10);
