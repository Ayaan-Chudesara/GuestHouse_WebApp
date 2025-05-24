package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Entity.GuestHouse;
import com.app.guesthouse.Entity.Room;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.BookingRepo;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Repository.GuestHouseRepo;
import com.app.guesthouse.Service.AdminService;
import jakarta.persistence.criteria.Join; // Correct import for Join
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Added for transactional methods

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; // Import for ChronoUnit
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepo userRepo;
    private final BookingRepo bookingRepo;
    private final BedRepo bedRepo;
    private final GuestHouseRepo guestHouseRepo; // Keep this if you need it elsewhere, though not directly used in mapToDTO for guestHouseId

    // Constructor Injection
    @Autowired
    public AdminServiceImpl(UserRepo userRepo, BookingRepo bookingRepo, BedRepo bedRepo, GuestHouseRepo guestHouseRepo) {
        this.userRepo = userRepo;
        this.bookingRepo = bookingRepo;
        this.bedRepo = bedRepo;
        this.guestHouseRepo = guestHouseRepo;
    }

    // UPDATED: mapToDTO to correctly populate flattened BookingDTO
    private BookingDTO mapToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());

        // Populate User details from Booking.user
        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId()); // Set user ID
            dto.setGuestName(booking.getUser().getName());
            dto.setGuestEmail(booking.getUser().getEmail());
        }

        // Populate Bed, Room, and GuestHouse details from Booking.bed traversal
        if (booking.getBed() != null) {
            dto.setBedId(booking.getBed().getId()); // Set bed ID
            dto.setBedNo(booking.getBed().getBedNo()); // Set bed number
            // dto.setBedStatus(booking.getBed().getStatus().name()); // If you want bed status

            if (booking.getBed().getRoom() != null) { // Assuming Bed has a Room entity
                dto.setRoomId(booking.getBed().getRoom().getId()); // Set room ID
                dto.setRoomNo(booking.getBed().getRoom().getRoomNo()); // Set room number
                dto.setRoomType(booking.getBed().getRoom().getRoomType());
                dto.setNumberOfBeds(booking.getBed().getRoom().getNumberOfBeds()); // From Room, not Bed directly

                if (booking.getBed().getRoom().getGuestHouse() != null) { // Assuming Room has a GuestHouse entity
                    dto.setGuestHouseId(booking.getBed().getRoom().getGuestHouse().getId());
                    dto.setGuestHouseName(booking.getBed().getRoom().getGuestHouse().getName());
                    dto.setGuestHouseLocation(booking.getBed().getRoom().getGuestHouse().getLocation());
                }
            }
        }

        dto.setBookingDate(booking.getBookingDate());
        dto.setDurationDays(booking.getDurationDays());
        dto.setPurpose(booking.getPurpose());
        dto.setStatus(booking.getStatus()); // Use the enum directly
        dto.setCreatedAt(booking.getCreatedAt());

        // Derive checkInDate and checkOutDate for frontend (assuming bookingDate is check-in)
        dto.setCheckInDate(booking.getBookingDate());
        dto.setCheckOutDate(booking.getBookingDate().plusDays(booking.getDurationDays()));

        return dto;
    }

    // Helper method to map DTO back to Entity for updates (only for fields allowed to be updated)
    // This is simplified and does not handle changes to associated User or Bed.
    // For changing User/Bed, separate methods are highly recommended.
    private Booking mapToEntityForUpdate(BookingDTO dto, Booking existingBooking) {
        // ID, User, Bed are typically not updated via this method for a flat DTO.
        // If the DTO contains new user/bed IDs for association, you'd fetch them here.
        // For now, assuming only direct booking fields are updated.

        if (dto.getBookingDate() != null) {
            existingBooking.setBookingDate(dto.getBookingDate());
        }
        if (dto.getDurationDays() != null) {
            existingBooking.setDurationDays(dto.getDurationDays());
        }
        if (dto.getPurpose() != null) {
            existingBooking.setPurpose(dto.getPurpose());
        }
        if (dto.getStatus() != null) {
            existingBooking.setStatus(dto.getStatus());
        }

        return existingBooking;
    }

    @Override
    public List<BookingDTO> getAllBookings() {
        // Ensure that associated entities (User, Bed, Room, GuestHouse) are fetched
        // to avoid N+1 problems, especially if using LAZY fetching.
        // You might need to add `@EntityGraph` or use explicit `JOIN FETCH` in your `BookingRepo` for this.
        return bookingRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional // Ensure atomicity for status changes
    public BookingDTO approveBooking(Long id) {
        Booking booking = bookingRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + id));
        if (booking.getStatus() == Booking.Status.PENDING) {
            booking.setStatus(Booking.Status.APPROVED);
            // Optionally, update bed status if it's not already BOOKED/OCCUPIED
            if (booking.getBed() != null && booking.getBed().getStatus() == Bed.Status.AVAILABLE) {
                booking.getBed().setStatus(Bed.Status.BOOKED);
                bedRepo.save(booking.getBed());
            }
            return this.mapToDTO(bookingRepo.save(booking));
        } else {
            // If the status is not PENDING, perhaps log and return current state or throw an error.
            // For now, just return the current DTO if no change.
            return this.mapToDTO(booking);
        }
    }

    @Override
    @Transactional // Ensure atomicity for status changes
    public BookingDTO rejectBooking(Long id) {
        Booking booking = bookingRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + id));
        if (booking.getStatus() == Booking.Status.PENDING || booking.getStatus() == Booking.Status.APPROVED) { // Allow rejecting approved as well
            booking.setStatus(Booking.Status.REJECTED);
            // If a booking is rejected, its bed should become AVAILABLE again
            if (booking.getBed() != null && booking.getBed().getStatus() == Bed.Status.BOOKED) {
                booking.getBed().setStatus(Bed.Status.AVAILABLE);
                bedRepo.save(booking.getBed());
            }
            return this.mapToDTO(bookingRepo.save(booking));
        } else {
            return this.mapToDTO(booking);
        }
    }

    @Override
    public List<BookingDTO> getPendingBookings() {
        return bookingRepo.findByStatus(Booking.Status.PENDING).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional // Transactional for updates
    public BookingDTO updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));
        Booking.Status newStatus;
        try {
            newStatus = Booking.Status.valueOf(status.toUpperCase()); // Ensure case-insensitivity
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid booking status provided: " + status);
        }

        Booking.Status oldStatus = booking.getStatus();
        booking.setStatus(newStatus);

        // Logic to manage bed status based on booking status transitions
        if (booking.getBed() != null) {
            if (newStatus == Booking.Status.CHECKED_OUT && oldStatus == Booking.Status.CHECKED_IN) {
                booking.getBed().setStatus(Bed.Status.AVAILABLE);
                bedRepo.save(booking.getBed());
            } else if (newStatus == Booking.Status.CHECKED_IN && oldStatus == Booking.Status.APPROVED) {
                booking.getBed().setStatus(Bed.Status.BOOKED); // Should ideally be BOOKED already, but ensures
                bedRepo.save(booking.getBed());
            } else if ((newStatus == Booking.Status.CANCELLED || newStatus == Booking.Status.REJECTED) &&
                    (oldStatus == Booking.Status.PENDING || oldStatus == Booking.Status.APPROVED || oldStatus == Booking.Status.CHECKED_IN)) {
                // If cancelled/rejected from an active state, make bed available
                if (booking.getBed().getStatus() == Bed.Status.BOOKED) { // Or other occupied statuses
                    booking.getBed().setStatus(Bed.Status.AVAILABLE);
                    bedRepo.save(booking.getBed());
                }
            }
        }
        return this.mapToDTO(bookingRepo.save(booking));
    }


    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        // These keys should match what your frontend expects
        stats.put("availableBeds", bedRepo.countByStatus(Bed.Status.AVAILABLE)); // Renamed from availableRooms
        stats.put("totalBookings", bookingRepo.count()); // Renamed from reservations
        stats.put("pendingRequests", bookingRepo.countByStatus(Booking.Status.PENDING));
        stats.put("checkIns", bookingRepo.countByStatus(Booking.Status.CHECKED_IN));
        stats.put("checkOuts", bookingRepo.countByStatus(Booking.Status.CHECKED_OUT));
        return stats;
    }

    @Override
    public List<BookingDTO> getSchedulerData(String start, String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        // Find bookings that overlap with the given date range [startDate, endDate]
        Specification<Booking> spec = (root, query, criteriaBuilder) -> {
            // (booking.bookingDate <= endDate AND booking.bookingDate + durationDays >= startDate)
            Predicate overlapsStart = criteriaBuilder.lessThanOrEqualTo(root.get("bookingDate"), endDate);
            Predicate overlapsEnd = criteriaBuilder.greaterThanOrEqualTo(
                    // Use DATE_ADD function for databases or handle in Java if DB function is problematic
                    criteriaBuilder.function("DATE_ADD", LocalDate.class, root.get("bookingDate"), criteriaBuilder.literal("DAY"), root.get("durationDays")),
                    startDate
            );
            return criteriaBuilder.and(overlapsStart, overlapsEnd);
        };
        // Use findAll(Specification) from JpaSpecificationExecutor
        List<Booking> bookings = bookingRepo.findAll(spec);
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Integer getTotalBeds() {
        return (int) bedRepo.count();
    }

    @Override
    @Transactional // Transactional for deletion
    public void deleteBooking(Long id) {
        Booking booking = bookingRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + id));
        // Logic to release the bed if this booking was occupying it
        if (booking.getBed() != null && (booking.getStatus() == Booking.Status.APPROVED || booking.getStatus() == Booking.Status.CHECKED_IN)) {
            // Only set to AVAILABLE if it was indeed BOOKED by this booking
            if (booking.getBed().getStatus() == Bed.Status.BOOKED) {
                booking.getBed().setStatus(Bed.Status.AVAILABLE);
                bedRepo.save(booking.getBed());
            }
        }
        bookingRepo.delete(booking);
    }

    @Override
    @Transactional // Transactional for updates
    public BookingDTO updateBooking(Long id, BookingDTO bookingDTO) {
        Booking existingBooking = bookingRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + id));

        // Use the mapToEntityForUpdate helper to update fields
        // This helper only updates direct fields. For changing associated User/Bed,
        // you would need separate logic here.
        Booking updatedEntity = mapToEntityForUpdate(bookingDTO, existingBooking);

        // Save and return the updated DTO
        return mapToDTO(bookingRepo.save(updatedEntity));
    }

    @Override
    public List<BookingDTO> getFilteredBookings(
            Long guestHouseId,
            String roomType,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            String status) {

        Specification<Booking> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Joins to access nested entities for filtering
            Join<Booking, Bed> bedJoin = root.join("bed"); // join with Bed entity
            Join<Bed, Room> roomJoin = bedJoin.join("room"); // join from Bed to Room

            if (guestHouseId != null) {
                Join<Room, GuestHouse> guestHouseJoin = roomJoin.join("guestHouse"); // join from Room to GuestHouse
                predicates.add(criteriaBuilder.equal(guestHouseJoin.get("id"), guestHouseId));
            }
            if (roomType != null && !roomType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(roomJoin.get("roomType"), roomType));
            }

            if (checkInDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("bookingDate"), checkInDate));
            }
            if (checkOutDate != null) {
                // A more accurate check-out filter for overlap:
                // (bookingDate + durationDays >= checkInDate_filter)
                // AND (bookingDate <= checkOutDate_filter)
                // But the filter seems to be for bookings whose *check-out* is on or before a given date.
                // Assuming checkOutDate parameter means "bookings ending by this date":
                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                criteriaBuilder.function("DATE_ADD", LocalDate.class, root.get("bookingDate"), criteriaBuilder.literal("DAY"), root.get("durationDays")),
                                checkOutDate
                        )
                );
            }
            if (status != null && !status.equalsIgnoreCase("ALL")) {
                try {
                    predicates.add(criteriaBuilder.equal(root.get("status"), Booking.Status.valueOf(status.toUpperCase())));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid booking status provided for filter: " + status);
                    // Decide whether to throw an exception or ignore invalid status
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<Booking> filteredBookings = bookingRepo.findAll(spec);
        return filteredBookings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
}