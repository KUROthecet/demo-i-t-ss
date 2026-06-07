import { Component, computed, HostListener, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { RouterLink, RouterLinkActive, Router, NavigationEnd } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { filter } from 'rxjs';
import { CartService } from '../../core/services/cart.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule, FormsModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss'
})
export class NavbarComponent implements AfterViewInit {
  searchQuery = '';
  showUserMenu = false;

  @ViewChild('navHome') navHome!: ElementRef;
  @ViewChild('navBrowse') navBrowse!: ElementRef;
  @ViewChild('navOrders') navOrders!: ElementRef;

  indicatorLeft = 0;
  indicatorWidth = 0;

  itemCount   = computed(this.computeItemCount.bind(this));
  currentUser = computed(this.computeCurrentUser.bind(this));

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private router: Router
  ) {
    this.router.events.pipe(
      filter(this.isNavigationEnd.bind(this))
    ).subscribe(this.onNavigationEnd.bind(this));
  }

  private computeItemCount(): number {
    return this.cartService.itemCount();
  }

  private computeCurrentUser(): any {
    return this.authService.getCurrentUser();
  }

  private isNavigationEnd(event: any): boolean {
    return event instanceof NavigationEnd;
  }

  private onNavigationEnd(): void {
    setTimeout(this.updateIndicator.bind(this), 50);
  }

  ngAfterViewInit() {
    setTimeout(this.updateIndicator.bind(this), 100);
  }

  updateIndicator() {
    let activeEl: HTMLElement | null = null;
    if (this.isActive('/home')) activeEl = this.navHome?.nativeElement;
    else if (this.isActive('/search')) activeEl = this.navBrowse?.nativeElement;
    else if (this.isActive('/order')) activeEl = this.navOrders?.nativeElement;

    if (activeEl) {
      this.indicatorLeft = activeEl.offsetLeft;
      this.indicatorWidth = activeEl.offsetWidth;
    } else {
      this.indicatorWidth = 0;
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.navbar__user')) {
      this.showUserMenu = false;
    }
  }

  toggleUserMenu(event: Event): void {
    event.stopPropagation();
    this.showUserMenu = !this.showUserMenu;
  }

  getUserInitial(): string {
    const user = this.currentUser();
    if (!user) return 'U';
    return (user.fullName || user.username || 'U').charAt(0).toUpperCase();
  }

  isActive(path: string): boolean {
    return this.router.url.startsWith(path);
  }

  search(): void {
    if (this.searchQuery.trim()) {
      this.router.navigate(['/search'], { queryParams: { q: this.searchQuery.trim() } });
    }
  }

  onSearchKey(event: KeyboardEvent): void {
    if (event.key === 'Enter') this.search();
  }

  logout(): void {
    this.authService.logout();
    this.showUserMenu = false;
    this.router.navigate(['/home']);
  }

  goToDashboard(): void {
    const role = this.currentUser()?.role;
    this.showUserMenu = false;
    if (role === 'ADMIN') this.router.navigate(['/admin/dashboard']);
    else if (role === 'PRODUCT_MANAGER') this.router.navigate(['/manager/dashboard']);
  }
}
