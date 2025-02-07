import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { CardModule } from 'primeng/card';
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [FormsModule, InputTextModule, ButtonModule, CardModule, RouterModule, ToastModule],
  providers: [MessageService]
})
export class LoginComponent {
  email: string = '';
  password: string = '';

  constructor(private authService: AuthService, private router: Router, private messageService: MessageService) {}

  login() {
    this.authService.login(this.email, this.password).subscribe(response => {
      this.authService.saveTokens(response.token, response.refreshToken);
      this.router.navigate(['/']);
    }, error => {
      console.error('Login error', error);
      this.messageService.add({ severity: 'error', summary: 'Login Error', detail: 'Invalid email or password', life: 3000 });
    });
  }
}
