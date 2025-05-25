import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Room } from 'src/app/core/models/room.model';

@Injectable({
  providedIn: 'root'
})
export class RoomService {
 private apiUrl = 'http://localhost:8080/api/rooms'; // Adjust if your Spring Boot app runs on a different port or path

  constructor(private http: HttpClient) { }

  /**
   * Fetches all rooms from the backend.
   * @returns An Observable of a list of Room objects.
   */
  getAllRooms(): Observable<Room[]> {
    return this.http.get<Room[]>(this.apiUrl);
  }

  /**
   * Fetches a single room by its ID.
   * @param id The ID of the room.
   * @returns An Observable of a single Room object.
   */
  getRoomById(id: number): Observable<Room> {
    return this.http.get<Room>(`${this.apiUrl}/${id}`);
  }

  /**
   * Creates a new room.
   * @param room The Room object to create.
   * @returns An Observable of the created Room object.
   */
  createRoom(room: Room): Observable<Room> {
    return this.http.post<Room>(this.apiUrl, room);
  }

  /**
   * Updates an existing room.
   * @param id The ID of the room to update.
   * @param room The updated Room object.
   * @returns An Observable of the updated Room object.
   */
  updateRoom(id: number, room: Room): Observable<Room> {
    return this.http.put<Room>(`${this.apiUrl}/${id}`, room);
  }

  /**
   * Deletes a room by its ID.
   * @param id The ID of the room to delete.
   * @returns An Observable that completes when the deletion is successful.
   */
  deleteRoom(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
