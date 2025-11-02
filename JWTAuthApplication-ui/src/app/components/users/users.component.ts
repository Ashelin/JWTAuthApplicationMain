import { Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Table, TableModule } from 'primeng/table';
import { FormsModule } from '@angular/forms';
import { FieldsetModule } from 'primeng/fieldset';
import { InputTextModule } from 'primeng/inputtext';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { CommonModule } from '@angular/common';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../services/auth.service';
import { UserRole } from '../../models/user-role.enum';

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
    CommonModule,
    DropdownModule
  ],
  providers: [MessageService, ConfirmationService]
})
export class UsersComponent implements OnInit {
  @ViewChild('dt') dt: Table | undefined;
  users: any[] = [];
  newUser = { firstName: '', lastName: '', email: '', password: '' };
  editingRow: number | null = null;
  originalUserData: any = {};
  roles: string[] = [];
  isAdmin: boolean = false;

  constructor(
    private readonly userService: UserService,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly messageService: MessageService,
    private readonly confirmationService: ConfirmationService
  ) {}

  ngOnInit(): void {
    this.loadRoles();
    this.loadUsers();
  }

  loadRoles(): void {
    this.userService.getAvailableRoles().subscribe({
      next: (roles) => {
        if (Array.isArray(roles) && roles.length > 0) {
          this.roles = roles;
        } else {
          console.warn('Empty or invalid roles received, using fallback');
          this.roles = Object.values(UserRole);
        }
      },
      error: (error) => {
        console.error('Error loading roles:', error);
        this.roles = Object.values(UserRole);
      }
    });
  }

  loadUsers() {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      console.error('User is not authorized');
      this.router.navigate(['/login']);
      return;
    }

    this.userService.searchUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.updateCurrentUserRole();
      },
      error: (error) => {
        console.error('User Request Error:', error);
        if (error.status === 403) {
          this.router.navigate(['/login']);
        }
      }
    });
  }

  private updateCurrentUserRole(): void {
    const currentUserEmail = this.authService.getCurrentUserEmail();
    if (currentUserEmail) {
      const currentUser = this.users.find(user => user.email === currentUserEmail);
      if (currentUser) {
        this.isAdmin = currentUser.userRole === UserRole.ADMIN;
      }
    } else {
      this.isAdmin = this.authService.isAdmin();
    }
  }

  onSubmit() {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    this.userService.createUser(this.newUser).subscribe({
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

  updateUser(user: any): void {
    const token = localStorage.getItem('accessToken');
    if (!token) return;

    const updateData = { firstName: user.firstName, lastName: user.lastName, email: user.email };

    this.userService.updateUser(user.id, updateData).subscribe({
      next: () => this.handleUserUpdateSuccess(user),
      error: (err) => this.handleUserUpdateError(err)
    });
  }

  private handleUserUpdateSuccess(user: any): void {
    const hasRoleChanged = this.hasRoleChanged(user);

    if (hasRoleChanged && this.isAdmin) {
      this.updateUserRole(user);
    } else {
      this.finishUserUpdate('User updated');
    }
  }

  private hasRoleChanged(user: any): boolean {
    const originalRole = this.originalUserData?.userRole;
    return user.userRole && user.userRole !== originalRole;
  }

  private updateUserRole(user: any): void {
    if (!this.isValidRole(user.userRole)) {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Invalid role selected',
        life: 3000
      });
      return;
    }

    this.userService.updateUserRole(user.id, user.userRole).subscribe({
      next: () => this.finishUserUpdate('User and role updated'),
      error: (err) => this.handleRoleUpdateError(err)
    });
  }

  private isValidRole(role: string): boolean {
    if (!role) {
      return false;
    }
    return this.roles.includes(role) || Object.values(UserRole).includes(role as UserRole);
  }

  private finishUserUpdate(detail: string): void {
    this.messageService.add({
      severity: 'success',
      summary: 'Updated',
      detail: detail,
      life: 3000
    });
    this.editingRow = null;
    this.loadUsers();
  }

  private handleUserUpdateError(err: any): void {
    if (err.status === 403) {
      this.messageService.add({
        severity: 'error',
        summary: 'Unauthorized',
        detail: 'You are not allowed to change roles',
        life: 3000
      });
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to update user',
        life: 3000
      });
    }
  }

  private handleRoleUpdateError(err: any): void {
    if (err.status === 403) {
      this.messageService.add({
        severity: 'error',
        summary: 'Unauthorized',
        detail: 'You are not allowed to change roles',
        life: 3000
      });
    } else {
      this.messageService.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to change role',
        life: 3000
      });
    }
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

    this.userService.deleteUser(userId).subscribe({
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
