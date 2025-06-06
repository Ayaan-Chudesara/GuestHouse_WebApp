package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.AdminBookingRequestDTO;
import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Entity.Room;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Entity.GuestHouse;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.BookingRepo;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Repository.RoomRepo;
import com.app.guesthouse.Repository.GuestHouseRepo;
import com.app.guesthouse.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    private final GuestHouseRepo guestHouseRepo;
    private final MailServiceImpl mailService;

    @Autowired
    public BookingServiceImpl(BookingRepo bookingRepo, BedRepo bedRepo, UserRepo userRepo, RoomRepo roomRepo, GuestHouseRepo guestHouseRepo, MailServiceImpl mailService) {
        this.bookingRepo = bookingRepo;
        this.bedRepo = bedRepo;
        this.userRepo = userRepo;
        this.roomRepo = roomRepo;
        this.guestHouseRepo = guestHouseRepo;
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
        dto.setNumberOfGuests(booking.getNumberOfGuests());

        dto.setCheckInDate(booking.getBookingDate());
        dto.setCheckOutDate(booking.getBookingDate().plusDays(booking.getDurationDays()));

        return dto;
    }

    @Transactional
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        System.out.println("Creating booking with details: " + 
            "userId=" + bookingDTO.getUserId() + ", " +
            "roomId=" + bookingDTO.getRoomId() + ", " +
            "checkInDate=" + bookingDTO.getCheckInDate() + ", " +
            "checkOutDate=" + bookingDTO.getCheckOutDate() + ", " +
            "numberOfGuests=" + bookingDTO.getNumberOfGuests());

        // Validate input
        validateBookingInput(bookingDTO);

        User user = userRepo.findById(bookingDTO.getUserId())
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + bookingDTO.getUserId()));
        System.out.println("Found user: " + user.getName() + " (ID: " + user.getId() + ")");

        Room room = roomRepo.findById(bookingDTO.getRoomId())
                .orElseThrow(() -> new NoSuchElementException("Room not found with ID: " + bookingDTO.getRoomId()));
        System.out.println("Found room: " + room.getRoomNo() + " (ID: " + room.getId() + ")");

        // Calculate duration
        long durationDays = ChronoUnit.DAYS.between(bookingDTO.getCheckInDate(), bookingDTO.getCheckOutDate());

        // Find available beds
        List<Bed> availableBeds = bedRepo.findAvailableBedsByCriteria(
                room.getGuestHouse().getId(),
                room.getRoomType(),
                bookingDTO.getNumberOfGuests(),
                bookingDTO.getCheckInDate(),
                bookingDTO.getCheckOutDate()
        );

        if (availableBeds.size() < bookingDTO.getNumberOfGuests()) {
            throw new IllegalStateException(
                String.format("Not enough beds available. Required: %d, Available: %d", 
                    bookingDTO.getNumberOfGuests(), 
                    availableBeds.size())
            );
        }

        // Create a booking for each bed needed
        List<Booking> bookings = new ArrayList<>();
        for (int i = 0; i < bookingDTO.getNumberOfGuests(); i++) {
            Bed selectedBed = availableBeds.get(i);
            
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setBed(selectedBed);
            booking.setBookingDate(bookingDTO.getCheckInDate());
            booking.setDurationDays((int) durationDays);
            booking.setPurpose(bookingDTO.getPurpose());
            booking.setStatus(Booking.Status.PENDING);
            booking.setCreatedAt(LocalDateTime.now());
            booking.setNumberOfGuests(1); // Each booking represents one guest/bed

            selectedBed.setStatus(Bed.Status.BOOKED);
            bedRepo.save(selectedBed);
            
            bookings.add(bookingRepo.save(booking));
        }

        // Send email notification
        try {
            for (Booking booking : bookings) {
                mailService.sendBookingNotification(user.getEmail(), booking);
            }
        } catch (Exception e) {
            System.err.println("Email sending failed: " + e.getMessage());
        }

        // Return the first booking's DTO (frontend can fetch others if needed)
        return mapToDTO(bookings.get(0));
    }

    private void validateBookingInput(BookingDTO bookingDTO) {
        if (bookingDTO.getRoomId() == null) {
            throw new IllegalArgumentException("Room ID is required for booking.");
        }
        if (bookingDTO.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required for booking.");
        }
        if (bookingDTO.getNumberOfGuests() == null || bookingDTO.getNumberOfGuests() < 1) {
            throw new IllegalArgumentException("Number of guests must be at least 1.");
        }
        if (bookingDTO.getCheckInDate() == null || bookingDTO.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and Check-out dates are required for booking creation.");
        }

        LocalDate today = LocalDate.now();
        LocalDate checkInDate = bookingDTO.getCheckInDate();
        LocalDate checkOutDate = bookingDTO.getCheckOutDate();

        if (checkInDate.isBefore(today)) {
            throw new IllegalArgumentException("Check-in date cannot be in the past. Selected: " + checkInDate + ", Today: " + today);
        }
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date. Check-in: " + checkInDate + ", Check-out: " + checkOutDate);
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
        if (booking.getBed() != null &&
                (booking.getStatus() == Booking.Status.APPROVED ||
                        booking.getStatus() == Booking.Status.CHECKED_IN ||
                        booking.getStatus() == Booking.Status.PENDING)
        ) {
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
                booking.getBed().setStatus(Bed.Status.BOOKED);
                bedRepo.save(booking.getBed());
            } else if ((status == Booking.Status.CANCELLED || status == Booking.Status.REJECTED) &&
                    (oldStatus == Booking.Status.PENDING || oldStatus == Booking.Status.APPROVED || oldStatus == Booking.Status.CHECKED_IN)) {
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
        List<Booking> bookings = bookingRepo.findByBookingDateBetween(start, end);
        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void createBookingAsAdmin(AdminBookingRequestDTO request) {
        try {
            System.out.println("\n=== Starting Admin Booking Creation ===");
            System.out.println("Guest House ID: " + request.getGuestHouseId());
            System.out.println("Room Type: " + request.getRoomType());
            System.out.println("Number of Beds: " + request.getNumberOfBeds());
            System.out.println("Check-in Date: " + request.getCheckInDate());
            System.out.println("Check-out Date: " + request.getCheckOutDate());

            validateAdminBookingRequest(request);

            // First check if the guest house exists
            GuestHouse guestHouse = guestHouseRepo.findById(request.getGuestHouseId())
                    .orElseThrow(() -> new NoSuchElementException("Guest House not found with ID: " + request.getGuestHouseId()));
            System.out.println("Found Guest House: " + guestHouse.getName());

            // Check if there are any rooms of the requested type
            List<Room> rooms = roomRepo.findByGuestHouseIdAndRoomType(request.getGuestHouseId(), request.getRoomType());
            System.out.println("Found " + rooms.size() + " rooms of type " + request.getRoomType());
            if (rooms.isEmpty()) {
                throw new IllegalStateException("No rooms found of type " + request.getRoomType() + " in the selected guest house");
            }

            User user = userRepo.findByEmail(request.getGuestEmail())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setName(request.getGuestName());
                        newUser.setEmail(request.getGuestEmail());
                        newUser.setPhone("N/A");
                        newUser.setPassword("default_hashed_password");
                        newUser.setRole(User.Role.USER);
                        return userRepo.save(newUser);
                    });

            System.out.println("User found/created: " + user.getName());

            long durationDays = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
            System.out.println("Duration days: " + durationDays);

            // Find available beds based on criteria
            List<Bed> availableBeds = bedRepo.findAvailableBedsByCriteria(
                    request.getGuestHouseId(),
                    request.getRoomType(),
                    1, // We only need rooms with at least 1 bed since we're booking individual beds
                    request.getCheckInDate(),
                    request.getCheckOutDate()
            );

            System.out.println("\n=== Available Beds Search Results ===");
            System.out.println("Total available beds found: " + availableBeds.size());
            for (Bed bed : availableBeds) {
                System.out.println("Bed ID: " + bed.getId() + 
                    ", No: " + bed.getBedNo() + 
                    ", Room: " + bed.getRoom().getRoomNo() + 
                    ", Type: " + bed.getRoom().getRoomType() +
                    ", Status: " + bed.getStatus());
            }

            if (availableBeds.size() < request.getNumberOfBeds()) {
                String errorMsg = String.format("Not enough beds available. Required: %d, Available: %d",
                    request.getNumberOfBeds(),
                    availableBeds.size());
                System.err.println(errorMsg);
                throw new IllegalStateException(errorMsg);
            }

            // Create a booking for each bed
            List<Booking> bookings = new ArrayList<>();
            for (int i = 0; i < request.getNumberOfBeds(); i++) {
                Bed selectedBed = availableBeds.get(i);
                System.out.println("\nCreating booking for bed: " + selectedBed.getBedNo());

                Booking booking = new Booking();
                booking.setUser(user);
                booking.setBed(selectedBed);
                booking.setBookingDate(request.getCheckInDate());
                booking.setDurationDays((int) durationDays);
                booking.setPurpose(request.getPurpose());
                booking.setCreatedAt(LocalDateTime.now());
                booking.setStatus(Booking.Status.APPROVED); // Admin bookings are auto-approved
                
                int guestsPerBed = request.getNumberOfGuests() / request.getNumberOfBeds();
                if (i < request.getNumberOfGuests() % request.getNumberOfBeds()) {
                    guestsPerBed++; // Distribute any remaining guests
                }
                booking.setNumberOfGuests(guestsPerBed);

                selectedBed.setStatus(Bed.Status.BOOKED);
                bedRepo.save(selectedBed);
                
                bookings.add(bookingRepo.save(booking));
                System.out.println("Created booking ID: " + booking.getId());
            }

            // Send email notifications
            try {
                for (Booking booking : bookings) {
                    mailService.sendBookingNotification(user.getEmail(), booking);
                }
            } catch (Exception e) {
                System.err.println("Email sending failed for admin-created booking: " + e.getMessage());
            }

            System.out.println("\n=== Admin Booking Creation Completed ===");
        } catch (Exception e) {
            System.err.println("\n=== Error in createBookingAsAdmin ===");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void validateAdminBookingRequest(AdminBookingRequestDTO request) {
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
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and Check-out dates are required.");
        }
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date cannot be before check-in date.");
        }
        if (request.getCheckInDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
    }
}