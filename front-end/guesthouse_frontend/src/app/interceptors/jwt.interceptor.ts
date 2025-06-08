// src/app/interceptors/jwt.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private router: Router, private authService: AuthService) {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = localStorage.getItem('jwt_token');
    const isApiRequest = request.url.startsWith('http://localhost:8080/api/');
    const isAuthRequest = request.url.includes('/api/auth/');

    // Only add token for API requests that are not auth requests
    if (token && isApiRequest && !isAuthRequest) {
      // Check if token is expired before making the request
      if (this.authService.isTokenExpired(token)) {
        console.log('JWT Interceptor: Token is expired, redirecting to login');
        this.authService.logout();
        this.router.navigate(['/auth/login']);
        return throwError(() => new Error('Token expired'));
      }

      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('JWT Interceptor: Token attached to request', {
        url: request.url,
        method: request.method,
        headers: request.headers.has('Authorization') ? 'Authorization header present' : 'No Authorization header'
      });
    } else {
      console.log('JWT Interceptor: No token attached for request:', {
        url: request.url,
        method: request.method,
        tokenExists: !!token,
        isApiRequest,
        isAuthRequest
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          // Unauthorized - token is invalid or expired
          console.log('JWT Interceptor: Unauthorized error (401)');
          this.authService.logout();
          this.router.navigate(['/auth/login']);
          return throwError(() => new Error('Session expired. Please log in again.'));
        } else if (error.status === 403) {
          // Forbidden - user doesn't have required permissions
          console.log('JWT Interceptor: Forbidden error (403)');
          return throwError(() => new Error('Insufficient permissions'));
        }
        
        // For other errors, just pass them through
        return throwError(() => error);
      })
    );
  }
}