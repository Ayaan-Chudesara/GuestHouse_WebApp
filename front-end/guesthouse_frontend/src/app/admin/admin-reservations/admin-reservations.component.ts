import { Component } from '@angular/core';
import { Booking, BookingStatus } from 'src/app/core/models/booking.model';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';
import { AdminReservationsService } from '../services/admin-reservations.service';

@Component({
  selector: 'app-admin-reservations',
  templateUrl: './admin-reservations.component.html',
  styleUrls: ['./admin-reservations.component.css']
})
export class AdminReservationsComponent {

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
  roomTypes: string[] = [];
  allBookingStatuses = Object.values(BookingStatus); // Get all enum values for dropdown

  constructor(private adminService: AdminReservationsService) { } // Inject AdminService

  ngOnInit(): void {
    this.loadFilterOptions(); // Populate dropdowns
    this.applyFilters(); // Load initial data (all bookings)
  }

  loadFilterOptions(): void {
    this.adminService.getGuestHousesForFilter().subscribe({
      next: (data) => this.guestHouses = data,
      error: (err) => console.error('Error loading guesthouses for filter:', err)
    });

    this.adminService.getRoomTypesForFilter().subscribe({
      next: (data) => this.roomTypes = data,
      error: (err) => console.error('Error loading room types for filter:', err)
    });
  }

  applyFilters(): void {
    this.loading = true;
    this.error = null;

    // Construct the filters object to pass to the service
    const filters = {
      guestHouseId: this.filterGuestHouseId || undefined, // Use undefined if null for cleaner params
      roomType: this.filterRoomType || undefined,
      checkInDate: this.filterCheckInDate || undefined,
      checkOutDate: this.filterCheckOutDate || undefined,
      status: this.filterStatus || undefined
    };

    this.adminService.getFilteredBookings(filters).subscribe({
      next: (data) => {
        this.bookings = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error fetching filtered bookings:', err);
        this.error = 'Failed to load bookings with applied filters.';
        this.loading = false;
      }
    });
  }

  clearFilters(): void {
    this.filterGuestHouseId = null;
    this.filterRoomType = null;
    this.filterCheckInDate = null;
    this.filterCheckOutDate = null;
    this.filterStatus = null;
    this.applyFilters(); // Reload all bookings after clearing filters
  }

  // --- Action Methods ---

  approveBooking(bookingId: number): void {
    if (confirm(`Are you sure you want to approve booking ${bookingId}?`)) {
      this.adminService.approveBooking(bookingId).subscribe({
        next: (message) => {
          console.log(`Booking ${bookingId} approved:`, message);
          this.applyFilters(); // Refresh the list to show updated status
        },
        error: (err) => {
          console.error(`Error approving booking ${bookingId}:`, err);
          alert(`Failed to approve booking: ${err.error || err.message}`);
        }
      });
    }
  }

  rejectBooking(bookingId: number): void {
    if (confirm(`Are you sure you want to reject booking ${bookingId}?`)) {
      this.adminService.rejectBooking(bookingId).subscribe({
        next: (message) => {
          console.log(`Booking ${bookingId} rejected:`, message);
          this.applyFilters(); // Refresh the list
        },
        error: (err) => {
          console.error(`Error rejecting booking ${bookingId}:`, err);
          alert(`Failed to reject booking: ${err.error || err.message}`);
        }
      });
    }
  }

  // Using the generic update status endpoint for check-in/out/cancel
  updateBookingStatus(bookingId: number, newStatus: BookingStatus): void {
      if (confirm(`Are you sure you want to change status of booking ${bookingId} to ${newStatus}?`)) {
          this.adminService.updateBookingStatusGeneric(bookingId, newStatus).subscribe({
              next: (updatedBooking) => {
                  console.log(`Booking ${bookingId} status updated to ${newStatus}`, updatedBooking);
                  this.applyFilters(); // Refresh the list
              },
              error: (err) => {
                  console.error(`Error updating booking ${bookingId} status to ${newStatus}:`, err);
                  alert(`Failed to update status: ${err.error?.message || err.error || err.statusText}`);
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
        error: (err) => {
          console.error(`Error deleting booking ${bookingId}:`, err);
          alert(`Failed to delete booking: ${err.error?.message || err.error || err.statusText}`);
        }
      });
    }
  }
}
