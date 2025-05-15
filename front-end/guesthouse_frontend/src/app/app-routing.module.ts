import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
   { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule)
  },
  {
    path: 'user',
    loadChildren: () => import('./user/user.module').then(m => m.UserModule)
  }
];


//   The first route is a redirect: when the user navigates to the root path ('') of the application, they are automatically redirected to 'auth/login'. The pathMatch: 'full' property ensures that this redirect only happens when the entire URL matches the empty string, preventing partial matches.

//The next three routes use Angular's lazy loading feature. Each route specifies a path ('auth', 'admin', or 'user') and uses the loadChildren property to dynamically import the corresponding module only when that route is accessed. For example, navigating to '/auth' will load the AuthModule from './auth/auth.module'. This approach improves the application's performance by splitting the code into smaller bundles that are loaded on demand.

//Overall, this routing setup helps organize the
 

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
