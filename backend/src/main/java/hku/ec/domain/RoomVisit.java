/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 一个学生进过这间房几次。熟悉度= min(该楼到访次数 / 10, 1)。 */
@Entity
@Table(name = "room_visits")
public class RoomVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private int visits;

    @Column(name = "last_at", nullable = false)
    private LocalDateTime lastAt = LocalDateTime.now();

    protected RoomVisit() { }

    public RoomVisit(String username, Long roomId, int visits) {
        this.username = username;
        this.roomId = roomId;
        this.visits = visits;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public Long getRoomId() { return roomId; }
    public int getVisits() { return visits; }
    public LocalDateTime getLastAt() { return lastAt; }
    public void setVisits(int v) { this.visits = v; this.lastAt = LocalDateTime.now(); }
}
