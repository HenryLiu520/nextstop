/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 一次拒绝。主动拒 1.0，换走 0.5，到了又走 0.3。21 天衰到 37%。 */
@Entity
@Table(name = "room_rejections")
public class RoomRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(nullable = false, precision = 3, scale = 1)
    private BigDecimal kind = BigDecimal.ONE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected RoomRejection() { }

    public RoomRejection(String username, Long roomId, BigDecimal kind) {
        this.username = username;
        this.roomId = roomId;
        this.kind = kind;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public Long getRoomId() { return roomId; }
    public BigDecimal getKind() { return kind; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
