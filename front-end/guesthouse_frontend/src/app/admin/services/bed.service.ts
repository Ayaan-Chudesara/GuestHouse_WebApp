import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Bed } from 'src/app/core/models/bed.model';

@Injectable({
  providedIn: 'root'
})
export class BedService {
 private apiUrl = 'http://localhost:8080/api/beds'; // Adjust if your Spring Boot app runs on a different port or path

  constructor(private http: HttpClient) { }

  /**
   * Fetches all beds from the backend.
   * @returns An Observable of a list of Bed objects.
   */
  getAllBeds(): Observable<Bed[]> {
    return this.http.get<Bed[]>(this.apiUrl);
  }

  /**
   * Fetches a single bed by its ID.
   * @param id The ID of the bed.
   * @returns An Observable of a single Bed object.
   */
  getBedById(id: number): Observable<Bed> {
    return this.http.get<Bed>(`${this.apiUrl}/${id}`);
  }

  /**
   * Creates a new bed.
   * @param bed The Bed object to create.
   * @returns An Observable of the created Bed object.
   */
  createBed(bed: Bed): Observable<Bed> {
    return this.http.post<Bed>(this.apiUrl, bed);
  }

  /**
   * Updates an existing bed.
   * @param id The ID of the bed to update.
   * @param bed The updated Bed object.
   * @returns An Observable of the updated Bed object.
   */
  updateBed(id: number, bed: Bed): Observable<Bed> {
    return this.http.put<Bed>(`${this.apiUrl}/${id}`, bed);
  }

  /**
   * Deletes a bed by its ID.
   * @param id The ID of the bed to delete.
   * @returns An Observable of a string indicating success (matching your backend).
   */
  deleteBed(id: number): Observable<string> {
    return this.http.delete<string>(`${this.apiUrl}/${id}`, { responseType: 'text' as 'json' }); // Backend returns string
  }

  /**
   * Fetches all beds for a specific room ID.
   * @param roomId The ID of the room.
   * @returns An Observable of a list of Bed objects for the given room.
   */
  getBedsByRoomId(roomId: number): Observable<Bed[]> {
    return this.http.get<Bed[]>(`${this.apiUrl}/room/${roomId}`);
  }
}
