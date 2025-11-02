import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/user`;

  constructor(private readonly http: HttpClient) {}

  private getAuthHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    return new HttpHeaders({ 'Authorization': `Bearer ${token}` });
  }

  searchUsers(): Observable<any[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<any[]>(`${this.apiUrl}/search`, { headers });
  }

  createUser(userData: { firstName: string; lastName: string; email: string; password: string }): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.post(`${this.apiUrl}/create`, userData, { headers });
  }

  updateUser(userId: number, updateData: { firstName: string; lastName: string; email: string }): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.patch(`${this.apiUrl}/update/${userId}`, updateData, { headers });
  }

  updateUserRole(userId: number, role: string): Observable<any> {
    const headers = this.getAuthHeaders();
    const rolePayload = { userRole: role };
    return this.http.patch(`${this.apiUrl}/role/${userId}`, rolePayload, { headers });
  }

  deleteUser(userId: number): Observable<any> {
    const headers = this.getAuthHeaders();
    return this.http.delete(`${this.apiUrl}/delete/${userId}`, { headers });
  }

  getAvailableRoles(): Observable<string[]> {
    const headers = this.getAuthHeaders();
    return this.http.get<string[]>(`${this.apiUrl}/roles`, { headers });
  }
}

