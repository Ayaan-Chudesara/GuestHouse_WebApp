package com.app.guesthouse.Repository;

import com.app.guesthouse.Entity.GuestHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestHouseRepo extends JpaRepository<GuestHouse, Long > {
}
