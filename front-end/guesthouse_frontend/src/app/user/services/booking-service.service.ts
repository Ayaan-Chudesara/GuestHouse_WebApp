import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthService } from 'src/app/auth/auth.service';
import { Booking, BookingRequest } from 'src/app/core/models/booking.model';
import { Room } from 'src/app/core/models/room.model';

@Injectable({
  providedIn: 'root'
})
export class BookingServiceService {
  private apiUrl = 'http://localhost:8080/api/bookings';
  private roomApiUrl = 'http://localhost:8080/api/rooms';
  private userApiUrl = 'http://localhost:8080/api/users';

  constructor(private http: HttpClient, private authService: AuthService) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  /**
   * Creates a new booking.
   * @param bookingRequest The booking data from the frontend form.
   * @returns An Observable of the created Booking object.
   */
  createBooking(bookingRequest: BookingRequest): Observable<Booking> {
    const headers = this.getAuthHeaders();
    return this.http.post<Booking>(this.apiUrl, bookingRequest, { headers });
  }

  /**
   * Fetches all available room types.
   * @returns An Observable of an array of room type strings.
   */
  getRoomTypes(): Observable<string[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<string[]>(`${this.roomApiUrl}/types`, { headers });
  }

  /**
   * Fetches all rooms.
   * IMPORTANT: As discussed, for better performance, you should consider
   * adding parameters to this method (e.g., guestHouseId, roomType, dates)
   * and create a specific backend endpoint for searching available rooms.
   * @returns An Observable of an array of Room objects.
   */
  getAllRooms(): Observable<Room[]> {
    const headers = this.getAuthHeaders();
    console.log('Fetching rooms with headers:', headers);
    return this.http.get<Room[]>(this.roomApiUrl, { headers });
  }

  /**
   * Fetches bookings for a specific user.
   * @param userId The ID of the user whose bookings are to be fetched.
   * @returns An Observable of an array of Booking objects.
   */
  getBookingsByUser(userId: number): Observable<Booking[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<Booking[]>(`${this.apiUrl}/user/${userId}`, { headers });
  }

  /**
   * Fetches all bookings (might be used by admin).
   * @returns An Observable of an array of Booking objects.
   */
  getAllBookings(): Observable<Booking[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<Booking[]>(this.apiUrl, { headers });
  }

  /**
   * Updates the status of a booking (might be used by admin).
   * @param bookingId The ID of the booking to update.
   * @param status The new status.
   * @returns An Observable of the updated Booking object.
   */
  updateBookingStatus(bookingId: number, status: string): Observable<Booking> {
    const headers = this.getAuthHeaders();
    // Assuming backend endpoint for status update is PUT /api/bookings/{id}/status
    return this.http.put<Booking>(`${this.apiUrl}/${bookingId}/status`, { status }, { headers });
  }

  /**
   * Deletes a booking.
   * @param bookingId The ID of the booking to delete.
   * @returns An Observable of void (or any type if backend returns data).
   */
  deleteBooking(bookingId: number): Observable<void> {
    const headers = this.getAuthHeaders();
    return this.http.delete<void>(`${this.apiUrl}/${bookingId}`, { headers });
  }

  /**
   * Verifies if a user exists in the database
   * @param userId The ID of the user to verify
   * @returns An Observable of boolean indicating if the user exists
   */
  verifyUser(userId: number): Observable<boolean> {
    const headers = this.getAuthHeaders();
    return this.http.get<boolean>(`${this.userApiUrl}/${userId}/verify`, { headers });
  }

  /**
   * Searches for available rooms based on the given criteria.
   * @param params Search parameters including dates, guest house, room type, and number of guests
   * @returns An Observable of an array of available Room objects
   */
  searchAvailableRooms(params: {
    checkInDate: string,
    checkOutDate: string,
    guestHouseId?: number,
    roomType?: string,
    numberOfGuests: number
  }): Observable<Room[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<Room[]>(`${this.roomApiUrl}/available`, { headers, params });
  }

  // You might need more methods depending on your backend API, e.g.:
  // getBookingDetails(id: number): Observable<Booking>;
}
