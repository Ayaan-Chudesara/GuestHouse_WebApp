import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/auth/auth.service';
import { Booking, BookingRequest } from 'src/app/core/models/booking.model';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';
import { Room } from 'src/app/core/models/room.model';
import { BookingServiceService } from '../services/booking-service.service';

@Component({
  selector: 'app-user-booking',
  templateUrl: './user-booking.component.html',
  styleUrls: ['./user-booking.component.css']
})
export class UserBookingComponent {
   
  bookingForm!: FormGroup;
  availableRooms: Room[] = [];
  selectedRoom: Room | null = null;
  message: string = '';
  searchAttempted: boolean = false;
  currentUserId: number | null = null;

  guestHouses: GuestHouse[] = [];
  roomTypes: string[] = ['Single Room', 'Double Room', 'Suite'];

  constructor(
    private fb: FormBuilder,
    private bookingService: BookingServiceService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.initBookingForm();
    this.fetchGuestHouses();

    const token = this.authService.getToken();
    if (token && !this.authService.isTokenExpired(token)) {
      this.currentUserId = this.authService.getDecodedToken()?.userId;
      if (this.currentUserId === null || this.currentUserId === undefined) {
        this.message = 'Error: Could not retrieve user ID from token. Please ensure your token contains a "userId" claim and log in again.';
        this.router.navigate(['/auth/login']);
      } else {
        console.log("Current User ID from token:", this.currentUserId);
        this.message = 'Ready to book your stay.';
      }
    } else {
      this.message = 'You must be logged in to make a booking. Please log in.';
      this.router.navigate(['/auth/login']);
    }

    this.bookingForm.get('checkInDate')?.valueChanges.subscribe(value => {
      const checkIn = new Date(value);
      const checkOut = new Date(this.bookingForm.get('checkOutDate')?.value);
      if (checkIn && checkOut && checkIn >= checkOut) {
        const nextDay = new Date(checkIn);
        nextDay.setDate(nextDay.getDate() + 1);
        this.bookingForm.get('checkOutDate')?.setValue(nextDay);
      }
    });

    this.bookingForm.get('checkOutDate')?.setValidators(
      Validators.min(new Date(this.getTomorrowDate()).getTime())
    );
  }

  initBookingForm(): void {
    this.bookingForm = this.fb.group({
      checkInDate: [new Date(), Validators.required],
      checkOutDate: [new Date(new Date().setDate(new Date().getDate() + 1)), Validators.required],
      numberOfGuests: [1, [Validators.required, Validators.min(1)]],
      guestHouseId: [null, Validators.required],
      roomTypeId: [null, Validators.required],
      purpose: ['']
    });
  }

  fetchGuestHouses(): void {
    this.guestHouses = [
      { id: 1, name: 'Main Guesthouse', location: 'City Center' },
      { id: 2, name: 'Lakeside Retreat', location: 'Lakefront' }
    ];
  }

  searchAvailability(): void {
    this.searchAttempted = true;
    this.selectedRoom = null;
    this.availableRooms = [];
    this.message = '';

    if (this.bookingForm.invalid) {
      this.message = 'Please fill in all required fields and ensure dates are valid.';
      this.bookingForm.markAllAsTouched();
      return;
    }

    const checkIn = this.bookingForm.get('checkInDate')?.value;
    const checkOut = this.bookingForm.get('checkOutDate')?.value;
    const numberOfGuests = this.bookingForm.get('numberOfGuests')?.value;
    const guestHouseId = this.bookingForm.get('guestHouseId')?.value;
    const roomType = this.bookingForm.get('roomTypeId')?.value; // Renamed for clarity in filter

    if (!checkIn || !checkOut || checkIn >= checkOut) {
        this.message = 'Please select valid check-in and check-out dates.';
        return;
    }

    this.message = 'Searching for rooms...';

    // IMPORTANT: This call should ideally go to a backend endpoint
    // that filters rooms based on all criteria (dates, guest house, room type, number of guests)
    // and returns only truly available rooms (considering bed availability).
    // For now, it will fetch ALL rooms and filter them on the frontend.
    this.bookingService.getAllRooms().subscribe({
      next: (rooms: Room[]) => {
        this.availableRooms = rooms.filter(room =>
          room.numberOfBeds >= numberOfGuests && // <--- RE-INTRODUCED: Filter by numberOfBeds
          (guestHouseId ? room.guestHouseId === guestHouseId : true) &&
          (roomType ? room.roomType === roomType : true) // Use roomType here
        );
        this.message = '';
        if (this.availableRooms.length === 0) {
          this.message = 'No rooms available for the selected criteria. Please try different dates or criteria.';
        }
      },
      error: (err: any) => {
        console.error('Error fetching rooms:', err);
        this.message = `Failed to load rooms: ${err.message || 'An error occurred.'}`;
      }
    });
  }

  selectRoom(room: Room): void {
    this.selectedRoom = room;
    this.message = `Room ${room.roomNo} selected. This room has ${room.numberOfBeds} beds. Proceed to confirm your booking.`;
  }

  calculateTotalPrice(): number {
    if (!this.selectedRoom || !this.bookingForm.get('checkInDate')?.value || !this.bookingForm.get('checkOutDate')?.value || !this.selectedRoom.pricePerNight) {
      return 0;
    }
    const checkIn = new Date(this.bookingForm.get('checkInDate')?.value);
    const checkOut = new Date(this.bookingForm.get('checkOutDate')?.value);
    const diffTime = Math.abs(checkOut.getTime() - checkIn.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return this.selectedRoom.pricePerNight * diffDays; // <--- Now uses selectedRoom.pricePerNight
  }

  clearSelection(): void {
    this.selectedRoom = null;
    this.message = '';
  }

  confirmBooking(): void {
    if (!this.selectedRoom) {
      this.message = 'Please select a room to book.';
      return;
    }
    if (this.currentUserId === null || this.currentUserId === undefined) {
      this.message = 'User ID is missing. Please log in again.';
      this.router.navigate(['/auth/login']);
      return;
    }

    const checkInDateValue = this.bookingForm.get('checkInDate')?.value;
    const checkOutDateValue = this.bookingForm.get('checkOutDate')?.value;

    const checkInDateString = checkInDateValue ? new Date(checkInDateValue).toISOString().split('T')[0] : '';
    const checkOutDateString = checkOutDateValue ? new Date(checkOutDateValue).toISOString().split('T')[0] : '';

    const bookingRequest: BookingRequest = {
      roomId: this.selectedRoom.id!,
      userId: this.currentUserId,
      checkInDate: checkInDateString,
      checkOutDate: checkOutDateString,
      numberOfGuests: this.bookingForm.get('numberOfGuests')?.value,
      purpose: this.bookingForm.get('purpose')?.value
    };

    this.message = 'Submitting your booking...';
    this.bookingService.createBooking(bookingRequest).subscribe({
      next: (booking: Booking) => {
        this.message = `Booking confirmed! Your booking ID is ${booking.id}.`;
        alert(`Booking Confirmed! Room: ${booking.roomNo}, Check-in: ${booking.checkInDate}, Check-out: ${booking.checkOutDate}`);
        this.router.navigate(['/user/my-bookings']);
        this.resetForm();
      },
      error: (err: any) => {
        console.error('Booking failed:', err);
        this.message = `Booking failed: ${err.error?.message || err.message || 'An error occurred.'}`;
      }
    });
  }

  resetForm(): void {
    this.bookingForm.reset({
      checkInDate: new Date(),
      checkOutDate: new Date(new Date().setDate(new Date().getDate() + 1)),
      numberOfGuests: 1,
      guestHouseId: null,
      roomTypeId: null,
      purpose: ''
    });
    this.availableRooms = [];
    this.selectedRoom = null;
    this.searchAttempted = false;
    this.message = '';
    this.bookingForm.markAsUntouched();
    this.bookingForm.markAsPristine();
  }

  getTodayDate(): Date {
    return new Date();
  }

  getTomorrowDate(): string {
    const today = new Date();
    today.setDate(today.getDate() + 1);
    return today.toISOString().split('T')[0];
  }
}
