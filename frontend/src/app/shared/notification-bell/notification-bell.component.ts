import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service';
import { VndCurrencyPipe } from '../pipes/vnd-currency.pipe';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule, RouterLink, VndCurrencyPipe],
  templateUrl: './notification-bell.component.html',
  styleUrl: './notification-bell.component.scss'
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  protected open = false;

  constructor(readonly notifService: NotificationService) {}

  ngOnInit(): void {
    this.notifService.startPolling();
  }

  ngOnDestroy(): void {
    this.notifService.stopPolling();
  }

  protected toggle(): void {
    this.open = !this.open;
  }

  protected markAllRead(): void {
    this.notifService.markAllRead();
  }

  protected formatRelative(dateStr: string): string {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const mins  = Math.floor(diff / 60_000);
    if (mins < 1)  return 'just now';
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24)  return `${hrs}h ago`;
    return `${Math.floor(hrs / 24)}d ago`;
  }

  @HostListener('document:click', ['$event'])
  protected onDocClick(e: MouseEvent): void {
    const el = e.target as HTMLElement;
    if (!el.closest('.notif-bell')) {
      this.open = false;
    }
  }
}
