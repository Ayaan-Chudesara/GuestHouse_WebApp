import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from 'src/app/auth/auth.service';
import { GuesthouseService } from './guesthouse.service';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';

@Injectable({
  providedIn: 'root'
})
export class AdminPanelService {
  // This baseUrl is specifically for AdminController endpoints that are NOT under /dashboard
  // and for the /dashboard ones that are also needed for the admin panel.
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
    private guestHouseService: GuesthouseService
  ) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  // Method to create a booking using the AdminBookingRequestDTO
  // Maps to: POST /api/admin/bookings/create-by-admin
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
    const headers = this.getAuthHeaders();
    // Format dates to match LocalDate format (YYYY-MM-DD)
    const formattedRequest = {
      ...requestData,
      checkInDate: requestData.checkInDate.split('T')[0],
      checkOutDate: requestData.checkOutDate.split('T')[0]
    };
    return this.http.post(`${this.apiUrl}/bookings/create-by-admin`, formattedRequest, { headers, responseType: 'text' });
  }

  // Method to get all users (for possible lookup or future features)
  // Maps to: GET /api/admin/all
  getAllUsers(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/users/all`, { headers });
  }

  // Method to get all bookings (e.g., for a list on the admin panel)
  // Maps to: GET /api/admin/allBookings
  getAllBookings(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/allBookings`, { headers });
  }

  // --- Dashboard stats needed for AdminBookingComponent's overview ---
  // Maps to: GET /api/admin/dashboard/stats
  getDashboardStats(): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.get<any>(`${this.apiUrl}/dashboard/stats`, { headers });
  }

  // Maps to: GET /api/admin/dashboard/total-beds
  getTotalBeds(): Observable<number> {
    const headers = this.getAuthHeaders();
    return this.http.get<number>(`${this.apiUrl}/dashboard/total-beds`, { headers });
  }

  // Maps to: GET /api/admin/dashboard/scheduler
  getSchedulerData(start: string, end: string): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/dashboard/scheduler`, {
      headers,
      params: { start, end }
    });
  }

  // Placeholder for Guest Houses - still no direct backend API for this provided
  getGuestHouses(): Observable<GuestHouse[]> {
    return this.guestHouseService.getAllGuestHouses();
  }
}
