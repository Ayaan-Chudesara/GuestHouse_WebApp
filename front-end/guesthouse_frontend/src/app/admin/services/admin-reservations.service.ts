import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Booking, BookingStatus } from 'src/app/core/models/booking.model';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';
import { User } from 'src/app/core/models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AdminReservationsService {
 // ⭐⭐ CRITICAL: Ensure this base URL matches your AdminController's @RequestMapping ⭐⭐
  // Example: @RequestMapping("/api/admin") on your AdminController
  private adminApiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) { }

  // --- Booking Endpoints ---

  getAllBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.adminApiUrl}/allBookings`);
  }

  getAllPendingBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.adminApiUrl}/allPendingBookings`);
  }

  getFilteredBookings(filters: {
    guestHouseId?: number;
    roomType?: string;
    checkInDate?: string;
    checkOutDate?: string;
    status?: BookingStatus;
  }): Observable<Booking[]> {
    let params = new HttpParams();
    if (filters.guestHouseId) {
      params = params.append('guestHouseId', filters.guestHouseId.toString());
    }
    if (filters.roomType) {
      params = params.append('roomType', filters.roomType);
    }
    if (filters.checkInDate) {
      params = params.append('checkInDate', filters.checkInDate);
    }
    if (filters.checkOutDate) {
      params = params.append('checkOutDate', filters.checkOutDate);
    }
    if (filters.status) {
      params = params.append('status', filters.status);
    }
    return this.http.get<Booking[]>(`${this.adminApiUrl}/bookings/filter`, { params: params });
  }

  approveBooking(bookingId: number): Observable<string> {
    // Backend returns a String message for this endpoint
    return this.http.post(`${this.adminApiUrl}/bookings/${bookingId}/approve`, {}, { responseType: 'text' });
  }

  rejectBooking(bookingId: number): Observable<string> {
    // Backend returns a String message for this endpoint
    return this.http.post(`${this.adminApiUrl}/bookings/${bookingId}/reject`, {}, { responseType: 'text' });
  }

  updateBookingStatusGeneric(bookingId: number, status: BookingStatus): Observable<Booking> {
    // Backend returns BookingDTO for this endpoint
    return this.http.post<Booking>(`${this.adminApiUrl}/bookings/${bookingId}/updateStatus/${status}`, {});
  }

  deleteBooking(bookingId: number): Observable<void> {
    // Backend returns 204 No Content for successful deletion
    return this.http.delete<void>(`${this.adminApiUrl}/bookings/${bookingId}`);
  }

  // --- User Endpoints (from your AdminController) ---

  getAllUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.adminApiUrl}/users/all`);
  }

  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.adminApiUrl}/users/${id}`);
  }

  deleteUserById(id: number): Observable<void> {
    return this.http.delete<void>(`${this.adminApiUrl}/users/${id}`);
  }

  // Add more user-related methods as needed (save, update)

  // --- Filter Option Endpoints ---

  getGuestHousesForFilter(): Observable<GuestHouse[]> {
    return this.http.get<GuestHouse[]>(`${this.adminApiUrl}/guesthouses-for-filter`);
  }

  getRoomTypesForFilter(): Observable<string[]> {
    return this.http.get<string[]>(`${this.adminApiUrl}/rooms/types-for-filter`);
  }
}
