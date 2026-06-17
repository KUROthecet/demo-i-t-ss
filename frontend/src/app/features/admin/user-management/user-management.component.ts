import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink, Params } from '@angular/router';
import { UserApiService } from '../../../core/services/user-api.service';
import { MediaApiService } from '../../../core/services/media-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { User, UserCreateRequest, UserRole } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.scss'
})
export class UserManagementComponent implements OnInit {
  users: User[]  = [];
  loading        = true;
  error          = '';
  successMsg     = '';
  readonly skeletons = Array(5).fill(0);

  showCreateModal = false;
  creating        = false;
  uploadingAvatar = false;
  createForm: UserCreateRequest = { username: '', password: '', email: '', roles: ['PRODUCT_MANAGER'], fullName: '', phone: '', avatarUrl: '' };

  showBlockModal   = false;
  blockingUserId: number | null = null;
  blockReason      = '';
  blocking         = false;

  showRoleModal    = false;
  roleUserId: number | null = null;
  selectedRoles: UserRole[] = [];

  private static readonly EMPTY_CREATE_FORM: UserCreateRequest = {
    username: '', password: '', email: '', roles: ['PRODUCT_MANAGER'], fullName: '', phone: '', avatarUrl: ''
  };

  constructor(
    private readonly userApi:   UserApiService,
    private readonly mediaApi:  MediaApiService,
    private readonly auth:      AuthService,
    private readonly route:     ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.route.queryParams.subscribe(this.onQueryParamsLoaded.bind(this));
  }

  private onQueryParamsLoaded(params: Params): void {
    if (params['create']) this.showCreateModal = true;
  }

  loadUsers(): void {
    this.loading = true;
    this.userApi.getUsers().subscribe({
      next:  this.onUsersLoaded.bind(this),
      error: this.onUsersError.bind(this)
    });
  }

  private onUsersLoaded(data: User[]): void {
    this.users   = data;
    this.loading = false;
  }

  private onUsersError(): void {
    this.loading = false;
  }

  private clearSuccessMsg(): void {
    this.successMsg = '';
  }

  createUser(): void {
    if (this.createForm.roles.length === 0) {
      this.error = 'Select at least one role.';
      return;
    }
    this.creating = true;
    this.userApi.createUser(this.createForm).subscribe({
      next:  this.onUserCreated.bind(this),
      error: this.onCreateError.bind(this)
    });
  }

  private onUserCreated(): void {
    this.showCreateModal = false;
    this.successMsg      = `User "${this.createForm.username}" created successfully!`;
    this.createForm      = { ...UserManagementComponent.EMPTY_CREATE_FORM };
    this.loadUsers();
    this.creating        = false;
    setTimeout(this.clearSuccessMsg.bind(this), 5000);
  }

  private onCreateError(e: any): void {
    this.error    = e.error?.message || 'Failed to create user.';
    this.creating = false;
  }

  onAvatarSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    this.uploadingAvatar = true;
    const formData = new FormData();
    formData.append('file', file);

    this.mediaApi.uploadImage(formData).subscribe({
      next:  this.onAvatarUploaded.bind(this),
      error: this.onAvatarUploadError.bind(this)
    });
  }

  private onAvatarUploaded(res: {url: string}): void {
    this.createForm.avatarUrl = res.url;
    this.uploadingAvatar      = false;
  }

  private onAvatarUploadError(): void {
    this.error           = 'Failed to upload avatar.';
    this.uploadingAvatar = false;
  }

  openBlockModal(userId: number): void {
    this.blockingUserId = userId;
    this.blockReason    = '';
    this.showBlockModal = true;
  }

  confirmBlock(): void {
    if (!this.blockReason.trim()) { this.error = 'Block reason is required.'; return; }
    this.blocking = true;
    this.userApi.blockUser(this.blockingUserId!, this.blockReason).subscribe({
      next:  this.onBlockSuccess.bind(this),
      error: this.onBlockError.bind(this)
    });
  }

  private onBlockSuccess(): void {
    this.showBlockModal = false;
    this.loadUsers();
    this.blocking = false;
  }

  private onBlockError(e: any): void {
    this.error    = e.error?.message || 'Block failed.';
    this.blocking = false;
  }

  unblock(userId: number): void {
    this.userApi.unblockUser(userId).subscribe({
      next:  this.onUnblockSuccess.bind(this),
      error: this.onUnblockError.bind(this)
    });
  }

  private onUnblockSuccess(): void {
    this.loadUsers();
    this.successMsg = 'User unblocked.';
    setTimeout(this.clearSuccessMsg.bind(this), 3000);
  }

  private onUnblockError(e: any): void {
    this.error = e.error?.message || 'Unblock failed.';
  }

  deactivate(userId: number): void {
    if (!confirm('Deactivate this user? This cannot be undone.')) return;
    this.userApi.deactivateUser(userId).subscribe({
      next:  this.onDeactivateSuccess.bind(this),
      error: this.onDeactivateError.bind(this)
    });
  }

  private onDeactivateSuccess(): void {
    this.loadUsers();
    this.successMsg = 'User deactivated.';
    setTimeout(this.clearSuccessMsg.bind(this), 3000);
  }

  private onDeactivateError(e: any): void {
    this.error = e.error?.message || 'Deactivation failed.';
  }

  resetPassword(userId: number): void {
    this.userApi.resetUserPassword(userId).subscribe({
      next:  this.onResetPasswordSuccess.bind(this),
      error: this.onResetPasswordError.bind(this)
    });
  }

  private onResetPasswordSuccess(): void {
    this.successMsg = 'Password reset email sent.';
    setTimeout(this.clearSuccessMsg.bind(this), 5000);
  }

  private onResetPasswordError(e: any): void {
    this.error = e.error?.message || 'Reset failed.';
  }

  openRoleModal(user: User): void {
    this.roleUserId    = user.id;
    this.selectedRoles = [...user.roles];
    this.showRoleModal = true;
  }

  toggleSelectedRole(role: UserRole): void {
    const idx = this.selectedRoles.indexOf(role);
    if (idx === -1) {
      this.selectedRoles.push(role);
    } else {
      this.selectedRoles.splice(idx, 1);
    }
  }

  toggleCreateRole(role: UserRole): void {
    const idx = this.createForm.roles.indexOf(role);
    if (idx === -1) {
      this.createForm.roles.push(role);
    } else {
      this.createForm.roles.splice(idx, 1);
    }
  }

  confirmChangeRole(): void {
    if (this.selectedRoles.length === 0) {
      this.error = 'Select at least one role.';
      return;
    }
    this.userApi.updateUserRoles(this.roleUserId!, this.selectedRoles).subscribe({
      next:  this.onRoleChanged.bind(this),
      error: this.onRoleChangeError.bind(this)
    });
  }

  private onRoleChanged(): void {
    this.showRoleModal = false;
    this.loadUsers();
    this.successMsg = 'Role updated.';
    setTimeout(this.clearSuccessMsg.bind(this), 3000);
  }

  private onRoleChangeError(e: any): void {
    this.error = e.error?.message || 'Role change failed.';
  }

  isSelf(userId: number): boolean { return this.auth.getCurrentUser()?.userId === userId; }
}
