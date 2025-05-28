import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/auth/auth.service';
import { Booking, BookingRequest } from 'src/app/core/models/booking.model';
import { GuestHouse } from 'src/app/core/models/guesthouse.model';
import { Room } from 'src/app/core/models/room.model';
import { BookingServiceService } from '../services/booking-service.service';
import { GuesthouseService } from 'src/app/admin/services/guesthouse.service';

@Component({
  selector: 'app-user-booking',
  templateUrl: './user-booking.component.html',
  styleUrls: ['./user-booking.component.css']
})
export class UserBookingComponent implements OnInit {
   
  bookingForm!: FormGroup;
  availableRooms: Room[] = [];
  selectedRoom: Room | null = null;
  message: string = '';
  searchAttempted: boolean = false;
  currentUserId: number | null = null;
  loading: boolean = false;

  guestHouses: GuestHouse[] = [];
  roomTypes: string[] = [];

  constructor(
    private fb: FormBuilder,
    private bookingService: BookingServiceService,
    private authService: AuthService,
    private router: Router,
    private guestHouseService: GuesthouseService
  ) { }

  ngOnInit(): void {
    this.initBookingForm();
    this.fetchGuestHouses();
    this.fetchRoomTypes();

    // Add form validation debugging
    this.bookingForm.valueChanges.subscribe(value => {
      console.log('Form values:', value);
      console.log('Form valid:', this.bookingForm.valid);
      console.log('Form errors:', {
        checkInDate: this.bookingForm.get('checkInDate')?.errors,
        checkOutDate: this.bookingForm.get('checkOutDate')?.errors,
        numberOfGuests: this.bookingForm.get('numberOfGuests')?.errors,
        guestHouseId: this.bookingForm.get('guestHouseId')?.errors,
        roomTypeId: this.bookingForm.get('roomTypeId')?.errors
      });
    });

    const token = this.authService.getToken();
    if (token && !this.authService.isTokenExpired(token)) {
      this.currentUserId = this.authService.getUserIdFromToken();
      console.log('Current User ID:', this.currentUserId);
      
      if (this.currentUserId === null || this.currentUserId === undefined) {
        this.message = 'Error: Could not retrieve a valid user ID. Please log in again.';
        this.router.navigate(['/auth/login']);
      } else {
        // Verify the user exists in the database
        this.bookingService.verifyUser(this.currentUserId).subscribe({
          next: (isValid) => {
            if (!isValid) {
              this.message = 'Error: Your user account was not found. Please log in again.';
              this.authService.logout();
              this.router.navigate(['/auth/login']);
            } else {
              this.message = 'Ready to book your stay.';
            }
          },
          error: (err) => {
            console.error('Error verifying user:', err);
            this.message = 'Error: Could not verify your user account. Please try logging in again.';
            this.authService.logout();
            this.router.navigate(['/auth/login']);
          }
        });
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
    this.guestHouseService.getAllGuestHouses().subscribe({
      next: (guestHouses) => {
        this.guestHouses = guestHouses;
      },
      error: (error) => {
        console.error('Error fetching guesthouses:', error);
        this.message = 'Failed to load guesthouses. Please try again later.';
      }
    });
  }

  fetchRoomTypes(): void {
    // First get all rooms to extract unique room types
    this.bookingService.getAllRooms().subscribe({
      next: (rooms: Room[]) => {
        // Extract unique room types
        this.roomTypes = [...new Set(rooms.map(room => room.roomType))];
      },
      error: (error) => {
        console.error('Error fetching room types:', error);
        this.message = 'Failed to load room types. Please try again later.';
      }
    });
  }

  searchAvailability(): void {
    this.searchAttempted = true;
    this.selectedRoom = null;
    this.availableRooms = [];
    this.message = '';
    this.loading = true;

    if (this.bookingForm.invalid) {
      this.message = 'Please fill in all required fields and ensure dates are valid.';
      this.bookingForm.markAllAsTouched();
      this.loading = false;
      return;
    }

    const checkIn = this.bookingForm.get('checkInDate')?.value;
    const checkOut = this.bookingForm.get('checkOutDate')?.value;
    const numberOfGuests = this.bookingForm.get('numberOfGuests')?.value;
    const guestHouseId = this.bookingForm.get('guestHouseId')?.value;
    const roomType = this.bookingForm.get('roomTypeId')?.value;

    if (!checkIn || !checkOut || checkIn >= checkOut) {
        this.message = 'Please select valid check-in and check-out dates.';
        this.loading = false;
        return;
    }

    this.message = 'Searching for rooms...';

    this.bookingService.getAllRooms().subscribe({
      next: (rooms: Room[]) => {
        console.log('All rooms:', rooms);
        console.log('Filter criteria:', { numberOfGuests, guestHouseId, roomType });
        
        this.availableRooms = rooms.filter(room => {
          const matchesGuests = room.numberOfBeds >= numberOfGuests;
          const matchesGuestHouse = !guestHouseId || room.guestHouseId === Number(guestHouseId);
          const matchesRoomType = !roomType || room.roomType === roomType;
          
          console.log(`Room ${room.roomNo}:`, {
            matchesGuests,
            matchesGuestHouse,
            matchesRoomType,
            roomType: room.roomType,
            selectedType: roomType
          });
          
          return matchesGuests && matchesGuestHouse && matchesRoomType;
        });

        this.loading = false;
        this.message = '';
        
        if (this.availableRooms.length === 0) {
          this.message = 'No rooms available for the selected criteria. Please try different dates or criteria.';
        }
      },
      error: (err: any) => {
        console.error('Error fetching rooms:', err);
        this.message = `Failed to load rooms: ${err.message || 'An error occurred.'}`;
        this.loading = false;
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

    if (!this.selectedRoom.id) {
      this.message = 'Invalid room selection. Please try again.';
      return;
    }

    const checkInDateValue = this.bookingForm.get('checkInDate')?.value;
    const checkOutDateValue = this.bookingForm.get('checkOutDate')?.value;

    if (!checkInDateValue || !checkOutDateValue) {
      this.message = 'Please select valid check-in and check-out dates.';
      return;
    }

    // Ensure dates are in YYYY-MM-DD format
    const formatDate = (date: Date): string => {
      const d = new Date(date);
      return d.toISOString().split('T')[0];
    };

    const bookingRequest: BookingRequest = {
      roomId: this.selectedRoom.id,
      userId: this.currentUserId,
      checkInDate: formatDate(checkInDateValue),
      checkOutDate: formatDate(checkOutDateValue),
      numberOfGuests: this.bookingForm.get('numberOfGuests')?.value || 1,
      purpose: this.bookingForm.get('purpose')?.value || ''
    };

    console.log('Sending booking request:', bookingRequest);

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
        let errorMessage = 'Booking failed: ';
        if (err.error?.message) {
          errorMessage += err.error.message;
        } else if (err.error instanceof Object) {
          // If the error is an object, stringify it for debugging
          errorMessage += JSON.stringify(err.error);
        } else if (err.message) {
          errorMessage += err.message;
        } else {
          errorMessage += 'An unknown error occurred.';
        }
        this.message = errorMessage;
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
