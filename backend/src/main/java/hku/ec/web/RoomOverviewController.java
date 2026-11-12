/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.web;

import hku.ec.domain.Room;
import hku.ec.repo.RoomRepository;
import hku.ec.repo.RoomReviewRepository;
import hku.ec.service.AvailabilityService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * 房间总览：一次给全部教室，带评价综合分。
 * 时间是可选的：给了 from 与 to 就附上这段时间空不空，不给就只给设施与评价。
 */
@RestController
@RequestMapping("/api")
public class RoomOverviewController {

    /** 六个可评价因子的权重，来自评分文档，去掉个人的熟悉度后按比例归一 */
    private static final Map<String, Double> W = new LinkedHashMap<>();
    static {
        W.put("quiet", 0.20);
        W.put("desk", 0.15);
        W.put("sockets", 0.15);
        W.put("aircon", 0.10);
        W.put("space", 0.10);
        W.put("light", 0.10);
    }

    private final RoomRepository rooms;
    private final RoomReviewRepository reviews;
    private final AvailabilityService availability;

    public RoomOverviewController(RoomRepository rooms, RoomReviewRepository reviews,
                                  AvailabilityService availability) {
        this.rooms = rooms;
        this.reviews = reviews;
        this.availability = availability;
    }

    @GetMapping("/rooms/overview")
    public List<Dtos.RoomOverview> overview(@RequestParam(required = false) String date,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to) {
        LocalDate d = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);

        // 时间段可选
        Map<String, Dtos.RoomAvailability> window = Map.of();
        if (from != null && !from.isBlank() && to != null && !to.isBlank()) {
            AvailabilityService.requireSearchWindow(LocalTime.parse(from), LocalTime.parse(to));
            Dtos.AvailabilityResponse resp = availability.availability(d, null, LocalTime.parse(from),
                    (int) java.time.Duration.between(LocalTime.parse(from), LocalTime.parse(to)).toMinutes());
            Map<String, Dtos.RoomAvailability> tmp = new HashMap<>();
            for (Dtos.RoomAvailability rs : resp.rooms()) {
                tmp.put(rs.room().code(), rs);
            }
            window = tmp;
        }

        Map<Long, double[]> agg = new HashMap<>();   // roomId -> [count, quiet, desk, sockets, aircon, space, light]
        for (Object[] row : reviews.factorAverages()) {
            double[] v = new double[7];
            v[0] = ((Number) row[1]).doubleValue();
            for (int i = 0; i < 6; i++) {
                Object o = row[i + 2];
                v[i + 1] = o == null ? Double.NaN : ((Number) o).doubleValue();
            }
            agg.put((Long) row[0], v);
        }

        List<Dtos.RoomOverview> out = new ArrayList<>();
        for (Room r : rooms.findAll()) {
            double[] a = agg.get(r.getId());
            Map<String, Double> factors = new LinkedHashMap<>();
            Double score = null;
            int count = 0;
            if (a != null) {
                count = (int) a[0];
                double num = 0, den = 0;
                for (Map.Entry<String, Double> e : W.entrySet()) {
                    int idx = new ArrayList<>(W.keySet()).indexOf(e.getKey()) + 1;
                    double val = a[idx];
                    if (Double.isNaN(val)) continue;
                    factors.put(e.getKey(), Math.round(val * 10.0) / 10.0);
                    num += e.getValue() * val;
                    den += e.getValue();
                }
                if (den > 0) score = Math.round((num / den) * 10.0) / 10.0;
            }

            Dtos.RoomAvailability st = window.get(r.getCode());
            Boolean free = st == null ? null : st.matches();
            String nextBusy = st == null ? null : st.nextBusyStart();

            out.add(new Dtos.RoomOverview(r.getCode(), r.getBuilding(), r.getFloor(), r.getCapacity(),
                    r.getSockets(), free, nextBusy, score, count, factors));
        }
        out.sort(Comparator.comparing(Dtos.RoomOverview::code));
        return out;
    }
}
