// src/app/core/models/booking.model.ts

import { BedStatus } from './bed.model'; // We'll create this next
import { UserRole } from './user.model'; // We'll create this next

export enum BookingStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  CHECKED_IN = 'CHECKED_IN',
  CANCELLED = 'CANCELLED',
  CHECKED_OUT = 'CHECKED_OUT'
}

/**
 * Interface for the data sent FROM the Angular frontend TO the Spring Boot backend
 * when creating a new booking.
 * This should contain only the input fields necessary for the backend to process the booking.
 */
export interface BookingRequest {
  roomId: number;       // The ID of the room the user selected
  userId: number;       // The ID of the currently logged-in user
  checkInDate: string;  // Check-in date in 'YYYY-MM-DD' string format
  checkOutDate: string; // Check-out date in 'YYYY-MM-DD' string format
  numberOfGuests: number; // The number of guests for this booking
  purpose: string;      // The purpose of the booking
}

export interface Booking {
  id: number;
  bookingDate: string;     // LocalDate (YYYY-MM-DD)
  durationDays: number;
  purpose: string;
  status: BookingStatus;
  createdAt: string;       // LocalDateTime (ISO 8601 string)

  userId: number;
  guestName: string;
  guestEmail: string;

  bedId: number;
  bedNo: string;

  roomId: number;
  roomNo: string;
  roomType: string;        // Assuming this is a string
  numberOfBeds: number;

  guestHouseId: number;
  guestHouseName: string;
  guestHouseLocation: string;

  checkInDate: string;
  checkOutDate: string;
}