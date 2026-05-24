import { Routes } from '@angular/router';
import { managerGuard, adminGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  {
    path: 'home',
    data: { tabIndex: 0 },
    loadComponent: () => import('./features/customer/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'search',
    data: { tabIndex: 1 },
    loadComponent: () => import('./features/customer/search/search.component').then(m => m.SearchComponent)
  },
  {
    path: 'product/:id',
    loadComponent: () => import('./features/customer/product-detail/product-detail.component').then(m => m.ProductDetailComponent)
  },
  {
    path: 'cart',
    loadComponent: () => import('./features/customer/cart/cart.component').then(m => m.CartComponent)
  },
  {
    path: 'checkout',
    loadComponent: () => import('./features/customer/checkout/checkout.component').then(m => m.CheckoutComponent)
  },
  {
    path: 'payment',
    loadComponent: () => import('./features/customer/payment/payment.component').then(m => m.PaymentComponent)
  },
  {
    path: 'order',
    data: { tabIndex: 2 },
    loadComponent: () => import('./features/customer/order-list/order-list.component').then(m => m.OrderListComponent)
  },
  {
    path: 'order/:id',
    loadComponent: () => import('./features/customer/order-detail/order-detail.component').then(m => m.OrderDetailComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },

  {
    path: 'manager',
    canActivate: [managerGuard],
    loadComponent: () =>
      import('./features/manager/manager-shell/manager-shell.component').then(m => m.ManagerShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/manager/dashboard/dashboard.component').then(m => m.ManagerDashboardComponent)
      },
      {
        path: 'products',
        loadComponent: () =>
          import('./features/manager/product-management/product-management.component').then(m => m.ProductManagementComponent)
      },
      {
        path: 'product-form',
        loadComponent: () =>
          import('./features/manager/product-form/product-form.component').then(m => m.ProductFormComponent)
      },
      {
        path: 'product-form/:id',
        loadComponent: () =>
          import('./features/manager/product-form/product-form.component').then(m => m.ProductFormComponent)
      },
      {
        path: 'orders',
        loadComponent: () =>
          import('./features/manager/order-processing/order-processing.component').then(m => m.OrderProcessingComponent)
      },
      {
        path: 'stock-history',
        loadComponent: () =>
          import('./features/manager/stock-history/stock-history.component').then(m => m.StockHistoryComponent)
      },
      {
        path: 'history',
        loadComponent: () =>
          import('./features/manager/history/history.component').then(m => m.HistoryComponent)
      }
    ]
  },

  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./features/admin/admin-shell/admin-shell.component').then(m => m.AdminShellComponent),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/admin/dashboard/dashboard.component').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'users',
        loadComponent: () =>
          import('./features/admin/user-management/user-management.component').then(m => m.UserManagementComponent)
      }
    ]
  },

  { path: '**', redirectTo: 'home' }
];
