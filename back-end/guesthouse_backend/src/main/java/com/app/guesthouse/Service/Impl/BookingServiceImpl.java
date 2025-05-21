package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.BookingRepo;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private BedRepo bedRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MailServiceImpl mailService;


    public BookingDTO createBooking(BookingDTO bookingDTO) {
        Optional<Bed> bedOpt = bedRepo.findById(bookingDTO.getBed().getId());
        Optional<User> userOpt = userRepo.findById(bookingDTO.getUser().getId());

        if (bedOpt.isEmpty() || userOpt.isEmpty()) {
            return null; // Or throw custom exception or error response
        }

        Booking booking = new Booking();
        booking.setUser(userOpt.get());
        booking.setBed(bedOpt.get());
        booking.setBookingDate(bookingDTO.getBookingDate());
        booking.setDurationDays(bookingDTO.getDurationDays());
        booking.setPurpose(bookingDTO.getPurpose());
        booking.setStatus(Booking.Status.PENDING);

        Booking saved = bookingRepo.save(booking);

        // 📨 Send Email Notification
        try {
            mailService.sendBookingNotification(saved.getUser().getEmail(), saved);
        } catch (Exception e) {
            System.out.println("Email sending failed: " + e.getMessage());
        }

        // Populate back to DTO
        bookingDTO.setId(saved.getId());
        bookingDTO.setCreatedAt(saved.getCreatedAt());
        bookingDTO.setStatus(saved.getStatus());

        return bookingDTO;
    }


    public List<BookingDTO> getAllBookings() {
        return bookingRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public BookingDTO getBookingById(Long id) {
        return bookingRepo.findById(id).map(this::convertToDTO).orElse(null);
    }

    public void deleteBooking(Long id) {
        bookingRepo.deleteById(id);
    }

    // Filter bookings by user ID
    public List<BookingDTO> getBookingsByUser(Long userId) {
        List<Booking> bookings = bookingRepo.findByUserId(userId);
        return bookings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Update booking status (e.g., approve, cancel)
    public BookingDTO updateBookingStatus(Long bookingId, Booking.Status status) {
        Optional<Booking> optional = bookingRepo.findById(bookingId);
        if (optional.isPresent()) {
            Booking booking = optional.get();
            booking.setStatus(status);
            bookingRepo.save(booking);
            return convertToDTO(booking);
        }
        return null;
    }

    public void updateStatus(Long bookingId, Booking.Status status) {
        Booking booking = bookingRepo.findById(bookingId).orElse(null);
        if (booking != null) {
            booking.setStatus(status);
            bookingRepo.save(booking);
        }
    }


    public List<BookingDTO> getBookingsBetweenDates(LocalDate start, LocalDate end) {
        List<Booking> bookings = bookingRepo.findByBookingDateBetween(start, end);
        return bookings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }


    private BookingDTO convertToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUser(booking.getUser());
        dto.setBed(booking.getBed());
        dto.setBookingDate(booking.getBookingDate());
        dto.setDurationDays(booking.getDurationDays());
        dto.setPurpose(booking.getPurpose());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }
}
