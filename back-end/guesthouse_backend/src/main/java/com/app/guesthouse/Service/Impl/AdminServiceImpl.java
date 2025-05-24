package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.DTO.BookingDTO;
import com.app.guesthouse.DTO.UserDTO;
import com.app.guesthouse.Entity.Bed;
import com.app.guesthouse.Entity.Booking;
import com.app.guesthouse.Entity.User;
import com.app.guesthouse.Repository.BedRepo;
import com.app.guesthouse.Repository.BookingRepo;
import com.app.guesthouse.Repository.UserRepo;
import com.app.guesthouse.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {
    private final UserRepo userRepo;
    private final BookingRepo bookingRepo;
    @Autowired
    private BedRepo bedRepo;


    AdminServiceImpl(UserRepo userRepo,BookingRepo bookingRepo){
        this.userRepo = userRepo;
        this.bookingRepo = bookingRepo;
    }

    private BookingDTO mapToDTO(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setUser(booking.getUser()); // Full User entity
        dto.setBed(booking.getBed());   // Full Bed entity
        dto.setBookingDate(booking.getBookingDate());
        dto.setDurationDays(booking.getDurationDays());
        dto.setPurpose(booking.getPurpose());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }



    public List<BookingDTO> getAllBookings(){
        List<Booking> bookingList = bookingRepo.findAll();
        List<BookingDTO> bookings = new ArrayList<>();

        for(Booking b  : bookingList){
            BookingDTO dto = this.mapToDTO(b);
            bookings.add(dto);
        }
        return bookings;

    }

    public BookingDTO approveBooking(Long id){
        Booking booking = bookingRepo.findById(id).orElseThrow(()->new RuntimeException("Booking not found"));
        booking.setStatus(Booking.Status.APPROVED);
        return this.mapToDTO(bookingRepo.save(booking));
    }

    public BookingDTO rejectBooking(Long id){
        Booking booking = bookingRepo.findById(id).orElseThrow(()-> new RuntimeException("Booking not found"));
        booking.setStatus((Booking.Status.REJECTED));
        return  this.mapToDTO(bookingRepo.save(booking));
    }

    public List<BookingDTO> getPendingBookings(){
        List<Booking> pendingBookings = bookingRepo.findByStatus(Booking.Status.PENDING);
        List<BookingDTO> bookings = new ArrayList<>();

        for(Booking b : pendingBookings){
            BookingDTO dto = this.mapToDTO(b);
            bookings.add(dto);
        }
        return bookings;
    }

    public BookingDTO updateBookingStatus(Long bookingId, String status) {
        Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus(Booking.Status.valueOf(status));
        return this.mapToDTO(bookingRepo.save(booking));
    }


    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("availableRooms", bedRepo.countByStatus(Bed.Status.AVAILABLE));
        stats.put("reservations", bookingRepo.count());
        stats.put("pendingRequests", bookingRepo.countByStatus(Booking.Status.PENDING));
        stats.put("checkIns", bookingRepo.countByStatus(Booking.Status.CHECKED_IN));
        stats.put("checkOuts", bookingRepo.countByStatus(Booking.Status.CHECKED_OUT));

        return stats;
    }

    @Override
    public List<BookingDTO> getSchedulerData(String start, String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        List<Booking> bookings = bookingRepo.findByBookingDateBetween(startDate, endDate);

        return bookings.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public Integer getTotalBeds() {
        return (int) bedRepo.count();
    }

}
