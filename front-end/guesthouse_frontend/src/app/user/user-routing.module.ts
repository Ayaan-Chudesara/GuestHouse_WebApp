import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { HomeComponent } from './home/home.component';
import { UserBookingComponent } from './user-booking/user-booking.component';
import { MyBookingsComponent } from './my-bookings/my-bookings.component';

const routes: Routes = [
   {path: '', // This path can be empty if admin module is lazy-loaded under a 'admin' parent route
      component: HomeComponent, // This component contains your admin navbar and layout
      children: [
        { path: 'booking', component: UserBookingComponent },
        {path: 'my-bookings', component: MyBookingsComponent} // Dashboard route]
      ]}
      ];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UserRoutingModule { }
