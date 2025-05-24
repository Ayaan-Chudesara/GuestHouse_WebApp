import { Component } from '@angular/core';
import { AuthService } from '../auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {

  name = '';
  email = '';
  phone = '';
  password = '';
  confirmPassword = '';
  passwordMismatch = false;

  constructor(private authService: AuthService, private router: Router) {}

  register() {
    if (this.password !== this.confirmPassword) {
      this.passwordMismatch = true;
      return;
    }

    const user = {
      name: this.name,
      email: this.email,
      phone: this.phone,
      password: this.password
    };

    this.authService.register(user).subscribe({
      next: () => {
        alert('Registration successful!');
        this.router.navigate(['/auth/login']);
      },
      error: err => {
        alert('Registration failed. Please try again.');
        console.error(err);
      }
    });
  }

}
