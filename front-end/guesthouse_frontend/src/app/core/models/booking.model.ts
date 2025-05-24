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