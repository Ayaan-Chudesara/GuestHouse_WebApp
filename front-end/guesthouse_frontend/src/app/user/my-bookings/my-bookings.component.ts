import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from 'src/app/auth/auth.service';
import { Booking } from 'src/app/core/models/booking.model';
import { BookingServiceService } from '../services/booking-service.service';

@Component({
  selector: 'app-my-bookings',
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.css']
})
export class MyBookingsComponent {
  // Array to hold the bookings fetched from the backend
  bookings: Booking[] = [];
  // State variables for UI feedback
  isLoading: boolean = true; // True while data is being fetched
  errorMessage: string | null = null; // Stores any error messages
  currentUserId: number | null = null; // Stores the ID of the logged-in user

  // Define the columns that will be displayed in the Angular Material table
  // These strings should match the `matColumnDef` in the HTML template
  displayedColumns: string[] = [
    'bookingId',
    'roomNo',
    'guestHouseName',
    'checkInDate',
    'checkOutDate',
    'numberOfGuests',
    'status'
  ];

  // Subject to manage component unsubscriptions, preventing memory leaks
  private unsubscribe$ = new Subject<void>();

  constructor(
    private bookingService: BookingServiceService, // Inject the booking service
    private authService: AuthService,             // Inject the authentication service
    private router: Router                        // Inject the router for navigation
  ) { }

  ngOnInit(): void {
    // When the component initializes, try to fetch the current user's bookings
    this.getCurrentUserBookings();
  }

  ngOnDestroy(): void {
    // Emit a value to signal all ongoing subscriptions to unsubscribe
    this.unsubscribe$.next();
    // Complete the subject
    this.unsubscribe$.complete();
  }

  /**
   * Attempts to get the current user's ID from the authentication token
   * and then fetches their bookings. Redirects to login if not authenticated.
   */
  getCurrentUserBookings(): void {
    this.isLoading = true; // Start loading state
    this.errorMessage = null; // Clear previous errors

    const token = this.authService.getToken();

    if (token && !this.authService.isTokenExpired(token)) {
      // Get the decoded token and extract the userId
      this.currentUserId = this.authService.getDecodedToken()?.userId;

      if (this.currentUserId === null || this.currentUserId === undefined) {
        // If userId is missing from the token, show an error and redirect
        this.errorMessage = 'Error: User ID not found in token. Please log in again.';
        this.isLoading = false;
        this.router.navigate(['/auth/login']);
        return;
      }

      // If userId is found, proceed to fetch bookings
      console.log("Fetching bookings for user ID:", this.currentUserId);
      this.fetchBookings(this.currentUserId);

    } else {
      // If no token or token is expired, show message and redirect to login
      this.errorMessage = 'You must be logged in to view your bookings. Redirecting to login...';
      this.isLoading = false;
      // Use setTimeout to allow the message to display briefly before redirecting
      setTimeout(() => {
        this.router.navigate(['/auth/login']);
      }, 2000); // Redirect after 2 seconds
    }
  }

  /**
   * Calls the BookingServiceService to fetch bookings for a given user ID.
   * @param userId The ID of the user whose bookings are to be fetched.
   */
  fetchBookings(userId: number): void {
    // Subscribe to the observable returned by the booking service
    this.bookingService.getBookingsByUser(userId)
      .pipe(takeUntil(this.unsubscribe$)) // Ensure subscription is cleaned up on component destruction
      .subscribe({
        next: (bookings: Booking[]) => {
          this.bookings = bookings; // Assign fetched bookings to the component property
          this.isLoading = false; // End loading state
          if (this.bookings.length === 0) {
            this.errorMessage = 'You have no active or past bookings.'; // Message for no bookings
          }
        },
        error: (err: any) => {
          // Handle errors during API call
          console.error('Error fetching bookings:', err);
          this.isLoading = false; // End loading state
          this.errorMessage = `Failed to load bookings: ${err.error?.message || err.message || 'An unexpected error occurred.'}`;
        }
      });
  }

  // Optional: Add methods for actions like canceling a booking or viewing details.
  // These would typically involve calling methods on the BookingServiceService
  // and then updating the local 'bookings' array or navigating to another route.

  // cancelBooking(bookingId: number): void {
  //   if (confirm('Are you sure you want to cancel this booking? This action cannot be undone.')) {
  //     this.bookingService.deleteBooking(bookingId) // Assuming you have a deleteBooking method in your service
  //       .pipe(takeUntil(this.unsubscribe$))
  //       .subscribe({
  //         next: () => {
  //           this.bookings = this.bookings.filter(b => b.id !== bookingId); // Remove cancelled booking from list
  //           alert('Booking cancelled successfully!');
  //           if (this.bookings.length === 0) {
  //             this.errorMessage = 'You have no active or past bookings.';
  //           }
  //         },
  //         error: (err: any) => {
  //           console.error('Error cancelling booking:', err);
  //           alert(`Failed to cancel booking: ${err.error?.message || 'An error occurred.'}`);
  //         }
  //       });
  //   }
  // }

  // viewBookingDetails(booking: Booking): void {
  //   // Example: Navigate to a detailed view of the booking
  //   this.router.navigate(['/user/booking-details', booking.id]);
  // }
}
