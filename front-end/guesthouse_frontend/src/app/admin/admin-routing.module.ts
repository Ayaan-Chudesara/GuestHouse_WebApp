import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { HomeComponent } from './home/home.component';
import { AdminBookingComponent } from './admin-booking/admin-booking.component';

const routes: Routes = [
  {
       path: '', // This path can be empty if admin module is lazy-loaded under a 'admin' parent route
    component: HomeComponent, // This component contains your admin navbar and layout
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'booking', component: AdminBookingComponent }, // Add the booking page here
      // Add routes for other admin pages as you build them:
      // { path: 'reservations', component: ReservationListComponent },
      // { path: 'pending-requests', component: PendingRequestsComponent },
      // { path: 'reports', component: ReportsComponent },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' } // Redirect empty path to dashboard
    ]

  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule { }
