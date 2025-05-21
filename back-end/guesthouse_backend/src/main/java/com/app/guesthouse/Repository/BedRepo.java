package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedRepo extends JpaRepository<Bed, Long> {
    List<Bed> findByRoomId(Long roomId);
}
