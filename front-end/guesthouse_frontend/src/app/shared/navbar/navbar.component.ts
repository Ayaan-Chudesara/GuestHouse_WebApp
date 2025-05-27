import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subscription, filter } from 'rxjs';
import { AuthService } from 'src/app/auth/auth.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
//  isAdmin = false;
//   isUser = false;

//   constructor(private authService: AuthService) {}

//   ngOnInit(): void {
//     const role = this.authService.getRoleFromToken(localStorage.getItem('jwt_token') || '');
//     this.isAdmin = role === 'ADMIN';
//     this.isUser = role === 'USER';
//        console.log('Navbar Subscribe: Role=', role, 'IsAdmin=', this.isAdmin, 'IsUser=', this.isUser);
  
//   }

//   logout(): void {
//     localStorage.removeItem('token');
//     window.location.href = '/auth/login';
//   }
  public isSidebarOpen: boolean = false; // Changed to public for template access if needed, though direct access isn't strictly necessary with just toggle/close methods
  public isAdmin: boolean = false; // Make public for template use
  public isUser: boolean = false;   // Make public for template use

  private userRoleSubscription: Subscription | undefined;
  private routerEventsSubscription: Subscription | undefined;

  constructor(public authService: AuthService, private router: Router) {
      // Close sidebar on route change
      this.routerEventsSubscription = this.router.events.pipe(
          filter(event => event instanceof NavigationEnd)
      ).subscribe(() => {
          this.closeSidebar(); // Always close on navigation, regardless of screen size
      });
  }

  ngOnInit(): void {
  console.log('NavbarComponent ngOnInit - Initial state:');
    console.log('  isLoggedIn (AuthService):', this.authService.isLoggedIn());
    console.log('  Current User Role (AuthService):', this.authService.getCurrentUserRole);
    console.log('  isAdmin (local):', this.isAdmin);
    console.log('  isUser (local):', this.isUser);


    this.userRoleSubscription = this.authService.userRole$.subscribe(role => {
      this.isAdmin = (role === 'ADMIN');
      this.isUser = (role === 'USER');

      console.log('NavbarComponent userRole$ subscription updated:');
      console.log('  New Role:', role);
      console.log('  isAdmin (updated):', this.isAdmin);
      console.log('  isUser (updated):', this.isUser);
      // No need for initializeSidebarState to check screen size anymore
      // Sidebar will always start closed.
      this.isSidebarOpen = false;
      this.handleBodyOverflow(); // Adjust body overflow based on initial state
    });
    // Ensure initial state is closed even if no role change happens immediately
    this.isSidebarOpen = false;
    this.handleBodyOverflow();
  }

  ngOnDestroy(): void {
    if (this.userRoleSubscription) {
      this.userRoleSubscription.unsubscribe();
    }
    if (this.routerEventsSubscription) {
      this.routerEventsSubscription.unsubscribe();
    }
  }

  // Simplified: Sidebar always starts closed.
  // private initializeSidebarState(): void {
  //     this.isSidebarOpen = false;
  //     this.handleBodyOverflow();
  // }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    this.handleBodyOverflow();
  }

  closeSidebar(): void {
    this.isSidebarOpen = false;
    this.handleBodyOverflow();
  }

  // Removed isSidebarOpenDesktop() as it's no longer needed for sidebar logic.

  // Handle body overflow to prevent background scrolling when sidebar is open
  private handleBodyOverflow(): void {
    // Apply overflow: hidden only when the sidebar is open
    if (this.isSidebarOpen) {
        document.body.classList.add('sidebar-open');
    } else {
        document.body.classList.remove('sidebar-open');
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
    this.closeSidebar(); // Ensure sidebar closes on logout
  }

  @HostListener('document:keydown.escape', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (this.isSidebarOpen) {
      this.closeSidebar();
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    // Only re-evaluate body overflow, no need to re-initialize sidebar state based on size
    this.handleBodyOverflow();
    // If you want it to close on large resize, you can add this:
    // if (window.innerWidth >= 993) { // Example: close sidebar if resized to desktop view
    //     this.closeSidebar();
    // }
  }
}
