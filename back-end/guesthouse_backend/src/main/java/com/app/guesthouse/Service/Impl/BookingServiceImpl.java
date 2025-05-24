package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.AdminBookingRequestDTO;
import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.BookingRepo;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Repository.RoomRepo;
import com.app.guesthouse.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepo bookingRepo;
    private final BedRepo bedRepo;
    private final UserRepo userRepo;
    private final RoomRepo roomRepo;
    private final MailServiceImpl mailService;

    @Autowired
    public BookingServiceImpl(BookingRepo bookingRepo, BedRepo bedRepo, UserRepo userRepo, RoomRepo roomRepo, MailServiceImpl mailService) {
        this.bookingRepo = bookingRepo;
        this.bedRepo = bedRepo;
        this.userRepo = userRepo;
        this.roomRepo = roomRepo;
        this.mailService = mailService;
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

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        User user = userRepo.findById(bookingDTO.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + bookingDTO.getUserId()));

        Bed bed = bedRepo.findById(bookingDTO.getBedId())
                .orElseThrow(() -> new NoSuchElementException("Bed not found with ID: " + bookingDTO.getBedId()));

        if (bookingDTO.getCheckInDate() == null || bookingDTO.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and Check-out dates are required for booking creation.");
        }
        if (bookingDTO.getCheckOutDate().isBefore(bookingDTO.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date cannot be before check-in date.");
        }
        long durationDays = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());
        if (durationDays == 0) {
            throw new IllegalArgumentException("Booking duration must be at least one day.");
        }

        // --- Crucial Availability Check ---
        List<Booking> overlappingBookings = bookingRepo.findOverlappingBookingsForBed(
                bed.getId(),
                bookingDTO.getCheckInDate(),
                bookingDTO.getCheckOutDate().minusDays(1) // End date for overlap check is day before checkout
        );

        if (!overlappingBookings.isEmpty()) {
            throw new IllegalStateException("Bed (ID: " + bed.getId() + ") is not available for the requested dates due to existing bookings.");
        }
        if (bed.getStatus() != Bed.Status.AVAILABLE) {
            throw new IllegalStateException("Bed (ID: " + bed.getId() + ") is not available for booking (current status: " + bed.getStatus() + ").");
        }
        // --- End Availability Check ---

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBed(bed);
        booking.setBookingDate(bookingDTO.getCheckInDate());
        booking.setDurationDays((int) durationDays);
        booking.setPurpose(bookingDTO.getPurpose());
        booking.setStatus(Booking.Status.PENDING);
        booking.setCreatedAt(LocalDateTime.now());

        bed.setStatus(Bed.Status.BOOKED); // Mark bed as booked
        bedRepo.save(bed);

        Booking saved = bookingRepo.save(booking);

        try {
            mailService.sendBookingNotification(saved.getUser().getEmail(), saved);
        } catch (Exception e) {
            System.out.println("Email sending failed for booking " + saved.getId() + ": " + e.getMessage());
        }

        return mapToDTO(saved);
    }


    public List<BookingDTO> getAllBookings() {
        return bookingRepo.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public BookingDTO getBookingById(Long id) {
        return bookingRepo.findById(id).map(this::mapToDTO).orElse(null);
    }

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

    public List<BookingDTO> getBookingsByUser(Long userId) {
        List<Booking> bookings = bookingRepo.findByUserId(userId);
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, Booking.Status status) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));
        Booking.Status oldStatus = booking.getStatus();
        booking.setStatus(status);

        if (booking.getBed() != null) {
            if (status == Booking.Status.CHECKED_OUT && oldStatus == Booking.Status.CHECKED_IN) {
                booking.getBed().setStatus(Bed.Status.AVAILABLE);
                bedRepo.save(booking.getBed());
            } else if (status == Booking.Status.CHECKED_IN && oldStatus == Booking.Status.APPROVED) {
                booking.getBed().setStatus(Bed.Status.BOOKED);
                bedRepo.save(booking.getBed());
            } else if ((status == Booking.Status.CANCELLED || status == Booking.Status.REJECTED) &&
                    (oldStatus == Booking.Status.PENDING || oldStatus == Booking.Status.APPROVED || oldStatus == Booking.Status.CHECKED_IN)) {
                if (booking.getBed().getStatus() == Bed.Status.BOOKED) {
                    booking.getBed().setStatus(Bed.Status.AVAILABLE);
                    bedRepo.save(booking.getBed());
                }
            }
        }
        return mapToDTO(bookingRepo.save(booking));
    }


    public List<BookingDTO> getBookingsBetweenDates(LocalDate start, LocalDate end) {
        List<Booking> bookings = bookingRepo.findByBookingDateBetween(start, end);
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createBookingAsAdmin(AdminBookingRequestDTO request) {
        // 1. Find or create User
        User user = userRepo.findByEmail(request.getGuestEmail())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setName(request.getGuestName());
                    newUser.setEmail(request.getGuestEmail());
                    newUser.setPhone("N/A");
                    newUser.setPassword("default_hashed_password"); // Ensure secure hashing here
                    newUser.setRole(User.Role.USER);
                    return userRepo.save(newUser);
                });

        // 2. Calculate duration and validate dates
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and Check-out dates are required.");
        }
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date cannot be before check-in date.");
        }
        long durationDays = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (durationDays == 0) {
            throw new IllegalArgumentException("Booking duration must be at least one day.");
        }

        // 3. Find a single suitable and AVAILABLE Bed
        List<Bed> candidateBeds = bedRepo.findAvailableBedsByCriteria(
                request.getGuestHouseId(),
                request.getRoomType(),
                request.getNumberOfBeds(),
                request.getCheckInDate(),
                request.getCheckOutDate().minusDays(1)
        );

        if (candidateBeds.isEmpty()) {
            throw new RuntimeException("No available bed found for the specified criteria and dates.");
        }

        Bed selectedBed = candidateBeds.get(0);

        // 4. Create Booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBed(selectedBed);
        booking.setBookingDate(request.getCheckInDate());
        booking.setDurationDays((int) durationDays);
        booking.setPurpose(request.getPurpose());
        booking.setCreatedAt(LocalDateTime.now());

        // REMOVED THE OFFENDING LINE: AdminBookingRequestDTO does not have getStatus().
        // Admin-created bookings are typically approved automatically or set to a specific initial status.
        booking.setStatus(Booking.Status.APPROVED); // Default to APPROVED for admin bookings

        // 5. Update Bed Status to BOOKED
        selectedBed.setStatus(Bed.Status.BOOKED);
        bedRepo.save(selectedBed);

        // 6. Save Booking
        bookingRepo.save(booking);

        try {
            mailService.sendBookingNotification(user.getEmail(), booking);
        } catch (Exception e) {
            System.err.println("Email sending failed for admin-created booking: " + e.getMessage());
        }
    }
}