import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
private baseUrl = 'http://localhost:8080/api/admin/dashboard'; // adjust if needed

  constructor(private http: HttpClient) {}

  getDashboardStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/stats`);
  }

  getSchedulerData(startDate: string, endDate: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/scheduler?start=${startDate}&end=${endDate}`);
  }

  getTotalBeds(start?: string, end?: string): Observable<number> {
  let url = `${this.baseUrl}/total-beds`;
  if (start && end) {
    url += `?start=${start}&end=${end}`;
  }
  return this.http.get<number>(url);
}

}
