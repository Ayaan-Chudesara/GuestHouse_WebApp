import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { UserRoutingModule } from './user-routing.module';

import { HomeComponent } from './home/home.component';
import { NavbarComponent } from '../shared/navbar/navbar.component';
import { SharedModule } from '../shared/shared.module';
import { UserBookingComponent } from './user-booking/user-booking.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MyBookingsComponent } from './my-bookings/my-bookings.component';
import { MatTableModule } from '@angular/material/table';         // For mat-table
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner'; // For mat-spinner
import { MatCheckboxModule } from '@angular/material/checkbox';

@NgModule({
  declarations: [
   UserBookingComponent,
    HomeComponent,
    MyBookingsComponent
    
    
  ],
  imports: [
    CommonModule,
    UserRoutingModule,
    SharedModule,
    FormsModule,
     MatFormFieldModule,
        MatInputModule,
        MatDatepickerModule,
        MatNativeDateModule, // Important for datepicker
        MatSelectModule,
        MatButtonModule,
        MatCardModule,
        MatIconModule,
        MatSnackBarModule,

        ReactiveFormsModule,

        MatTableModule, 
        MatProgressSpinnerModule,
        MatCheckboxModule,
         
  
  ]
})
export class UserModule { }
