package com.app.guesthouse.Service.Impl;

import com.app.guesthouse.Entity.Booking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingNotification(String to, Booking booking) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Booking Confirmation - Guest House");

        message.setText(
                "Dear " + booking.getUser().getName() + ",\n\n" +
                        "Your booking has been received successfully.\n\n" +
                        "Booking Details:\n" +
                        "Booking ID: " + booking.getId() + "\n" +
                        "Bed No: " + booking.getBed().getBedNo() + "\n" +
                        "Room No: " + booking.getBed().getRoom().getRoomNo() + "\n" +
                        "Booking Date: " + booking.getBookingDate() + "\n" +
                        "Duration: " + booking.getDurationDays() + " day(s)\n\n" +
                        "Status: " + booking.getStatus() + "\n\n" +
                        "Thank you for using our service.\nGuest House Team");

        mailSender.send(message);
    }
}
