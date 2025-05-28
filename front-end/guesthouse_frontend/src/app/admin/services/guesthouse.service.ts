import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';
import { AuthService } from 'src/app/auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class GuesthouseService {
  private apiUrl = 'http://localhost:8080/api/guesthouses'; // Adjust if your Spring Boot app runs on a different port or path

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
  }

  /**
   * Fetches all guesthouses from the backend.
   * @returns An Observable of a list of GuestHouse objects.
   */
  getAllGuestHouses(): Observable<GuestHouse[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<GuestHouse[]>(this.apiUrl, { headers });
  }

  /**
   * Fetches a single guesthouse by its ID.
   * @param id The ID of the guesthouse.
   * @returns An Observable of a single GuestHouse object.
   */
  getGuestHouseById(id: number): Observable<GuestHouse> {
    const headers = this.getAuthHeaders();
    return this.http.get<GuestHouse>(`${this.apiUrl}/${id}`, { headers });
  }

  /**
   * Creates a new guesthouse.
   * @param guesthouse The GuestHouse object to create.
   * @returns An Observable of the created GuestHouse object.
   */
  createGuestHouse(guesthouse: GuestHouse): Observable<GuestHouse> {
    const headers = this.getAuthHeaders();
    return this.http.post<GuestHouse>(this.apiUrl, guesthouse, { headers });
  }

  /**
   * Updates an existing guesthouse.
   * @param id The ID of the guesthouse to update.
   * @param guesthouse The updated GuestHouse object.
   * @returns An Observable of the updated GuestHouse object.
   */
  updateGuestHouse(id: number, guesthouse: GuestHouse): Observable<GuestHouse> {
    const headers = this.getAuthHeaders();
    return this.http.put<GuestHouse>(`${this.apiUrl}/${id}`, guesthouse, { headers });
  }

  /**
   * Deletes a guesthouse by its ID.
   * @param id The ID of the guesthouse to delete.
   * @returns An Observable of a boolean indicating success.
   */
  deleteGuestHouse(id: number): Observable<boolean> {
    const headers = this.getAuthHeaders();
    return this.http.delete<boolean>(`${this.apiUrl}/${id}`, { headers });
  }
}
