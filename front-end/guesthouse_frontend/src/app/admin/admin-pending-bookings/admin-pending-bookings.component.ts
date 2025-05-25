import { Component } from '@angular/core';
import { Booking } from 'src/app/core/models/booking.model';
import { AdminReservationsService } from '../services/admin-reservations.service';
import { Observable, forkJoin, finalize } from 'rxjs';

@Component({
  selector: 'app-admin-pending-bookings',
  templateUrl: './admin-pending-bookings.component.html',
  styleUrls: ['./admin-pending-bookings.component.css']
})
export class AdminPendingBookingsComponent {
  pendingBookings: Booking[] = [];
  loading = false;
  error: string | null = null;
  processingAll = false; // To disable buttons during "Approve All" / "Reject All"

  constructor(private adminService: AdminReservationsService) { }

  ngOnInit(): void {
    this.loadPendingBookings();
  }

  loadPendingBookings(): void {
    this.loading = true;
    this.error = null;
    this.adminService.getAllPendingBookings().subscribe({
      next: (data) => {
        this.pendingBookings = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching pending bookings:', err);
        this.error = 'Failed to load pending bookings. Please try again.';
        this.loading = false;
      }
    });
  }

  approveBooking(bookingId: number): void {
    if (confirm(`Are you sure you want to approve booking ${bookingId}?`)) {
      this.loading = true; // Show loading for individual action
      this.adminService.approveBooking(bookingId).subscribe({
        next: (response) => {
          console.log(`Booking ${bookingId} approved:`, response);
          this.loadPendingBookings(); // Reload to remove the approved booking
        },
        error: (err) => {
          console.error(`Error approving booking ${bookingId}:`, err);
          alert(`Failed to approve booking: ${err.error || err.message}`);
          this.loading = false; // Hide loading on error
        }
      });
    }
  }

  rejectBooking(bookingId: number): void {
    if (confirm(`Are you sure you want to reject booking ${bookingId}?`)) {
      this.loading = true; // Show loading for individual action
      this.adminService.rejectBooking(bookingId).subscribe({
        next: (response) => {
          console.log(`Booking ${bookingId} rejected:`, response);
          this.loadPendingBookings(); // Reload to remove the rejected booking
        },
        error: (err) => {
          console.error(`Error rejecting booking ${bookingId}:`, err);
          alert(`Failed to reject booking: ${err.error || err.message}`);
          this.loading = false; // Hide loading on error
        }
      });
    }
  }

  approveAllPending(): void {
    if (this.pendingBookings.length === 0) {
      alert('No pending bookings to approve.');
      return;
    }

    if (confirm(`Are you sure you want to APPROVE ALL (${this.pendingBookings.length}) pending bookings?`)) {
      this.processingAll = true;
      this.error = null; // Clear previous errors

      const approvalRequests: Observable<string>[] = this.pendingBookings.map(booking =>
        this.adminService.approveBooking(booking.id)
      );

      // forkJoin waits for all observables to complete
      forkJoin(approvalRequests).pipe(
        finalize(() => this.processingAll = false) // Ensures processingAll is reset regardless of success/error
      ).subscribe({
        next: (responses) => {
          console.log('All pending bookings approved:', responses);
          alert(`Successfully approved ${responses.length} bookings.`);
          this.loadPendingBookings(); // Reload to show empty or updated list
        },
        error: (err) => {
          console.error('Error approving all pending bookings:', err);
          alert(`Failed to approve some or all bookings. Please check console for details.`);
          this.loadPendingBookings(); // Reload to see what's left
        }
      });
    }
  }

  rejectAllPending(): void {
    if (this.pendingBookings.length === 0) {
      alert('No pending bookings to reject.');
      return;
    }

    if (confirm(`Are you sure you want to REJECT ALL (${this.pendingBookings.length}) pending bookings?`)) {
      this.processingAll = true;
      this.error = null; // Clear previous errors

      const rejectionRequests: Observable<string>[] = this.pendingBookings.map(booking =>
        this.adminService.rejectBooking(booking.id)
      );

      // forkJoin waits for all observables to complete
      forkJoin(rejectionRequests).pipe(
        finalize(() => this.processingAll = false) // Ensures processingAll is reset
      ).subscribe({
        next: (responses) => {
          console.log('All pending bookings rejected:', responses);
          alert(`Successfully rejected ${responses.length} bookings.`);
          this.loadPendingBookings(); // Reload to show empty or updated list
        },
        error: (err) => {
          console.error('Error rejecting all pending bookings:', err);
          alert(`Failed to reject some or all bookings. Please check console for details.`);
          this.loadPendingBookings(); // Reload to see what's left
        }
      });
    }
  }
}
