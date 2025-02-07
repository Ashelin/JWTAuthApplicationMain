import { Component } from '@angular/core';
import { Router, RouterModule, RouterOutlet } from '@angular/router';
import { AppMenuComponent } from "./app-menu/app-menu.component";
import { CommonModule } from '@angular/common';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterModule, AppMenuComponent, CommonModule, ToastModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
  providers: [MessageService]
})
export class AppComponent {
  title = 'JWTAuthApplication-ui';
  constructor(private router: Router) {}

  shouldShowMenu(): boolean {
    return !['/login', '/register'].includes(this.router.url);
  }
}
