/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.web;

import hku.ec.domain.Room;
import hku.ec.repo.RoomRepository;
import hku.ec.repo.RoomReviewRepository;
import hku.ec.service.AuthService;
import hku.ec.service.AvailabilityService;
import hku.ec.service.RoomScoreService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * 房间总览：一次给全部教室，带评分。
 * 时间是可选的：给了 from 与 to 就附上这段时间空不空，不给就只给设施与评价。
 * 带上 token 时按七因子算（含这个人的熟悉度与拒绝折扣），不带就按六因子归一。
 */
@RestController
@RequestMapping("/api")
public class RoomOverviewController {

    /** 评价里的六个因子，顺序与 RoomReviewRepository.factorAverages() 的列一致 */
    private static final List<String> REVIEW_FACTORS =
            List.of("quiet", "desk", "sockets", "aircon", "space", "light");

    private final RoomRepository rooms;
    private final RoomReviewRepository reviews;
    private final AvailabilityService availability;
    private final RoomScoreService scoring;
    private final AuthService auth;

    public RoomOverviewController(RoomRepository rooms, RoomReviewRepository reviews,
                                  AvailabilityService availability, RoomScoreService scoring,
                                  AuthService auth) {
        this.rooms = rooms;
        this.reviews = reviews;
        this.availability = availability;
        this.scoring = scoring;
        this.auth = auth;
    }

    @GetMapping("/rooms/overview")
    public List<Dtos.RoomOverview> overview(@RequestParam(required = false) String date,
                                            @RequestParam(required = false) String from,
                                            @RequestParam(required = false) String to,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        LocalDate d = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);

        String viewer = auth.resolve(Tokens.from(authHeader)).map(AuthService.Session::username).orElse(null);

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
            Map<String, Double> reviewFactors = new LinkedHashMap<>();
            int count = 0;

            if (a != null) {
                count = (int) a[0];
                for (int i = 0; i < REVIEW_FACTORS.size(); i++) {
                    double val = a[i + 1];
                    if (Double.isNaN(val)) continue;
                    reviewFactors.put(REVIEW_FACTORS.get(i), val / 10.0);
                }
            }
            /* 没有评价时退回房间自带的两项：安静与座位舒适，各 1 到 10 */
            reviewFactors.putIfAbsent("quiet",
                    r.getQuietScore() == null ? null : r.getQuietScore() / 10.0);
            reviewFactors.putIfAbsent("desk",
                    r.getComfortScore() == null ? null : r.getComfortScore() / 10.0);
            reviewFactors.values().removeIf(Objects::isNull);

            RoomScoreService.Result s = scoring.score(r, viewer, reviewFactors);

            Dtos.RoomAvailability st = window.get(r.getCode());
            Boolean free = st == null ? null : st.matches();
            String nextBusy = st == null ? null : st.nextBusyStart();

            out.add(new Dtos.RoomOverview(r.getCode(), r.getBuilding(), r.getFloor(), r.getCapacity(),
                    r.getSockets(), free, nextBusy, s.score(), s.sPrime(), s.nEff(), s.familiarity(),
                    count, s.factors()));
        }
        out.sort(Comparator.comparing(Dtos.RoomOverview::code));
        return out;
    }
}
