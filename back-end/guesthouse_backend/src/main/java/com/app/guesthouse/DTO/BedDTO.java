package com.app.guesthouse.DTO;

import ch.qos.logback.core.status.Status;
import com.app.guesthouse.Entity.Room;

public class BedDTO {

    private Long id;
    private String bedNo;
    private Status status;
    private Room room;

    public BedDTO(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBedNo() {
        return bedNo;
    }

    public void setBedNo(String bedNo) {
        this.bedNo = bedNo;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public BedDTO(Long id, String bedNo, Status status, Room room){
        this.id = id;
        this.bedNo = bedNo;
        this.status = status;
        this.room = room;
    }
}
