package com.app.guesthouse.Service;

import com.app.guesthouse.DTO.BedDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Room;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.RoomRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

public interface BedService {


    public List<BedDTO> getAllBeds() ;
    public BedDTO getBedById(Long id) ;

    public BedDTO createBed(BedDTO dto);

    public BedDTO updateBed(Long id, BedDTO dto);
    public void deleteBed(Long id) ;

    List<BedDTO> getBedsByRoomId(Long roomId);
}
