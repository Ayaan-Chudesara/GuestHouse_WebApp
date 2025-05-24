import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { jwtDecode } from 'jwt-decode';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
 private baseUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, { email, password });
  }

    saveToken(token: string) {
    localStorage.setItem('jwt_token', token);
  }
  
  getToken(): string | null {
    return localStorage.getItem('jwt_token'); // <--- This must be 'jwt_token'
  }
  getRoleFromToken(token: string): string {
    const decoded: any = jwtDecode(token);
    return decoded.role;
  }

    register(userData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, userData ,  { responseType: 'text' });
  }

   logout(): void {
    localStorage.removeItem('jwt_token'); // <--- This must be 'jwt_token'
    console.log('User logged out. Token removed from localStorage.');
  }
}
