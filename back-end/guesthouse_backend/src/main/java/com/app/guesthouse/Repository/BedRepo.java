package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BedRepo extends JpaRepository<Bed, Long> {

}
