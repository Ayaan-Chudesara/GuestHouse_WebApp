import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminPendingBookingsComponent } from './admin-pending-bookings.component';

describe('AdminPendingBookingsComponent', () => {
  let component: AdminPendingBookingsComponent;
  let fixture: ComponentFixture<AdminPendingBookingsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AdminPendingBookingsComponent]
    });
    fixture = TestBed.createComponent(AdminPendingBookingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
