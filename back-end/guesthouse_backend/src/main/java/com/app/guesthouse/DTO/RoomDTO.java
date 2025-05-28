package com.app.guesthouse.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private String roomNo;
    private String roomType;
    private Long guestHouseId;
    private String guestHouseName;
    private Integer numberOfBeds;
    private Double pricePerNight;
}
