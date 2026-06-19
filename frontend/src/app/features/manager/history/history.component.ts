import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MediaApiService } from '../../../core/services/media-api.service';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './history.component.html',
  styleUrl: './history.component.scss'
})
export class HistoryComponent implements OnInit {
  protected allLogs: any[]      = [];
  protected loading             = true;
  protected filterAction        = '';
  protected filterBarcode       = '';
  protected filterStartDate     = '';
  protected filterEndDate       = '';
  readonly skeletons = Array(8).fill(0);

  constructor(private readonly mediaApi: MediaApiService) {}

  ngOnInit(): void {
    this.mediaApi.getHistoryLogs().subscribe({
      next:  this.onLogsLoaded.bind(this),
      error: this.onLogsError.bind(this)
    });
  }

  private onLogsLoaded(data: any): void {
    this.allLogs = data;
    this.loading = false;
  }

  private onLogsError(): void {
    this.loading = false;
  }

  private matchesFilter(log: any): boolean {
    if (this.filterAction    && !log.actionType?.includes(this.filterAction))                                          return false;
    if (this.filterBarcode   && !log.productBarcode?.toLowerCase().includes(this.filterBarcode.toLowerCase()))         return false;
    if (this.filterStartDate && new Date(log.createdAt) < new Date(this.filterStartDate))                             return false;
    if (this.filterEndDate   && new Date(log.createdAt) > new Date(this.filterEndDate + 'T23:59:59'))                 return false;
    return true;
  }

  protected get filteredLogs(): any[] {
    const result: any[] = [];
    for (const log of this.allLogs) {
      if (this.matchesFilter(log)) {
        result.push(log);
      }
    }
    return result;
  }

  protected clearFilters(): void {
    this.filterAction    = '';
    this.filterBarcode   = '';
    this.filterStartDate = '';
    this.filterEndDate   = '';
  }

  protected formatDate(d: string): string {
    if (!d) return '—';
    return new Date(d).toLocaleString('vi-VN');
  }

  protected getActionClass(actionType: string): string {
    if (actionType?.includes('ADD'))        return 'action-badge--add';
    if (actionType?.includes('UPDATE'))     return 'action-badge--update';
    if (actionType?.includes('DELETE'))     return 'action-badge--delete';
    if (actionType?.includes('DEACTIVATE')) return 'action-badge--deactivate';
    return '';
  }
}
