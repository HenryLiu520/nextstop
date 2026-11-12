-- 报告 5.3 说每块落在三种状态之一：school occupied / admin occupied / free。
-- 状态是算出来的，加一列就得靠触发器维护，迟早和课表不一致，所以做成视图。
CREATE OR REPLACE VIEW room_block_state AS
SELECT r.id                AS room_id,
       d.day               AS block_date,
       h.hour              AS hour,
       CASE
           WHEN EXISTS (SELECT 1 FROM class_slots c
                        WHERE c.room_id = r.id
                          AND c.day_of_week = EXTRACT(ISODOW FROM d.day)::int
                          AND c.start_time <= make_time(h.hour, 0, 0)
                          AND c.end_time   >  make_time(h.hour, 0, 0)) THEN 'school'
           WHEN EXISTS (SELECT 1 FROM room_updates u
                        WHERE u.room_id = r.id
                          AND u.slot_date = d.day
                          AND u.active
                          AND u.start_time <= make_time(h.hour, 0, 0)
                          AND u.end_time   >  make_time(h.hour, 0, 0)) THEN 'admin'
           ELSE 'free'
       END AS state
FROM rooms r
CROSS JOIN (SELECT generate_series(current_date, current_date + 30, interval '1 day')::date AS day) d
CROSS JOIN (SELECT generate_series(8, 21) AS hour) h;
