import { Component } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-admin-booking',
  templateUrl: './admin-booking.component.html',
  styleUrls: ['./admin-booking.component.css']
})
export class AdminBookingComponent {
 bookingForm!: FormGroup;

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {
    this.bookingForm = this.fb.group({
      guestName: ['', Validators.required],
      guestEmail: ['', [Validators.required, Validators.email]],
      checkInDate: ['', Validators.required],
      checkOutDate: ['', Validators.required],
      guestHouse: ['', Validators.required],
      roomType: ['', Validators.required],
      // Add more controls as needed for bed selection, number of guests, etc.
    });
  }

  onSubmit(): void {
    if (this.bookingForm.valid) {
      console.log('Booking Form Data:', this.bookingForm.value);
      // Here you would typically send this data to your backend API
      // For now, we're just logging it.
      alert('Booking form submitted! Check console for data.');
      // this.bookingForm.reset(); // Optionally reset the form after submission
    } else {
      // Mark all fields as touched to display validation errors
      this.bookingForm.markAllAsTouched();
      alert('Please fill in all required fields correctly.');
    }
  }

  // Helper to quickly get form controls in template if needed (optional)
  get f() { return this.bookingForm.controls; }
}
