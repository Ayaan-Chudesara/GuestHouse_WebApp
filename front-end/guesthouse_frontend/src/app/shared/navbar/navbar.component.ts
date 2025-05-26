import { Component } from '@angular/core';
import { AuthService } from 'src/app/auth/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
 isAdmin = false;
  isUser = false;

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    const role = this.authService.getRoleFromToken(localStorage.getItem('jwt_token') || '');
    this.isAdmin = role === 'ADMIN';
    this.isUser = role === 'USER';
       console.log('Navbar Subscribe: Role=', role, 'IsAdmin=', this.isAdmin, 'IsUser=', this.isUser);
  
  }

  logout(): void {
    localStorage.removeItem('token');
    window.location.href = '/auth/login';
  }
}
