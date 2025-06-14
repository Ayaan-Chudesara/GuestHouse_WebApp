import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs'; // <--- Import BehaviorSubject here
import { jwtDecode } from 'jwt-decode';
import { Router } from '@angular/router'; // <--- Import Router for potential use (e.g., redirect on bad token)

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/api/auth';

  // --- NEW: BehaviorSubject to hold and emit the current user's role ---
  private _userRole = new BehaviorSubject<string | null>(null);
  // Public observable for components to subscribe to
  userRole$: Observable<string | null> = this._userRole.asObservable();
  // --- END NEW ---

  constructor(private http: HttpClient, private router: Router) { // <--- Inject Router
    // --- NEW: Initialize role on service creation (e.g., on app start) ---
    this.initializeUserRole();
  }

  private initializeUserRole(): void {
    const token = this.getToken();
    if (token && !this.isTokenExpired(token)) {
      const role = this.getRoleFromToken(); // Get role from current token
      this._userRole.next(role); // Set initial role
    } else {
      this._userRole.next(null); // No valid token, set role to null
      // Optional: If token is expired, clear it
      if (token && this.isTokenExpired(token)) {
        this.logout();
      }
    }
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/login`, { email, password });
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<any> {
    return this.http.post(`${this.baseUrl}/reset-password`, { token, newPassword });
  }

  saveToken(token: string) {
    localStorage.setItem('jwt_token', token);
    // --- NEW: Update user role whenever a new token is saved ---
    const role = this.getRoleFromToken();
    this._userRole.next(role);
    // --- END NEW ---
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
      // Optional: If token is invalid, remove it and log out
      this.logout();
      return null;
    }
  }

  // Modified getRoleFromToken to not take a token as an argument,
  // it will internally get the token using this.getToken()
  getRoleFromToken(): string | null { // <--- Removed 'token: string' argument
    const decoded = this.getDecodedToken();
    if (decoded && decoded.role) { // Assuming 'role' is the claim name in your JWT
      return decoded.role.toUpperCase(); // Always return role in uppercase
    }
    return null;
  }

  public getUserIdFromToken(): number | null {
    const decoded = this.getDecodedToken();
    if (decoded) {
      // Check for both possible claim names
      const userId = decoded.userId || decoded.sub || decoded.id;
      if (userId) {
        // Ensure it's a number
        const numericId = Number(userId);
        if (!isNaN(numericId)) {
          return numericId;
        }
        console.warn("User ID from token is not a valid number:", userId);
        return null;
      }
    }
    console.warn("JWT token does not contain a valid user ID claim.");
    return null;
  }

  public isTokenExpired(token: string): boolean {
    if (!token) {
      return true;
    }
    try {
      const decoded: any = jwtDecode(token);
      if (decoded.exp) {
        const expirationDate = new Date(0);
        expirationDate.setUTCSeconds(decoded.exp);
        // Add a 5-minute buffer to handle clock skew
        const now = new Date();
        const fiveMinutesFromNow = new Date(now.getTime() + 5 * 60 * 1000);
        return expirationDate.valueOf() < fiveMinutesFromNow.valueOf();
      }
      return false;
    } catch (error) {
      console.error("Error checking token expiration:", error);
      return true; // Assume expired if decoding fails
    }
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/register`, userData, { responseType: 'text' });
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    // --- NEW: Emit null role on logout ---
    this._userRole.next(null);
    // --- END NEW ---
    console.log('User logged out. Token removed from localStorage.');
    this.router.navigate(['/auth/login']);
  }

  // --- NEW: Helper method to check if user is logged in (optional but useful) ---
  isLoggedIn(): boolean {
    const token = this.getToken();
    return token !== null && !this.isTokenExpired(token);
  }

  // --- NEW: Helper method to check if current user is admin ---
  isAdminUser(): boolean {
    const role = this.getRoleFromToken();
    return role === 'ADMIN';
  }

  // --- NEW: Helper method to check if current user is a regular user ---
  isRegularUser(): boolean {
    const role = this.getRoleFromToken();
    return role === 'USER';
  }

  hasAdminAccess(): boolean {
    const token = this.getToken();
    if (!token || this.isTokenExpired(token)) {
      return false;
    }
    return this.isAdminUser();
  }

  getCurrentUserRole(): string | null {
    return this._userRole.getValue();
  }

  private getRoleWithPrefix(role: string): string {
    return `ROLE_${role}`;
  }
}