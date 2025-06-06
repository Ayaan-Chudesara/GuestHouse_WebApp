package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Entity.GuestHouse;
import com.app.guesthouse.Entity.Room;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.BookingRepo;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Repository.GuestHouseRepo;
import com.app.guesthouse.Service.AdminService;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    private final GuestHouseRepo guestHouseRepo;

    @Autowired
    public AdminServiceImpl(UserRepo userRepo, BookingRepo bookingRepo, BedRepo bedRepo, GuestHouseRepo guestHouseRepo) {
        this.userRepo = userRepo;
        this.bookingRepo = bookingRepo;
        this.bedRepo = bedRepo;
        this.guestHouseRepo = guestHouseRepo;
    }

    private BookingDTO mapToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());

        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setGuestName(booking.getUser().getName());
            dto.setGuestEmail(booking.getUser().getEmail());
        }

        if (booking.getBed() != null) {
            dto.setBedId(booking.getBed().getId());
            dto.setBedNo(booking.getBed().getBedNo());

            if (booking.getBed().getRoom() != null) {
                dto.setRoomId(booking.getBed().getRoom().getId());
                dto.setRoomNo(booking.getBed().getRoom().getRoomNo());
                dto.setRoomType(booking.getBed().getRoom().getRoomType());
                dto.setNumberOfBeds(booking.getBed().getRoom().getNumberOfBeds());

                if (booking.getBed().getRoom().getGuestHouse() != null) {
                    dto.setGuestHouseId(booking.getBed().getRoom().getGuestHouse().getId());
                    dto.setGuestHouseName(booking.getBed().getRoom().getGuestHouse().getName());
                    dto.setGuestHouseLocation(booking.getBed().getRoom().getGuestHouse().getLocation());
                }
            }
        }

        dto.setBookingDate(booking.getBookingDate());
        dto.setDurationDays(booking.getDurationDays());
        dto.setPurpose(booking.getPurpose());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());

        dto.setCheckInDate(booking.getBookingDate());
        dto.setCheckOutDate(booking.getBookingDate().plusDays(booking.getDurationDays()));

        return dto;
    }

    private Booking mapToEntityForUpdate(BookingDTO dto, Booking existingBooking) {

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
        return bookingRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingDTO approveBooking(Long id) {
        Booking booking = bookingRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + id));
        if (booking.getStatus() == Booking.Status.PENDING) {
            booking.setStatus(Booking.Status.APPROVED);
            if (booking.getBed() != null && booking.getBed().getStatus() == Bed.Status.AVAILABLE) {
                booking.getBed().setStatus(Bed.Status.BOOKED);
                bedRepo.save(booking.getBed());
            }
            return this.mapToDTO(bookingRepo.save(booking));
        } else {
            return this.mapToDTO(booking);
        }
    }

    @Override
    @Transactional
    public BookingDTO rejectBooking(Long id) {
        Booking booking = bookingRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + id));
        if (booking.getStatus() == Booking.Status.PENDING || booking.getStatus() == Booking.Status.APPROVED) { // Allow rejecting approved as well
            booking.setStatus(Booking.Status.REJECTED);
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
    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));
        Booking.Status newStatus;
        try {
            newStatus = Booking.Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid booking status provided: " + status);
        }

        Booking.Status oldStatus = booking.getStatus();
        booking.setStatus(newStatus);

        if (booking.getBed() != null) {
            if (newStatus == Booking.Status.CHECKED_OUT && oldStatus == Booking.Status.CHECKED_IN) {
                booking.getBed().setStatus(Bed.Status.AVAILABLE);
                bedRepo.save(booking.getBed());
            } else if (newStatus == Booking.Status.CHECKED_IN && oldStatus == Booking.Status.APPROVED) {
                booking.getBed().setStatus(Bed.Status.BOOKED);
                bedRepo.save(booking.getBed());
            } else if ((newStatus == Booking.Status.CANCELLED || newStatus == Booking.Status.REJECTED) &&
                    (oldStatus == Booking.Status.PENDING || oldStatus == Booking.Status.APPROVED || oldStatus == Booking.Status.CHECKED_IN)) {

                if (booking.getBed().getStatus() == Bed.Status.BOOKED) {
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
        stats.put("availableBeds", bedRepo.countByStatus(Bed.Status.AVAILABLE));
        stats.put("totalBookings", bookingRepo.count());
        stats.put("pendingRequests", bookingRepo.countByStatus(Booking.Status.PENDING));
        stats.put("checkIns", bookingRepo.countByStatus(Booking.Status.CHECKED_IN));
        stats.put("checkOuts", bookingRepo.countByStatus(Booking.Status.CHECKED_OUT));
        return stats;
    }

    @Override
    public List<BookingDTO> getSchedulerData(String start, String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        Specification<Booking> spec = (root, query, criteriaBuilder) -> {
            Predicate overlapsStart = criteriaBuilder.lessThanOrEqualTo(root.get("bookingDate"), endDate);
            Predicate overlapsEnd = criteriaBuilder.greaterThanOrEqualTo(
                    criteriaBuilder.function("DATE_ADD", LocalDate.class, root.get("bookingDate"), criteriaBuilder.literal("DAY"), root.get("durationDays")),
                    startDate
            );
            return criteriaBuilder.and(overlapsStart, overlapsEnd);
        };
        List<Booking> bookings = bookingRepo.findAll(spec);
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Integer getTotalBeds(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                return (int) bedRepo.count();
            }

            System.out.println("Searching for available beds between " + startDate + " and " + endDate);

            // Get all beds
            List<Bed> allBeds = bedRepo.findAll();
            System.out.println("Total beds in system: " + allBeds.size());

            // Count available beds
            int availableBeds = 0;
            for (Bed bed : allBeds) {
                System.out.println("\nChecking bed ID: " + bed.getId() + ", Number: " + bed.getBedNo());
                System.out.println("Bed status: " + bed.getStatus());

                // Check for overlapping bookings regardless of bed status
                List<Booking> overlappingBookings = bookingRepo.findOverlappingBookingsForBed(
                    bed.getId(),
                    startDate,
                    endDate
                );
                
                System.out.println("Found " + overlappingBookings.size() + " overlapping bookings");
                if (overlappingBookings.isEmpty()) {
                    System.out.println("Bed " + bed.getId() + " is available for the date range");
                    availableBeds++;
                } else {
                    System.out.println("Overlapping bookings found for bed " + bed.getId() + ":");
                    for (Booking booking : overlappingBookings) {
                        System.out.println("  - Booking ID: " + booking.getId() + 
                                         ", Date: " + booking.getBookingDate() + 
                                         ", Duration: " + booking.getDurationDays() + 
                                         ", Status: " + booking.getStatus());
                    }
                }
            }

            System.out.println("\nFinal count of available beds: " + availableBeds);
            return availableBeds;

        } catch (Exception e) {
            System.err.println("Error in getTotalBeds: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + id));
        if (booking.getBed() != null && (booking.getStatus() == Booking.Status.APPROVED || booking.getStatus() == Booking.Status.CHECKED_IN)) {
            if (booking.getBed().getStatus() == Bed.Status.BOOKED) {
                booking.getBed().setStatus(Bed.Status.AVAILABLE);
                bedRepo.save(booking.getBed());
            }
        }
        bookingRepo.delete(booking);
    }

    @Override
    @Transactional
    public BookingDTO updateBooking(Long id, BookingDTO bookingDTO) {
        Booking existingBooking = bookingRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + id));

        Booking updatedEntity = mapToEntityForUpdate(bookingDTO, existingBooking);

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

            Join<Booking, Bed> bedJoin = root.join("bed");
            Join<Bed, Room> roomJoin = bedJoin.join("room");

            if (guestHouseId != null) {
                Join<Room, GuestHouse> guestHouseJoin = roomJoin.join("guestHouse");
                predicates.add(criteriaBuilder.equal(guestHouseJoin.get("id"), guestHouseId));
            }
            if (roomType != null && !roomType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(roomJoin.get("roomType"), roomType));
            }

            if (checkInDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("bookingDate"), checkInDate));
            }
            if (checkOutDate != null) {
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