import { Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
import { RegisterComponent } from './components/auth/register/register.component';
import { UsersComponent } from './components/users/users.component';
import { AuthGuard } from './auth.guard';
import { EmptyComponent } from './components/empty-component/empty-component.component';

export const routes: Routes = [
    { path: '', component: EmptyComponent, canActivate: [AuthGuard] },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'users', component: UsersComponent, canActivate: [AuthGuard] },
    { path: '**', redirectTo: '/' }
];