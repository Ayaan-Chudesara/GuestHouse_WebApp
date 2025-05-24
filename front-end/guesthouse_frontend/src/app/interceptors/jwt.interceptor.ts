// src/app/interceptors/jwt.interceptor.ts
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor() {}

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const token = localStorage.getItem('jwt_token'); // Or 'token', depending on your AuthService.saveToken

    // Define the login URL to explicitly exclude it from token addition
    const loginUrl = 'http://localhost:8080/api/auth/login'; // Make sure this matches your login endpoint

    // Check if a token exists AND if the request is NOT the login request AND if it's to your backend API
    if (token && request.url.startsWith('http://localhost:8080/api/') && !request.url.includes(loginUrl)) {
      // Clone the request and add the Authorization header
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      console.log('JWT Interceptor: Token attached to request', request.url);
    } else {
      // This will log for the login request, or any request without a token, or non-backend requests
      console.log('JWT Interceptor: No token attached (either no token, not an API request requiring auth, or it\'s the login request)', request.url);
    }

    return next.handle(request);
  }
}