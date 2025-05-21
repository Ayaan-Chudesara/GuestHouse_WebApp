package com.app.guesthouse.DTO;

import ch.qos.logback.core.status.Status;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Room;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BedDTO {
    private Long id;
    private String bedNo;
    private Bed.Status status;

    private Long roomId;     // lightweight linkage
    private String roomNo;


}
