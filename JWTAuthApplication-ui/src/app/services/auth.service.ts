import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, catchError, switchMap, throwError } from 'rxjs';
import { environment } from '@env/environment';
import { UserRole } from '../models/user-role.enum';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/authenticate`, { email, password });
  }

  register(firstName: string, lastName: string, email: string, password: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, { firstName, lastName, email, password });
  }

  refreshToken(): Observable<any> {
    const refreshToken = localStorage.getItem('refreshToken');

    if (!refreshToken) {
      this.logout();
      return throwError(() => new Error('missing refresh token!'));
    }

    return this.http.post(`${this.apiUrl}/refresh-token`, { refreshToken }).pipe(
      switchMap((response: any) => {
        this.saveTokens(response.token, response.refreshToken);
        return response.token;
      }),
      catchError(error => {
        this.logout();
        return throwError(() => error);
      })
    );
  }

  saveTokens(accessToken: string, refreshToken: string): void {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    this.router.navigate(['/login']);
  }

  private decodePayload(): any | null {
    const token = localStorage.getItem('accessToken');
    if (!token) return null;

    try {
      const parts = token.split('.');
      if (parts.length < 2) return null;
      return JSON.parse(atob(parts[1]));
    } catch {
      return null;
    }
  }

  getCurrentUserEmail(): string | null {
    const payload = this.decodePayload();
    if (!payload) return null;
    return payload.sub ?? payload.email ?? null;
  }

  getCurrentUserRole(): string | null {
    const payload = this.decodePayload();
    if (!payload) return null;

    const authorities = payload.authorities;
    if (Array.isArray(authorities) && authorities.length > 0 && typeof authorities[0] === 'string') {
      return (authorities[0] as string).replace('ROLE_', '');
    }

    return payload.userRole ?? null;
  }

  isAdmin(): boolean {
    return this.getCurrentUserRole() === UserRole.ADMIN;
  }
}
