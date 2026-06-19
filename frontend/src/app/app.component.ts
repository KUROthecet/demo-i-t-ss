import { Component, inject } from '@angular/core';
import { RouterOutlet, Router, NavigationEnd, NavigationStart } from '@angular/router';
import { ScrollToTopComponent } from './shared/scroll-to-top/scroll-to-top.component';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { FooterComponent } from './shared/footer/footer.component';
import { AmbientBackgroundComponent } from './shared/ambient-background/ambient-background.component';
import { trigger, transition, style, query, group, animate } from '@angular/animations';

export const slideInAnimation = trigger('routeAnimations', [
  transition(':increment', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({ position: 'absolute', top: 0, left: 0, width: '100%', opacity: 1 })
    ], { optional: true }),
    query(':enter', [style({ transform: 'translateX(50px)', opacity: 0 })], { optional: true }),
    group([
      query(':leave', [
        animate('350ms cubic-bezier(0.34, 1.56, 0.64, 1)', style({ transform: 'translateX(-50px)', opacity: 0 }))
      ], { optional: true }),
      query(':enter', [
        animate('350ms cubic-bezier(0.34, 1.56, 0.64, 1)', style({ transform: 'translateX(0)', opacity: 1 }))
      ], { optional: true })
    ])
  ]),
  transition(':decrement', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({ position: 'absolute', top: 0, left: 0, width: '100%', opacity: 1 })
    ], { optional: true }),
    query(':enter', [style({ transform: 'translateX(-50px)', opacity: 0 })], { optional: true }),
    group([
      query(':leave', [
        animate('350ms cubic-bezier(0.34, 1.56, 0.64, 1)', style({ transform: 'translateX(50px)', opacity: 0 }))
      ], { optional: true }),
      query(':enter', [
        animate('350ms cubic-bezier(0.34, 1.56, 0.64, 1)', style({ transform: 'translateX(0)', opacity: 1 }))
      ], { optional: true })
    ])
  ]),
  transition('* <=> *', [
    style({ position: 'relative' }),
    query(':enter, :leave', [
      style({ position: 'absolute', top: 0, left: 0, width: '100%' })
    ], { optional: true }),
    group([
      query(':leave', [
        animate('200ms ease-out', style({ opacity: 0 }))
      ], { optional: true }),
      query(':enter', [
        style({ opacity: 0, transform: 'translateY(16px)' }),
        animate('380ms cubic-bezier(0.16, 1, 0.3, 1)', style({ opacity: 1, transform: 'translateY(0)' }))
      ], { optional: true })
    ])
  ])
]);

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ScrollToTopComponent, NavbarComponent, FooterComponent, AmbientBackgroundComponent],
  animations: [slideInAnimation],
  template: `
    @if (isCustomerLayout) {
      <app-ambient-background />
      <app-navbar />
      <div [@routeAnimations]="getRouteAnimationData(outlet)" class="customer-layout-wrapper">
        <router-outlet #outlet="outlet" />
      </div>
      <app-footer />
    } @else {
      <router-outlet />
    }
    <app-scroll-to-top />
  `,
  styles: [`
    .customer-layout-wrapper {
      position: relative;
      overflow-x: hidden;
      min-height: calc(100vh - 72px);
    }
  `]
})
export class AppComponent {
  isCustomerLayout: boolean;
  router = inject(Router);

  constructor() {
    this.isCustomerLayout = this.isCustomerPath(window.location.pathname);

    this.router.events.subscribe((event) => {
      if (event instanceof NavigationStart) {
        if (!this.isCustomerPath(event.url)) {
          this.isCustomerLayout = false;
        }
      } else if (event instanceof NavigationEnd) {
        this.isCustomerLayout = this.isCustomerPath(event.urlAfterRedirects);
      }
    });
  }

  private isCustomerPath(url: string): boolean {
    return !url.startsWith('/admin') && !url.startsWith('/manager') && !url.startsWith('/login');
  }

  getRouteAnimationData(outlet: RouterOutlet) {
    if (!outlet?.isActivated) return undefined;
    const tabIndex = outlet.activatedRouteData['tabIndex'];
    if (tabIndex !== undefined) return tabIndex;
    let path = '';
    for (const s of outlet.activatedRoute.snapshot.url) {
      if (path) path += '/';
      path += s.path;
    }
    return path || 'root';
  }
}
