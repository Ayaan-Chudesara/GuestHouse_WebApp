# GuestHouse Management System

A full-stack web application for managing guest house bookings and accommodations. The system provides separate interfaces for users and administrators to handle room bookings, guest management, and facility administration.

## Features

### User Features
- User registration and authentication
- Browse available rooms and guest houses
- Make room bookings with specific dates and guest count
- View booking history and status
- Cancel bookings
- Real-time availability checking

### Admin Features
- Comprehensive dashboard for booking management
- Approve/reject booking requests
- Create bookings on behalf of guests
- Manage rooms and beds
- View occupancy statistics
- User management
- Check-in/check-out functionality

## Technology Stack

### Frontend
- Angular 16
- TypeScript
- Bootstrap for responsive design
- JWT for authentication
- Angular Material components

### Backend
- Spring Boot 3
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Maven for dependency management

## Project Structure

```
├── front-end/
│   └── guesthouse_frontend/    # Angular application
│       ├── src/
│       │   ├── app/
│       │   │   ├── admin/      # Admin module components
│       │   │   ├── user/       # User module components
│       │   │   ├── auth/       # Authentication components
│       │   │   └── core/       # Core functionality
│       │   └── assets/
│       └── package.json
│
└── back-end/
    └── guesthouse_backend/     # Spring Boot application
        ├── src/
        │   └── main/
        │       ├── java/
        │       │   └── com/app/guesthouse/
        │       │       ├── config/
        │       │       ├── controller/
        │       │       ├── entity/
        │       │       ├── repository/
        │       │       └── service/
        │       └── resources/
        └── pom.xml
```

## Room Types and Capacity
- Single Room: 1 bed
- Double Room: 2 beds
- Suite: 3 beds

## Key Features Implementation

### Booking System
- Real-time availability checking
- Automatic bed allocation
- Booking status management (PENDING, APPROVED, REJECTED, CHECKED_IN, CANCELLED, CHECKED_OUT)
- Email notifications for booking updates

### Security
- JWT-based authentication
- Role-based access control (USER, ADMIN)
- Secure password handling
- Protected API endpoints

## Getting Started

### Prerequisites
- Node.js and npm
- Java 17 or higher
- MySQL 8.0 or higher
- Maven

### Backend Setup
1. Navigate to `back-end/guesthouse_backend`
2. Configure MySQL connection in `application.properties`
3. Run `mvn clean install`
4. Start the application using `mvn spring-boot:run`

### Frontend Setup
1. Navigate to `front-end/guesthouse_frontend`
2. Run `npm install`
3. Start the development server using `ng serve`
4. Access the application at `http://localhost:4200`

## API Endpoints

### Public Endpoints
- `POST /api/auth/login`: User authentication
- `POST /api/auth/register`: User registration

### User Endpoints
- `GET /api/bookings`: Get user's bookings
- `POST /api/bookings/create`: Create new booking
- `PUT /api/bookings/{id}/cancel`: Cancel booking

### Admin Endpoints
- `GET /api/admin/allBookings`: Get all bookings
- `POST /api/admin/bookings/create-by-admin`: Create booking as admin
- `PUT /api/admin/bookings/{id}/approve`: Approve booking
- `PUT /api/admin/bookings/{id}/reject`: Reject booking

## Database Schema

The system uses the following main entities:
- User: Stores user information and authentication details
- GuestHouse: Contains guest house information
- Room: Represents individual rooms with type and capacity
- Bed: Individual beds within rooms
- Booking: Manages booking information and status
