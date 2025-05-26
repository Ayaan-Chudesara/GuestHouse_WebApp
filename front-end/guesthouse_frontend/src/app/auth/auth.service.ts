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
    return localStorage.getItem('jwt_token');
  }

  public getDecodedToken(): any | null {
    const token = this.getToken();
    if (!token) {
      return null;
    }
    try {
      return jwtDecode(token);
    } catch (error) {
      console.error("Error decoding token:", error);
      return null;
    }
  }

  // Modified getRoleFromToken to use the new getDecodedToken
  getRoleFromToken(token: string): string | null {
    const decoded = this.getDecodedToken();
    if (decoded && decoded.role) {
      return decoded.role;
    }
    return null;
  }

  // --- NEW METHOD TO GET USER ID FROM TOKEN (ONLY NECESSARY CHANGE) ---
  public getUserIdFromToken(): number | null {
    const decoded = this.getDecodedToken();
    if (decoded && decoded.userId) { // Assuming 'userId' is the claim name in your JWT
      return decoded.userId;
    }
    console.warn("JWT token does not contain a 'userId' claim.");
    return null;
  }
  // --- END NEW METHOD ---

  public isTokenExpired(token: string): boolean {
    if (!token) {
      return true;
    }
    try {
      const decoded: any = jwtDecode(token);
      if (decoded.exp) {
        const expirationDate = new Date(0);
        expirationDate.setUTCSeconds(decoded.exp);
        return expirationDate.valueOf() < new Date().valueOf();
      }
      return false;
    } catch (error) {
      console.error("Error checking token expiration:", error);
      return true;
    }
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, userData , { responseType: 'text' });
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    console.log('User logged out. Token removed from localStorage.');
  }
}