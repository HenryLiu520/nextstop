/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** 学生对房间的评价：星级 + 文字。一个人对一间房只有一条，可改可删。 */
@Entity
@Table(name = "room_reviews")
public class RoomReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Integer rating;

    @Column(nullable = false)
    private String body;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** 分项打分，JSON 文本，键是因子名，值是 1 到 10；没有就是 null */
    @Column(name = "dimensions")
    private String dimensions;

    /* 六个因子拆成列，便于按房间求平均 */
    private Integer quiet;
    private Integer desk;
    private Integer sockets;
    private Integer aircon;
    private Integer space;
    private Integer light;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getQuiet() { return quiet; }
    public void setQuiet(Integer quiet) { this.quiet = quiet; }
    public Integer getDesk() { return desk; }
    public void setDesk(Integer desk) { this.desk = desk; }
    public Integer getSockets() { return sockets; }
    public void setSockets(Integer sockets) { this.sockets = sockets; }
    public Integer getAircon() { return aircon; }
    public void setAircon(Integer aircon) { this.aircon = aircon; }
    public Integer getSpace() { return space; }
    public void setSpace(Integer space) { this.space = space; }
    public Integer getLight() { return light; }
    public void setLight(Integer light) { this.light = light; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
