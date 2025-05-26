import { Component } from '@angular/core';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
 email = '';
  password = '';

  constructor(private authService: AuthService, private router: Router) {}

  onLogin() {
    this.authService.login(this.email, this.password).subscribe({
      next: (res) => {
        const token = res.token;
        this.authService.saveToken(token);

         const role = this.authService.getRoleFromToken(token);

        if (role === 'ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else if (role === 'USER') {
          this.router.navigate(['/user/booking']);
        } else {
          alert('Unknown role!');
        }
      },
      error: (err) => {
        alert('Login failed');
        console.error(err);
      }
    });
  }
}
