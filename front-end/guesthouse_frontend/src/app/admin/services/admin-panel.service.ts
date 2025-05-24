import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class AdminPanelService {
 // This baseUrl is specifically for AdminController endpoints that are NOT under /dashboard
  // and for the /dashboard ones that are also needed for the admin panel.
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) { }

  // Method to create a booking using the AdminBookingRequestDTO
  // Maps to: POST /api/admin/bookings
  createBooking(requestData: {
    guestName: string;
    guestEmail: string;
    checkInDate: string;
    checkOutDate: string;
    guestHouseId: number;
    roomType: string;
    numberOfBeds: number;
    purpose: string;
  }): Observable<string> { // Backend returns a String "Booking created successfully"
    return this.http.post<string>(`${this.apiUrl}/bookings`, requestData);
  }

  // Method to get all users (for possible lookup or future features)
  // Maps to: GET /api/admin/all
  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/all`);
  }

  // Method to get all bookings (e.g., for a list on the admin panel)
  // Maps to: GET /api/admin/allBookings
  getAllBookings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/allBookings`);
  }

  // --- Dashboard stats needed for AdminBookingComponent's overview ---
  // Maps to: GET /api/admin/dashboard/stats
  getDashboardStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/dashboard/stats`);
  }

  // Maps to: GET /api/admin/dashboard/total-beds
  getTotalBeds(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/dashboard/total-beds`);
  }

  // Maps to: GET /api/admin/dashboard/scheduler
  getSchedulerData(start: string, end: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/dashboard/scheduler`, {
      params: { start, end }
    });
  }

  // Placeholder for Guest Houses - still no direct backend API for this provided
  getGuestHouses(): Observable<any[]> {
    console.warn("Using mock data for Guest Houses. Backend API for /api/guesthouses not found.");
    return new Observable(observer => {
      observer.next([
        { id: 1, name: 'Main Guest House' },
        { id: 2, name: 'Annex Guest House' },
        { id: 3, name: 'Garden View Suites' }
      ]);
      observer.complete();
    });
  }
}
