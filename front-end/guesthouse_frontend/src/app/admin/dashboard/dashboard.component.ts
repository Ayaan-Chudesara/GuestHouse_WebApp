import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
 availableRooms = 27;
  reservations = 0;
  pendingRequests = 0;
  checkIns = 0;
  checkOuts = 0;

  startDate: string = '';
  endDate: string = '';
  totalBeds: number | null = null;

  todayDate = new Date().toLocaleDateString();
  todayMonthYear = new Date().toLocaleString('default', { month: 'long', year: 'numeric' });

  getTotalBeds() {
    if (this.startDate && this.endDate) {
      this.totalBeds = 42; // Replace with real logic/API
    }
  }
}
