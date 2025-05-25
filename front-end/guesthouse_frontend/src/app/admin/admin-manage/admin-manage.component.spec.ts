import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminManageComponent } from './admin-manage.component';

describe('AdminManageComponent', () => {
  let component: AdminManageComponent;
  let fixture: ComponentFixture<AdminManageComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AdminManageComponent]
    });
    fixture = TestBed.createComponent(AdminManageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
