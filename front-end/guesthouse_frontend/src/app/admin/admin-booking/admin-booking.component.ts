import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { DashboardService } from '../services/dashboard.service';
import { AdminPanelService } from 'src/app/admin/services/admin-panel.service'; // UPDATED PATH HERE
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-admin-booking',
  templateUrl: './admin-booking.component.html',
  styleUrls: ['./admin-booking.component.css']
})
export class AdminBookingComponent {
  bookingForm!: FormGroup;
  guestHouses: any[] = []; // Using 'any[]' for simplicity as requested

  // Dashboard Stats
  dashboardStats: any = {}; // To hold data from /api/admin/dashboard/stats
  totalBeds: number = 0;   // From /api/admin/dashboard/total-beds
  schedulerBookings: any[] = []; // Data for scheduler from /api/admin/dashboard/scheduler

  constructor(
    private fb: FormBuilder,
    private adminPanelService: AdminPanelService, // INJECT NEW SERVICE
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.bookingForm = this.fb.group({
      guestName: ['', Validators.required],
      guestEmail: ['', [Validators.required, Validators.email]],
      checkInDate: ['', Validators.required],
      checkOutDate: ['', Validators.required],
      guestHouse: ['', Validators.required], // guestHouseId maps to this
      roomType: ['', Validators.required],
      numberOfBeds: [1, [Validators.required, Validators.min(1)]],
      purpose: ['']
    });

    this.loadDashboardData();
    this.loadGuestHouses();
  }

  loadDashboardData(): void {
    // Fetch overall dashboard stats
    this.adminPanelService.getDashboardStats().subscribe({ // Use adminPanelService
      next: (data) => {
        this.dashboardStats = data;
        console.log('Admin Dashboard Stats:', this.dashboardStats);
      },
      error: (err) => console.error('Failed to load admin dashboard stats:', err)
    });

    // Fetch total beds
    this.adminPanelService.getTotalBeds().subscribe({ // Use adminPanelService
      next: (count) => {
        this.totalBeds = count;
        console.log('Admin Total Beds:', this.totalBeds);
      },
      error: (err) => console.error('Failed to load admin total beds:', err)
    });

    // Fetch scheduler data (e.g., for the next 30 days)
    const today = new Date();
    const futureDate = new Date();
    futureDate.setDate(today.getDate() + 30);
    const startDate = today.toISOString().split('T')[0];
    const endDate = futureDate.toISOString().split('T')[0];

    this.adminPanelService.getSchedulerData(startDate, endDate).subscribe({ // Use adminPanelService
      next: (data) => {
        this.schedulerBookings = data;
        console.log('Admin Scheduler Data:', this.schedulerBookings);
      },
      error: (err) => console.error('Failed to load admin scheduler data:', err)
    });
  }

  loadGuestHouses(): void {
    // This will use the mock data from AdminPanelService for guest houses if no direct API.
    this.adminPanelService.getGuestHouses().subscribe({ // Use adminPanelService
      next: (data) => this.guestHouses = data,
      error: (err) => {
        console.error('Failed to load guest houses (via AdminPanelService):', err);
        this.guestHouses = [ // Fallback mock data
          { id: 1, name: 'Main Guest House' },
          { id: 2, name: 'Annex Guest House' },
          { id: 3, name: 'Garden View Suites' }
        ];
        this.snackBar.open('Could not load Guest Houses from API. Using mock data.', 'Close', { duration: 3000 });
      }
    });
  }

  onSubmit(): void {
    if (this.bookingForm.invalid) {
      this.snackBar.open('Please fill out all required fields correctly.', 'Close', { duration: 3000 });
      this.bookingForm.markAllAsTouched();
      return;
    }

    const formValue = this.bookingForm.value;

    const adminBookingRequest = {
      guestName: formValue.guestName,
      guestEmail: formValue.guestEmail,
      checkInDate: new Date(formValue.checkInDate).toISOString().split('T')[0],
      checkOutDate: new Date(formValue.checkOutDate).toISOString().split('T')[0],
      guestHouseId: formValue.guestHouse,
      roomType: formValue.roomType,
      numberOfBeds: formValue.numberOfBeds,
      purpose: formValue.purpose
    };

    console.log('Sending Admin Booking Request to backend:', adminBookingRequest);

    this.adminPanelService.createBooking(adminBookingRequest).subscribe({ // Use adminPanelService
      next: (response) => {
        this.snackBar.open('Booking created successfully!', 'Close', { duration: 3000 });
        this.bookingForm.reset();
        this.loadDashboardData(); // Refresh dashboard stats and scheduler data
        Object.keys(this.bookingForm.controls).forEach(key => {
          this.bookingForm.get(key)?.setErrors(null);
        });
      },
      error: (err) => {
        console.error('Error creating booking:', err);
        this.snackBar.open('Failed to create booking: ' + (err.error?.message || 'Server error'), 'Close', { duration: 5000 });
      }
    });
  }

  onClearForm(): void {
    this.bookingForm.reset();
    Object.keys(this.bookingForm.controls).forEach(key => {
      this.bookingForm.get(key)?.setErrors(null);
    });
  }
}
