import { TestBed } from '@angular/core/testing';

import { GuesthouseService } from './guesthouse.service';

describe('GuesthouseService', () => {
  let service: GuesthouseService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GuesthouseService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
