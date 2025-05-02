package com.app.guesthouse.DTO;

import ch.qos.logback.core.status.Status;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingDTO {

    private Long id;
    private User user;
    private Bed bed;
    private LocalDate bookingDate;
    private Integer durationDays;
    private String purpose;
    private Status status;
    private LocalDateTime createdAt;

    public BookingDTO(){}

    public BookingDTO(Long id, User user, Bed bed, LocalDate bookingDate, Integer durationDays, String purpose, Status status, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.bed = bed;
        this.bookingDate = bookingDate;
        this.durationDays = durationDays;
        this.purpose = purpose;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Bed getBed() {
        return bed;
    }

    public void setBed(Bed bed) {
        this.bed = bed;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
