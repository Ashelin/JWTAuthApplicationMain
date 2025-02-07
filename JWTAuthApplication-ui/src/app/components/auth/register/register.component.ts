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
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['../login/login.component.css'],
  standalone: true,
  imports: [FormsModule, InputTextModule, ButtonModule, CardModule, RouterModule, ToastModule],
  providers: [MessageService]
})
export class RegisterComponent {
  firstName: string = '';
  lastName: string = '';
  email: string = '';
  password: string = '';

  constructor(private authService: AuthService, private router: Router, private messageService: MessageService) {}

  register() {
    this.authService.register(this.firstName, this.lastName, this.email, this.password).subscribe(
      (response: any) => {
        this.authService.saveTokens(response.token, response.refreshToken);
        this.router.navigate(['/users']);
      },
      (error: any) => {
        console.error('Registration error', error);
        this.messageService.add({ severity: 'error', summary: 'Registration Error', detail: 'Failed to register user', life: 3000 });
      }
    );
  }
}
