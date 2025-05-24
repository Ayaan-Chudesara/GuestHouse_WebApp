import { TestBed } from '@angular/core/testing';

import { AdminReservationsService } from './admin-reservations.service';

describe('AdminReservationsService', () => {
  let service: AdminReservationsService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AdminReservationsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
