package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.AdminBookingRequestDTO;
import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Entity.Room; // Import Room entity
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
                dto.setNumberOfBeds(booking.getBed().getRoom().getNumberOfBeds()); // Correctly reflects room capacity

                if (booking.getBed().getRoom().getGuestHouse() != null) {
                    dto.setGuestHouseId(booking.getBed().getRoom().getGuestHouse().getId());
                    dto.setGuestHouseName(booking.getBed().getRoom().getGuestHouse().getName());
                    dto.setGuestHouseLocation(booking.getBed().getRoom().getGuestHouse().getLocation());
                }
            }
        }

        dto.setBookingDate(booking.getBookingDate()); // This is the checkInDate from entity
        dto.setDurationDays(booking.getDurationDays());
        dto.setPurpose(booking.getPurpose());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setNumberOfGuests(booking.getNumberOfGuests());

        dto.setCheckInDate(booking.getBookingDate()); // Set DTO checkInDate from entity's bookingDate
        dto.setCheckOutDate(booking.getBookingDate().plusDays(booking.getDurationDays())); // Calculate checkOutDate for DTO

        return dto;
    }

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        // 1. Validate input and log request details
        System.out.println("Creating booking with details: " + 
            "userId=" + bookingDTO.getUserId() + ", " +
            "roomId=" + bookingDTO.getRoomId() + ", " +
            "checkInDate=" + bookingDTO.getCheckInDate() + ", " +
            "checkOutDate=" + bookingDTO.getCheckOutDate() + ", " +
            "numberOfGuests=" + bookingDTO.getNumberOfGuests());

        if (bookingDTO.getRoomId() == null) {
            throw new IllegalArgumentException("Room ID is required for booking.");
        }
        if (bookingDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for booking.");
        }
        if (bookingDTO.getNumberOfGuests() == null || bookingDTO.getNumberOfGuests() < 1) {
            throw new IllegalArgumentException("Number of guests must be at least 1.");
        }

        // 2. Verify user exists and fetch user details
        System.out.println("Checking if user exists with ID: " + bookingDTO.getUserId());
        boolean userExists = userRepo.existsById(bookingDTO.getUserId());
        if (!userExists) {
            throw new NoSuchElementException("User not found with ID: " + bookingDTO.getUserId() + ". Please ensure you are logged in correctly.");
        }

        // 3. Fetch User
        User user = userRepo.findById(bookingDTO.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + bookingDTO.getUserId()));
        System.out.println("Found user: " + user.getName() + " (ID: " + user.getId() + ")");

        // 4. Validate Dates and Calculate Duration
        if (bookingDTO.getCheckInDate() == null || bookingDTO.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and Check-out dates are required for booking creation.");
        }

        LocalDate today = LocalDate.now();
        LocalDate checkInDate = bookingDTO.getCheckInDate();
        LocalDate checkOutDate = bookingDTO.getCheckOutDate();

        // Validate check-in date
        if (checkInDate.isBefore(today)) {
            throw new IllegalArgumentException("Check-in date cannot be in the past. Selected: " + checkInDate + ", Today: " + today);
        }

        // Validate check-out date
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date. Check-in: " + checkInDate + ", Check-out: " + checkOutDate);
        }

        // Calculate duration
        long durationDays = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (durationDays == 0) {
            throw new IllegalArgumentException("Booking duration must be at least one day. Check-in: " + checkInDate + ", Check-out: " + checkOutDate);
        }

        // 5. Ensure the Room exists and validate capacity
        Room room = roomRepo.findById(bookingDTO.getRoomId())
                .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + bookingDTO.getRoomId()));
        System.out.println("Found room: " + room.getRoomNo() + " (ID: " + room.getId() + ")");

        if (bookingDTO.getNumberOfGuests() > room.getNumberOfBeds()) {
            throw new IllegalArgumentException("Number of guests (" + bookingDTO.getNumberOfGuests() + 
                ") exceeds room capacity (" + room.getNumberOfBeds() + ").");
        }

        // 6. Search for an AVAILABLE Bed within this specific Room for the given dates
        List<Bed> availableBedsInRoom = bedRepo.findFirstAvailableBedInRoomForDates(
                room.getId(),
                checkInDate,
                checkOutDate
        );

        if (availableBedsInRoom.isEmpty()) {
            throw new IllegalStateException(
                "No available beds found in Room " + room.getRoomNo() + 
                " for the dates " + checkInDate + 
                " to " + checkOutDate + 
                ". Please select different dates or a different room."
            );
        }

        // Select the first available bed found
        Bed selectedBed = availableBedsInRoom.get(0);
        System.out.println("Selected bed: " + selectedBed.getBedNo() + " (ID: " + selectedBed.getId() + ")");

        // 7. Create Booking entity
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setBed(selectedBed);
        booking.setBookingDate(checkInDate);
        booking.setDurationDays((int) durationDays);
        booking.setPurpose(bookingDTO.getPurpose());
        booking.setStatus(Booking.Status.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setNumberOfGuests(bookingDTO.getNumberOfGuests());

        // 8. Update Bed Status
        selectedBed.setStatus(Bed.Status.BOOKED);
        bedRepo.save(selectedBed);
        System.out.println("Updated bed status to BOOKED");

        // 9. Save Booking
        try {
            Booking saved = bookingRepo.save(booking);
            System.out.println("Successfully created booking with ID: " + saved.getId());
            
            // 10. Send Notification
            try {
                mailService.sendBookingNotification(saved.getUser().getEmail(), saved);
            } catch (Exception e) {
                System.err.println("Email sending failed for booking " + saved.getId() + ": " + e.getMessage());
            }

            return mapToDTO(saved);
        } catch (Exception e) {
            System.err.println("Error saving booking: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
        // Logic for setting bed back to AVAILABLE should correctly handle status transitions
        if (booking.getBed() != null &&
                (booking.getStatus() == Booking.Status.APPROVED ||
                        booking.getStatus() == Booking.Status.CHECKED_IN ||
                        booking.getStatus() == Booking.Status.PENDING) // Also consider PENDING bookings that are deleted
        ) {
            // Only set to AVAILABLE if no other active bookings exist for this bed.
            // This is a more robust check:
            boolean otherActiveBookings = bookingRepo.findOverlappingBookingsForBedExcludingCurrent(
                    booking.getBed().getId(),
                    booking.getBookingDate(),
                    booking.getBookingDate().plusDays(booking.getDurationDays()),
                    booking.getId()
            ).size() > 0;

            if (!otherActiveBookings && booking.getBed().getStatus() == Bed.Status.BOOKED) {
                booking.getBed().setStatus(Bed.Status.AVAILABLE);
                bedRepo.save(booking.getBed());
            }
        }
        bookingRepo.delete(booking);
    }

    public List<BookingDTO> getBookingsByUser(Long userId) {
        List<Booking> bookings = bookingRepo.findByUserId(userId); // Assuming findByUserId exists in BookingRepo
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Transactional
    public BookingDTO updateBookingStatus(Long bookingId, Booking.Status status) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new NoSuchElementException("Booking not found with ID: " + bookingId));
        Booking.Status oldStatus = booking.getStatus();
        booking.setStatus(status);

        if (booking.getBed() != null) {
            if (status == Booking.Status.CHECKED_OUT && oldStatus == Booking.Status.CHECKED_IN) {
                // When checking out, release the bed if no other active bookings overlap its original dates
                boolean otherActiveBookings = bookingRepo.findOverlappingBookingsForBedExcludingCurrent(
                        booking.getBed().getId(),
                        booking.getBookingDate(),
                        booking.getBookingDate().plusDays(booking.getDurationDays()),
                        booking.getId()
                ).size() > 0;

                if (!otherActiveBookings) {
                    booking.getBed().setStatus(Bed.Status.AVAILABLE);
                    bedRepo.save(booking.getBed());
                }
            } else if (status == Booking.Status.CHECKED_IN && oldStatus == Booking.Status.APPROVED) {
                // Ensure bed is marked BOOKED if it moves to CHECKED_IN
                booking.getBed().setStatus(Bed.Status.BOOKED);
                bedRepo.save(booking.getBed());
            } else if ((status == Booking.Status.CANCELLED || status == Booking.Status.REJECTED) &&
                    (oldStatus == Booking.Status.PENDING || oldStatus == Booking.Status.APPROVED || oldStatus == Booking.Status.CHECKED_IN)) {
                // If cancelled/rejected, release the bed if no other active bookings overlap its original dates
                boolean otherActiveBookings = bookingRepo.findOverlappingBookingsForBedExcludingCurrent(
                        booking.getBed().getId(),
                        booking.getBookingDate(),
                        booking.getBookingDate().plusDays(booking.getDurationDays()),
                        booking.getId()
                ).size() > 0;

                if (!otherActiveBookings) {
                    booking.getBed().setStatus(Bed.Status.AVAILABLE);
                    bedRepo.save(booking.getBed());
                }
            }
        }
        return mapToDTO(bookingRepo.save(booking));
    }


    public List<BookingDTO> getBookingsBetweenDates(LocalDate start, LocalDate end) {
        List<Booking> bookings = bookingRepo.findByBookingDateBetween(start, end); // Assuming this method exists in BookingRepo
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createBookingAsAdmin(AdminBookingRequestDTO request) {
        try {
            // 1. Validate request
            if (request.getGuestName() == null || request.getGuestName().trim().isEmpty()) {
                throw new IllegalArgumentException("Guest name is required");
            }
            if (request.getGuestEmail() == null || request.getGuestEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("Guest email is required");
            }
            if (request.getGuestHouseId() == null) {
                throw new IllegalArgumentException("Guesthouse ID is required");
            }
            if (request.getRoomType() == null || request.getRoomType().trim().isEmpty()) {
                throw new IllegalArgumentException("Room type is required");
            }
            if (request.getNumberOfBeds() <= 0) {
                throw new IllegalArgumentException("Number of beds must be greater than 0");
            }

            // 2. Find or create User
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

            // 3. Calculate duration and validate dates
            if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
                throw new IllegalArgumentException("Check-in and Check-out dates are required.");
            }
            if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
                throw new IllegalArgumentException("Check-out date cannot be before check-in date.");
            }
            if (request.getCheckInDate().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Check-in date cannot be in the past.");
            }
            long durationDays = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
            if (durationDays == 0) {
                throw new IllegalArgumentException("Booking duration must be at least one day.");
            }

            // 4. Find a suitable and AVAILABLE Bed
            List<Bed> candidateBeds = bedRepo.findAvailableBedsByCriteria(
                    request.getGuestHouseId(),
                    request.getRoomType(),
                    request.getNumberOfBeds(),
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );

            if (candidateBeds.isEmpty()) {
                throw new IllegalStateException(
                    String.format("No available beds found for guesthouse ID %d, room type %s, capacity %d, dates %s to %s",
                        request.getGuestHouseId(),
                        request.getRoomType(),
                        request.getNumberOfBeds(),
                        request.getCheckInDate(),
                        request.getCheckOutDate())
                );
            }

            Bed selectedBed = candidateBeds.get(0);

            // 5. Create Booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setBed(selectedBed);
            booking.setBookingDate(request.getCheckInDate());
            booking.setDurationDays((int) durationDays);
            booking.setPurpose(request.getPurpose());
            booking.setCreatedAt(LocalDateTime.now());
            booking.setStatus(Booking.Status.APPROVED); // Default to APPROVED for admin bookings
            
            // Set numberOfGuests to numberOfBeds if not provided
            int numberOfGuests = request.getNumberOfGuests();
            if (numberOfGuests <= 0) {
                numberOfGuests = request.getNumberOfBeds();
            }
            booking.setNumberOfGuests(numberOfGuests);

            // 6. Update Bed Status to BOOKED
            selectedBed.setStatus(Bed.Status.BOOKED);
            bedRepo.save(selectedBed);

            // 7. Save Booking
            Booking savedBooking = bookingRepo.save(booking);

            // 8. Send email notification
            try {
                mailService.sendBookingNotification(user.getEmail(), savedBooking);
            } catch (Exception e) {
                System.err.println("Email sending failed for admin-created booking: " + e.getMessage());
                // Don't throw here, as the booking was successful
            }
        } catch (Exception e) {
            // Log the full error for debugging
            System.err.println("Error in createBookingAsAdmin: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to maintain the transaction
        }
    }
}