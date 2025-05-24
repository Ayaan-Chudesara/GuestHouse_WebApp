import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../services/dashboard.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  availableRooms = 0;
  reservations = 0;
  pendingRequests = 0;
  checkIns = 0;
  checkOuts = 0;
  totalBeds: number | null = null;

  startDate: string = '';
  endDate: string = '';

  todayDate = new Date().toLocaleDateString();
  todayMonthYear = new Date().toLocaleString('default', { month: 'long', year: 'numeric' });

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.fetchDashboardStats();
  }

  fetchDashboardStats(): void {
    this.dashboardService.getDashboardStats().subscribe({
      next: (data) => {
        this.availableRooms = data.availableRooms;
        this.reservations = data.reservations;
        this.pendingRequests = data.pendingRequests;
        this.checkIns = data.checkIns;
        this.checkOuts = data.checkOuts;
      },
      error: (err) => {
        console.error('Failed to fetch dashboard stats:', err);
      }
    });
  }

  fetchTotalBeds(): void {
    this.dashboardService.getTotalBeds().subscribe({
      next: (count) => {
        this.totalBeds = count;
      },
      error: (err) => {
        console.error('Failed to fetch total beds:', err);
      }
    });
  }

 onDateRangeSelected(): void {
  if (this.startDate && this.endDate) {
    this.dashboardService.getTotalBeds(this.startDate, this.endDate).subscribe({
      next: (count) => {
        this.totalBeds = count;
      },
      error: (err) => {
        console.error('Failed to fetch total beds:', err);
      }
    });
  }
}

}
