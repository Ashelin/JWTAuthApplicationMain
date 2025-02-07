import { Component, OnInit, ViewChild } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Table } from 'primeng/table';
import { FormsModule } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { FieldsetModule } from 'primeng/fieldset';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { environment } from '@env/environment';

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css'],
  standalone: true,
  imports: [
    FormsModule,
    TableModule,
    FieldsetModule,
    InputTextModule,
    ButtonModule,
    ToastModule,
    ConfirmDialogModule,
    CommonModule
  ],
  providers: [MessageService, ConfirmationService]
})
export class UsersComponent implements OnInit {
  @ViewChild('dt') dt: Table | undefined;
  users: any[] = [];
  newUser = { firstName: '', lastName: '', email: '', password: '' };
  editingRow: number | null = null;
  originalUserData: any = {};

  constructor(
    private http: HttpClient,
    private router: Router,
    private messageService: MessageService,
    private confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      console.error('User is not authorized');
      this.router.navigate(['/login']);
      return;
    }

    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.get<any[]>(`${environment.apiUrl}/user/search`, { headers }).subscribe({
      next: (data) => this.users = data,
      error: (error) => {
        console.error('User Request Error:', error);
        if (error.status === 403) {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  onSubmit() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.post(`${environment.apiUrl}/user/create`, this.newUser, { headers }).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Success!', detail: 'User added', life: 3000 });
        this.loadUsers();
        this.newUser = { firstName: '', lastName: '', email: '', password: '' };
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Error!', detail: 'Failed to add a user', life: 3000 });
      }
    });
  }

  editUser(index: number) {
    this.editingRow = index;
    this.originalUserData = { ...this.users[index] };
  }

  cancelEdit() {
    if (this.editingRow !== null) {
      this.users[this.editingRow] = { ...this.originalUserData };
      this.editingRow = null;
    }
  }

  updateUser(user: any) {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    const updateData = { firstName: user.firstName, lastName: user.lastName, email: user.email };

    this.http.patch(`${environment.apiUrl}/user/update/${user.id}`, updateData, { headers }).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Updated', detail: 'User updated', life: 3000 });
        this.editingRow = null;
        this.loadUsers();
      }
    });
  }

  confirmDelete(user: any) {
    this.confirmationService.confirm({
      message: `Are you sure you want to delete the user ${user.firstName} ${user.lastName}?`,
      header: 'Confirming deletion',
      icon: 'pi pi-exclamation-triangle',
      accept: () => this.deleteUser(user.id)
    });
  }

  deleteUser(userId: number) {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    const headers = new HttpHeaders({ 'Authorization': `Bearer ${token}` });

    this.http.delete(`${environment.apiUrl}/user/delete/${userId}`, { headers }).subscribe({
      next: () => {
        this.users = this.users.filter(user => user.id !== userId);
        this.messageService.add({
          severity: 'success',
          summary: 'Deleted',
          detail: 'User was successfully deleted',
          life: 3000
        });
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: 'Error',
          detail: 'Failed to delete user',
          life: 3000
        });
      }
    });
  }

  formatDate(dateString: string): string {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleString('en-EN');
  }
}
