import { Component, OnInit } from '@angular/core';
import { Booking, BookingStatus } from 'src/app/core/models/booking.model';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';
import { AdminReservationsService } from '../services/admin-reservations.service';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/auth/auth.service';

@Component({
  selector: 'app-admin-reservations',
  templateUrl: './admin-reservations.component.html',
  styleUrls: ['./admin-reservations.component.css']
})
export class AdminReservationsComponent implements OnInit {
  bookings: Booking[] = [];
  loading = false;
  error: string | null = null;
  bookingStatus = BookingStatus; // Make enum available in template for ngIf

  // Filtering properties
  filterGuestHouseId: number | null = null;
  filterRoomType: string | null = null;
  filterCheckInDate: string | null = null;
  filterCheckOutDate: string | null = null;
  filterStatus: BookingStatus | null = null;

  // Data for filter dropdowns
  guestHouses: GuestHouse[] = [];
  roomTypes: string[] = ['Single Room', 'Double Room', 'Suite'];
  allBookingStatuses = Object.values(BookingStatus); // Get all enum values for dropdown

  constructor(
    private adminService: AdminReservationsService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    if (!this.authService.hasAdminAccess()) {
      this.error = 'Access denied. Admin privileges required.';
      this.router.navigate(['/auth/login']);
      return;
    }
    this.loadGuestHouses();
    this.loadRoomTypes();
    this.applyFilters();
  }

  loadGuestHouses(): void {
    this.adminService.getGuestHousesForFilter().subscribe({
      next: (data) => {
        this.guestHouses = data;
      },
      error: (err) => {
        console.error('Error loading guesthouses:', err);
        this.error = err.message || 'Failed to load guesthouses';
      }
    });
  }

  loadRoomTypes(): void {
    this.adminService.getRoomTypesForFilter().subscribe({
      next: (data) => {
        this.roomTypes = data;
      },
      error: (err) => {
        console.error('Error loading room types:', err);
        this.error = err.message || 'Failed to load room types';
      }
    });
  }

  applyFilters(): void {
    this.loading = true;
    this.error = null;

    if (!this.authService.hasAdminAccess()) {
      this.error = 'Your session has expired or you do not have admin privileges.';
      this.loading = false;
      this.router.navigate(['/auth/login']);
      return;
    }

    try {
      let formattedCheckInDate: string | undefined;
      let formattedCheckOutDate: string | undefined;

      if (this.filterCheckInDate) {
        formattedCheckInDate = new Date(this.filterCheckInDate).toISOString().split('T')[0];
      }

      if (this.filterCheckOutDate) {
        formattedCheckOutDate = new Date(this.filterCheckOutDate).toISOString().split('T')[0];
      }

      const filters = {
        guestHouseId: this.filterGuestHouseId || undefined,
        roomType: this.filterRoomType || undefined,
        checkInDate: formattedCheckInDate,
        checkOutDate: formattedCheckOutDate,
        status: this.filterStatus || undefined
      };

      this.adminService.getFilteredBookings(filters).subscribe({
        next: (data) => {
          this.bookings = data;
          this.loading = false;
          this.error = null;
        },
        error: (err) => {
          console.error('Error fetching filtered bookings:', err);
          this.loading = false;
          if (err.status === 403) {
            this.error = 'You do not have permission to access this resource. Please check your admin privileges.';
            this.router.navigate(['/auth/login']);
          } else {
            this.error = err.message || 'An error occurred while fetching bookings.';
          }
        }
      });
    } catch (err) {
      console.error('Error in applyFilters:', err);
      this.loading = false;
      this.error = 'An error occurred while applying filters.';
    }
  }

  clearFilters(): void {
    this.filterGuestHouseId = null;
    this.filterRoomType = null;
    this.filterCheckInDate = null;
    this.filterCheckOutDate = null;
    this.filterStatus = null;
    this.applyFilters();
  }

  // --- Action Methods ---

  approveBooking(bookingId: number): void {
    if (confirm(`Are you sure you want to approve booking ${bookingId}?`)) {
      this.adminService.approveBooking(bookingId).subscribe({
        next: (message: string) => {
          console.log(`Booking ${bookingId} approved:`, message);
          this.applyFilters(); // Refresh the list to show updated status
        },
        error: (err: Error) => {
          console.error(`Error approving booking ${bookingId}:`, err);
          alert(`Failed to approve booking: ${err.message}`);
        }
      });
    }
  }

  rejectBooking(bookingId: number): void {
    if (confirm(`Are you sure you want to reject booking ${bookingId}?`)) {
      this.adminService.rejectBooking(bookingId).subscribe({
        next: (message: string) => {
          console.log(`Booking ${bookingId} rejected:`, message);
          this.applyFilters(); // Refresh the list
        },
        error: (err: Error) => {
          console.error(`Error rejecting booking ${bookingId}:`, err);
          alert(`Failed to reject booking: ${err.message}`);
        }
      });
    }
  }

  // Using the generic update status endpoint for check-in/out/cancel
  updateBookingStatus(bookingId: number, newStatus: BookingStatus): void {
    if (confirm(`Are you sure you want to change status of booking ${bookingId} to ${newStatus}?`)) {
      this.adminService.updateBookingStatusGeneric(bookingId, newStatus).subscribe({
        next: (updatedBooking: Booking) => {
          console.log(`Booking ${bookingId} status updated to ${newStatus}`, updatedBooking);
          this.applyFilters(); // Refresh the list
        },
        error: (err: Error) => {
          console.error(`Error updating booking ${bookingId} status to ${newStatus}:`, err);
          alert(`Failed to update status: ${err.message}`);
        }
      });
    }
  }

  deleteBooking(bookingId: number): void {
    if (confirm(`Are you sure you want to delete booking ${bookingId}? This action cannot be undone.`)) {
      this.adminService.deleteBooking(bookingId).subscribe({
        next: () => {
          console.log(`Booking ${bookingId} deleted successfully.`);
          this.applyFilters(); // Refresh the list
        },
        error: (err: Error) => {
          console.error(`Error deleting booking ${bookingId}:`, err);
          alert(`Failed to delete booking: ${err.message}`);
        }
      });
    }
  }
}
