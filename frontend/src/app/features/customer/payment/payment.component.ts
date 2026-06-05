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
  protected order: any | null = null;
  protected paymentMethod = '';

  constructor(private readonly router: Router) {
    const nav = this.router.getCurrentNavigation();
    const state = nav?.extras?.state as { orderData: any} | undefined;
    if (state?.orderData) {
      if (typeof state.orderData === 'string') {
        this.order = JSON.parse(state.orderData);
      } else {
        this.order = state.orderData;
      }
    // MẸO: Dùng dấu phẩy (,) thay vì dấu cộng (+) để console không bị ép kiểu chuỗi
    console.log("Saved order: ", this.order); 
          
    // Bây giờ this.order đã là Object chuẩn, gọi paymentMethod sẽ ra "VIETQR"
    this.paymentMethod = this.order.paymentMethod;
    console.log("Payment method : ", this.paymentMethod);
    }
  }

  ngOnInit(): void {
    if (!this.order) {
      this.router.navigate(['/home']);
    }
  }

  get vietQrLink(): string {
    if (!this.order) return '';
    
    const bankId = 'MB';
    const accountNo = '0975452106';
    const accountName = 'BUI TRUNG HIEU';
    const memo = 'DH' + this.order.id; 
    
    // Using template literals for a clean URL
    return `https://img.vietqr.io/image/${bankId}-${accountNo}-qr_only.png?amount=${this.order.amount}&addInfo=${memo}&accountName=${accountName}`;
  }
}
