/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.service;

import hku.ec.domain.Room;
import hku.ec.domain.RoomRejection;
import hku.ec.domain.RoomVisit;
import hku.ec.repo.RoomRejectionRepository;
import hku.ec.repo.RoomVisitRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Challenge 1 的静态分：七因子，权重合计 1.00，拒绝再打一次长期折扣。
 *
 * 熟悉度只对某一个账号有意义，所以没有登录时按六因子归一到 0.80，和以前一样。
 */
@Service
public class RoomScoreService {

    public static final Map<String, Double> WEIGHTS = new LinkedHashMap<>();
    static {
        WEIGHTS.put("familiar", 0.20);
        WEIGHTS.put("quiet",    0.20);
        WEIGHTS.put("desk",     0.15);
        WEIGHTS.put("sockets",  0.15);
        WEIGHTS.put("aircon",   0.10);
        WEIGHTS.put("space",    0.10);
        WEIGHTS.put("light",    0.10);
    }

    private static final double HALF_LIFE_DAYS = 21.0;
    private static final double DISCOUNT = 0.12;
    private static final int FAMILIARITY_CAP = 10;

    private final RoomVisitRepository visits;
    private final RoomRejectionRepository rejections;

    public RoomScoreService(RoomVisitRepository visits, RoomRejectionRepository rejections) {
        this.visits = visits;
        this.rejections = rejections;
    }

    /** 一间房、一个人：S、S'、以及拒绝的有效次数 */
    public Result score(Room room, String username, Map<String, Double> reviewFactors) {
        Map<String, Double> factors = new LinkedHashMap<>();

        reviewFactors.forEach((k, v) -> { if (v != null) factors.put(k, v); });

        double familiarity = 0.0;
        double nEff = 0.0;
        if (username != null) {
            familiarity = familiarity(username, room.getBuilding());
            nEff = effectiveRejections(username, room.getId());
            factors.put("familiar", familiarity);
        }

        double num = 0.0, den = 0.0;
        for (Map.Entry<String, Double> e : WEIGHTS.entrySet()) {
            Double v = factors.get(e.getKey());
            if (v == null) continue;
            num += e.getValue() * v;
            den += e.getValue();
        }
        Double s = den > 0 ? (num / den) * 10.0 : null;
        Double sPrime = s == null ? null : s * Math.pow(1 - DISCOUNT, nEff);

        Map<String, Double> rounded = new LinkedHashMap<>();
        factors.forEach((k, v) -> rounded.put(k, Math.round(v * 100.0) / 100.0));
        return new Result(round(s), round(sPrime), rounded, round(nEff), round(familiarity));
    }

    /** 这栋楼来过几次，封顶十次 */
    private double familiarity(String username, String building) {
        List<RoomVisit> rows = visits.inBuilding(username, building);
        if (rows.isEmpty()) return 0.0;
        int total = rows.stream().mapToInt(RoomVisit::getVisits).sum();
        return Math.min(total / (double) FAMILIARITY_CAP, 1.0);
    }

    /** n_eff = Σ e^(−天数/21) × 每次的份量 */
    private double effectiveRejections(String username, Long roomId) {
        List<RoomRejection> rows = rejections.findByUsername(username);
        LocalDateTime now = LocalDateTime.now();
        double sum = 0.0;
        for (RoomRejection r : rows) {
            if (!roomId.equals(r.getRoomId())) continue;
            double days = Duration.between(r.getCreatedAt(), now).toHours() / 24.0;
            sum += Math.exp(-days / HALF_LIFE_DAYS) * r.getKind().doubleValue();
        }
        return sum;
    }

    private static Double round(Double v) {
        return v == null ? null : Math.round(v * 100.0) / 100.0;
    }

    public record Result(Double score, Double sPrime, Map<String, Double> factors,
                         double nEff, double familiarity) { }
}
