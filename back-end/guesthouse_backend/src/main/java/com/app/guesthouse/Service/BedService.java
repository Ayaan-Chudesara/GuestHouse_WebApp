package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.BedDTO;

import java.util.List;

public interface BedService {


     List<BedDTO> getAllBeds() ;
     BedDTO getBedById(Long id) ;

     BedDTO createBed(BedDTO dto);

     BedDTO updateBed(Long id, BedDTO dto);
     void deleteBed(Long id) ;

    List<BedDTO> getBedsByRoomId(Long roomId);
}
