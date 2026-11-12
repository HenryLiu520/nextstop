/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.repo;

import hku.ec.domain.RoomVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomVisitRepository extends JpaRepository<RoomVisit, Long> {
    List<RoomVisit> findByUsername(String username);

    /** 这个人在这一栋楼的到访：熟悉度按楼算，不按房间算 */
    @Query("select v from RoomVisit v, Room r " +
           "where v.roomId = r.id and v.username = ?1 and r.building = ?2")
    List<RoomVisit> inBuilding(String username, String building);
}
