// src/app/admin/dashboard/dashboard.component.ts (or wherever your dashboard component is)

import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../services/dashboard.service'; // Ensure this path is correct

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  availableBeds = 0;
  totalBookings = 0;
  pendingRequests = 0;
  checkIns = 0;
  checkOuts = 0;
  totalBeds: number | null = null;

  startDate: Date | string | null = null;
  endDate: Date | string | null = null;

  todayDate = new Date().toLocaleDateString();
  todayMonthYear = new Date().toLocaleString('default', { month: 'long', year: 'numeric' });

  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.fetchDashboardStats();
  }

  fetchDashboardStats(): void {
    this.dashboardService.getDashboardStats().subscribe({
      next: (data) => {
        this.availableBeds = data.availableBeds;
        this.totalBookings = data.totalBookings;
        this.pendingRequests = data.pendingRequests;
        this.checkIns = data.checkIns;
        this.checkOuts = data.checkOuts;

        console.log('Dashboard stats fetched successfully:', data);
        console.log('Mapped stats:', {
            availableBeds: this.availableBeds,
            totalBookings: this.totalBookings,
            pendingRequests: this.pendingRequests,
            checkIns: this.checkIns,
            checkOuts: this.checkOuts
        });
      },
      error: (err) => {
        console.error('Failed to fetch dashboard stats:', err);
      }
    });
  }

  onDateRangeSelected(): void {
    let formattedStartDate: string | null = null;
    let formattedEndDate: string | null = null;

    if (this.startDate) {
        if (this.startDate instanceof Date) {
            formattedStartDate = this.startDate.toISOString().split('T')[0];
        } else if (typeof this.startDate === 'string') {
            formattedStartDate = this.startDate;
        }
    }

    if (this.endDate) {
        if (this.endDate instanceof Date) {
            formattedEndDate = this.endDate.toISOString().split('T')[0];
        } else if (typeof this.endDate === 'string') {
            formattedEndDate = this.endDate;
        }
    }

    if (formattedStartDate && formattedEndDate) {
      this.dashboardService.getTotalBeds(formattedStartDate, formattedEndDate).subscribe({
        next: (count) => {
          this.totalBeds = count;
          console.log(`Total beds for range ${formattedStartDate} to ${formattedEndDate}:`, count);
        },
        error: (err) => {
          console.error('Failed to fetch total beds (with range):', err);
        }
      });
    } else {
        console.warn('Start date and/or End date not selected for total beds search.');
    }
  }
}