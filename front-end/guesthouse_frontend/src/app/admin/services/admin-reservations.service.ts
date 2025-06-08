import { HttpClient, HttpParams, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Booking, BookingStatus } from 'src/app/core/models/booking.model';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';
import { User } from 'src/app/core/models/user.model';
import { AuthService } from 'src/app/auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AdminReservationsService {
  // Example: @RequestMapping("/api/admin") on your AdminController
  private readonly adminApiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient, private authService: AuthService) { } 

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders().set('Authorization', `Bearer ${token}`);
  }

  private handleError(error: HttpErrorResponse) {
    console.error('An error occurred:', error);

    if (error.status === 403) {
      return throwError(() => new Error('You do not have permission to access this resource'));
    }

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      return throwError(() => new Error('A client-side error occurred. Please try again.'));
    } else {
      // Server-side error
      const message = error.error?.message || error.message || 'An error occurred. Please try again later.';
      return throwError(() => new Error(message));
    }
  }

  // --- Booking Endpoints ---

  getAllBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.adminApiUrl}/allBookings`, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }

  getAllPendingBookings(): Observable<Booking[]> {
    return this.http.get<Booking[]>(`${this.adminApiUrl}/allPendingBookings`, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
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
      params = params.set('guestHouseId', filters.guestHouseId.toString());
    }
    if (filters.roomType) {
      params = params.set('roomType', filters.roomType);
    }
    if (filters.checkInDate) {
      params = params.set('checkInDate', filters.checkInDate);
    }
    if (filters.checkOutDate) {
      params = params.set('checkOutDate', filters.checkOutDate);
    }
    if (filters.status) {
      params = params.set('status', filters.status);
    }

    return this.http.get<Booking[]>(`${this.adminApiUrl}/bookings/filter`, {
      headers: this.getAuthHeaders(),
      params: params
    }).pipe(catchError(this.handleError));
  }

  approveBooking(bookingId: number): Observable<string> {
    // Backend returns a String message for this endpoint
    return this.http.post<string>(`${this.adminApiUrl}/bookings/${bookingId}/approve`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }

  rejectBooking(bookingId: number): Observable<string> {
    // Backend returns a String message for this endpoint
    return this.http.post<string>(`${this.adminApiUrl}/bookings/${bookingId}/reject`, {}, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }

  updateBookingStatusGeneric(bookingId: number, newStatus: BookingStatus): Observable<Booking> {
    // Backend returns BookingDTO for this endpoint
    return this.http.put<Booking>(`${this.adminApiUrl}/bookings/${bookingId}/status`, { status: newStatus }, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }

  deleteBooking(bookingId: number): Observable<void> {
    // Backend returns 204 No Content for successful deletion
    return this.http.delete<void>(`${this.adminApiUrl}/bookings/${bookingId}`, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
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
    return this.http.get<GuestHouse[]>(`${this.adminApiUrl}/guesthouses-for-filter`, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }

  getRoomTypesForFilter(): Observable<string[]> {
    return this.http.get<string[]>(`${this.adminApiUrl}/rooms/types-for-filter`, {
      headers: this.getAuthHeaders()
    }).pipe(catchError(this.handleError));
  }
}
