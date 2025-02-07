
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MegaMenuItem } from 'primeng/api';
import { MegaMenu } from 'primeng/megamenu';

@Component({
  selector: 'app-menu',
  templateUrl: './app-menu.component.html',
  styleUrls: ['./app-menu.component.css'],
  standalone: true,
  imports: [MegaMenu]
})
export class AppMenuComponent {
  menuItems: MegaMenuItem[];

  constructor(private router: Router) {
    this.menuItems = [
      { label: 'Main Page', icon: 'pi pi-home', command: () => this.navigateTo('/') },
      { label: 'Users', icon: 'pi pi-users', command: () => this.navigateTo('/users') },
      {
        label: 'Logout',
        icon: 'pi pi-sign-out',
        command: () => this.logout(),
        styleClass: 'logout-button'
      }
    ];
  }

  navigateTo(route: string) {
    this.router.navigate([route]);
  }

  logout() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.router.navigate(['/login']);
  }
}