import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { User, UserCreateRequest } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  loading = true;
  error = '';
  successMsg = '';
  readonly skeletons = Array(5).fill(0);

  showCreateModal = false;
  creating = false;
  createForm: UserCreateRequest = { username: '', password: '', email: '', role: 'PRODUCT_MANAGER', fullName: '', phone: '' };

  showBlockModal = false;
  blockingUserId: number | null = null;
  blockReason = '';
  blocking = false;

  showRoleModal = false;
  roleUserId: number | null = null;
  newRole: 'ADMIN' | 'PRODUCT_MANAGER' = 'PRODUCT_MANAGER';

  constructor(private api: ApiService, private auth: AuthService, private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.loadUsers();
    this.route.queryParams.subscribe(params => {
      if (params['create']) {
        this.showCreateModal = true;
      }
    });
  }

  loadUsers(): void {
    this.loading = true;
    this.api.getUsers().subscribe({
      next: (data) => { this.users = data; this.loading = false; },
      error: ()    => { this.loading = false; }
    });
  }

  createUser(): void {
    this.creating = true;
    this.api.createUser(this.createForm).subscribe({
      next: () => {
        this.showCreateModal = false;
        this.successMsg = `User "${this.createForm.username}" created successfully!`;
        this.createForm = { username: '', password: '', email: '', role: 'PRODUCT_MANAGER', fullName: '', phone: '' };
        this.loadUsers();
        this.creating = false;
        setTimeout(() => this.successMsg = '', 5000);
      },
      error: (e) => { this.error = e.error?.message || 'Failed to create user.'; this.creating = false; }
    });
  }

  openBlockModal(userId: number): void {
    this.blockingUserId = userId;
    this.blockReason = '';
    this.showBlockModal = true;
  }

  confirmBlock(): void {
    if (!this.blockReason.trim()) { this.error = 'Block reason is required.'; return; }
    this.blocking = true;
    this.api.blockUser(this.blockingUserId!, this.blockReason).subscribe({
      next: () => { this.showBlockModal = false; this.loadUsers(); this.blocking = false; },
      error: (e) => { this.error = e.error?.message || 'Block failed.'; this.blocking = false; }
    });
  }

  unblock(userId: number): void {
    this.api.unblockUser(userId).subscribe({
      next: () => { this.loadUsers(); this.successMsg = 'User unblocked.'; setTimeout(() => this.successMsg = '', 3000); },
      error: (e) => { this.error = e.error?.message || 'Unblock failed.'; }
    });
  }

  deactivate(userId: number): void {
    if (!confirm('Deactivate this user? This cannot be undone.')) return;
    this.api.deactivateUser(userId).subscribe({
      next: () => { this.loadUsers(); this.successMsg = 'User deactivated.'; setTimeout(() => this.successMsg = '', 3000); },
      error: (e) => { this.error = e.error?.message || 'Deactivation failed.'; }
    });
  }

  resetPassword(userId: number): void {
    this.api.resetUserPassword(userId).subscribe({
      next: () => { this.successMsg = 'Password reset email sent.'; setTimeout(() => this.successMsg = '', 5000); },
      error: (e) => { this.error = e.error?.message || 'Reset failed.'; }
    });
  }

  openRoleModal(user: User): void {
    this.roleUserId = user.id;
    this.newRole = user.role === 'ADMIN' ? 'PRODUCT_MANAGER' : 'ADMIN';
    this.showRoleModal = true;
  }

  confirmChangeRole(): void {
    this.api.changeUserRole(this.roleUserId!, this.newRole).subscribe({
      next: () => { this.showRoleModal = false; this.loadUsers(); this.successMsg = 'Role updated.'; setTimeout(() => this.successMsg = '', 3000); },
      error: (e) => { this.error = e.error?.message || 'Role change failed.'; }
    });
  }

  isSelf(userId: number): boolean { return this.auth.getCurrentUser()?.userId === userId; }
}
