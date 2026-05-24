import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Order } from '../../../core/models/order.model';
import { NavbarComponent } from '../../../shared/navbar/navbar.component';
import { VndCurrencyPipe } from '../../../shared/pipes/vnd-currency.pipe';

@Component({
  selector: 'app-payment',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent, VndCurrencyPipe],
  templateUrl: './payment.component.html',
  styleUrl: './payment.component.scss'
})
export class PaymentComponent implements OnInit {
  protected order: Order | null = null;
  protected paymentMethod = '';

  constructor(private readonly router: Router) {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as { order: Order; paymentMethod: string } | undefined;
    if (state?.order) {
      this.order         = state.order;
      this.paymentMethod = state.paymentMethod;
    }
  }

  ngOnInit(): void {
    if (!this.order) {
      this.router.navigate(['/home']);
    }
  }
}
