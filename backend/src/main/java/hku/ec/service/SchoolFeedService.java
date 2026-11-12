/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import hku.ec.domain.ClassSlot;
import hku.ec.domain.SchoolSlot;
import hku.ec.repo.ClassSlotRepository;
import hku.ec.repo.RoomRepository;
import hku.ec.repo.SchoolSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从学校接口把课表拉进来。
 *
 * 每次拉取先全部落进 school_slots（原始样子），再由它刷新工作课表 class_slots。
 * 拉不到就什么都不动，日志留一行，绝不清空已有数据。
 */
@Service
public class SchoolFeedService {

    private static final Logger log = LoggerFactory.getLogger(SchoolFeedService.class);

    private final SchoolSlotRepository schoolSlots;
    private final ClassSlotRepository classSlots;
    private final RoomRepository rooms;
    private final ObjectMapper json = new ObjectMapper();

    @Value("${app.school-feed.enabled:true}")
    private boolean enabled;

    @Value("${app.school-feed.base-url:http://127.0.0.1:9000}")
    private String baseUrl;

    @Value("${app.school-feed.api-key:hku-demo-key}")
    private String apiKey;

    @Value("${app.school-feed.days:7}")
    private int days;

    public SchoolFeedService(SchoolSlotRepository schoolSlots, ClassSlotRepository classSlots, RoomRepository rooms) {
        this.schoolSlots = schoolSlots;
        this.classSlots = classSlots;
        this.rooms = rooms;
    }

    /** 启动 30 秒后拉一次，之后每 30 分钟一次 */
    @Scheduled(initialDelayString = "${app.school-feed.initial-ms:30000}",
               fixedDelayString = "${app.school-feed.every-ms:1800000}")
    public void pullOnSchedule() {
        if (!enabled) return;
        try {
            int n = pull(LocalDate.now(), days);
            log.info("school feed: {} slot(s) pulled, working timetable refreshed", n);
        } catch (Exception e) {
            log.warn("school feed unavailable, keeping the timetable as it stands: {}", e.toString());
        }
    }

    /** 拉一段时间的课表；返回落库的条数 */
    @Transactional
    public int pull(LocalDate from, int dayCount) throws Exception {
        List<SchoolSlot> pulled = new ArrayList<>();
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

        for (int i = 0; i < Math.max(1, dayCount); i++) {
            LocalDate d = from.plusDays(i);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v1/timetable?date=" + d))
                    .timeout(Duration.ofSeconds(5))
                    .header("X-Api-Key", apiKey)
                    .GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                throw new IllegalStateException("feed answered " + res.statusCode() + " for " + d);
            }
            JsonNode root = json.readTree(res.body());
            JsonNode arr = root.isArray() ? root : root.path("slots");
            if (!arr.isArray()) continue;
            for (JsonNode s : arr) {
                String code = s.path("roomCode").asText(null);
                String start = s.path("start").asText(null);
                String end = s.path("end").asText(null);
                if (code == null || start == null || end == null) continue;
                pulled.add(new SchoolSlot(code, s.path("dayOfWeek").asInt(d.getDayOfWeek().getValue()),
                        LocalTime.parse(start), LocalTime.parse(end), s.path("courseCode").asText("")));
            }
        }

        if (pulled.isEmpty()) return 0;   /* 拉到了但没内容：当作没拉到，不动现有课表 */

        schoolSlots.deleteAllInBatch();
        schoolSlots.saveAll(pulled);
        refreshWorkingTimetable();
        return pulled.size();
    }

    /** class_slots 从 school_slots 重建，认不出的房号跳过 */
    private void refreshWorkingTimetable() {
        Map<String, Long> byCode = new HashMap<>();
        rooms.findAll().forEach(r -> byCode.put(r.getCode(), r.getId()));

        /* 工作课表是「每周重复」的：同一天同一门课拉七天只算一行 */
        java.util.Set<String> seen = new java.util.HashSet<>();
        List<ClassSlot> fresh = new ArrayList<>();
        for (SchoolSlot s : schoolSlots.findAll()) {
            Long roomId = byCode.get(s.getRoomCode());
            if (roomId == null) continue;
            String key = roomId + "|" + s.getDayOfWeek() + "|" + s.getStartTime() + "|"
                    + s.getEndTime() + "|" + s.getCourseCode();
            if (!seen.add(key)) continue;
            ClassSlot c = new ClassSlot();
            c.setRoomId(roomId);
            c.setDayOfWeek(s.getDayOfWeek());
            c.setStartTime(s.getStartTime());
            c.setEndTime(s.getEndTime());
            c.setCourseCode(s.getCourseCode());
            fresh.add(c);
        }
        if (fresh.isEmpty()) return;

        classSlots.deleteAllInBatch();
        classSlots.saveAll(fresh);
    }
}
