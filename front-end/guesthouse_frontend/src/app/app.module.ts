import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
// Import Angular Material Modules we'll use
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core'; // For mat-datepicker
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon'; 
import { AdminBookingComponent } from './admin/admin-booking/admin-booking.component';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import {  HTTP_INTERCEPTORS } from '@angular/common/http';

import { JwtInterceptor } from './interceptors/jwt.interceptor';


@NgModule({
  declarations: [
    AppComponent
   
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    HttpClientModule,
    BrowserAnimationsModule,

     MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule, // Important for datepicker
    MatSelectModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatSnackBarModule
  ],
  providers: [
 {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true // This is crucial to allow multiple interceptors if you add more later
    }

  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
