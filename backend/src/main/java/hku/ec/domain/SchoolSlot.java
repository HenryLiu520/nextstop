/*
 * Empty Classroom Plan — HKU COMP1110 Group 08
 * Author: LIU Haoran (u3686264) · 2026
 * Signature: EC-COMP1110-G08-u3686264-2026
 */
package hku.ec.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/** 学校接口拉回来的原始一行，按拉取时的样子存，不解释。 */
@Entity
@Table(name = "school_slots")
public class SchoolSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_code", nullable = false, length = 32)
    private String roomCode;

    @Column(name = "day_of_week", nullable = false)
    private int dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "course_code", nullable = false, length = 32)
    private String courseCode;

    @Column(name = "pulled_at", nullable = false)
    private LocalDateTime pulledAt = LocalDateTime.now();

    protected SchoolSlot() { }

    public SchoolSlot(String roomCode, int dayOfWeek, LocalTime startTime, LocalTime endTime, String courseCode) {
        this.roomCode = roomCode;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseCode = courseCode;
    }

    public Long getId() { return id; }
    public String getRoomCode() { return roomCode; }
    public int getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getCourseCode() { return courseCode; }
    public LocalDateTime getPulledAt() { return pulledAt; }
}
