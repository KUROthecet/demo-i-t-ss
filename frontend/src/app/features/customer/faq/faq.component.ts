import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

export class FaqItem {
  public q: string;
  public a: string;
  public open: boolean;

  constructor(q: string, a: string, open: boolean) {
    this.q = q;
    this.a = a;
    this.open = open;
  }
}

export class FaqSection {
  public title: string;
  public items: FaqItem[];

  constructor(title: string, items: FaqItem[]) {
    this.title = title;
    this.items = items;
  }
}

@Component({
  selector: 'app-faq',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.scss'
})
export class FaqComponent {
  public sections: FaqSection[];

  constructor() {
    this.sections = [
      new FaqSection('Orders & Payment', [
        new FaqItem(
          'Do I need an account to buy?',
          'No. AIMS is designed for customers who want to browse and purchase without registering. Simply add items to your cart and proceed to checkout. You will receive order updates via the email you provide at checkout.',
          true
        ),
        new FaqItem(
          'What payment methods do you accept?',
          'We currently accept VietQR (scan to pay via your banking app) and PayPal / credit card via the PayPal Sandbox gateway. Additional payment methods may be added in future releases.',
          false
        ),
        new FaqItem(
          'Can I cancel my order?',
          'Yes, as long as your order is still in the pending review state. Once a product manager has approved it, cancellation is no longer possible. Access your order via the Orders page and confirm the cancellation there.',
          false
        ),
        new FaqItem(
          'How is the price range determined?',
          'All prices are set by our product managers and must fall between 30% and 150% of the product\'s original cost price. This range is enforced by the system to maintain fair pricing.',
          false
        )
      ]),
      new FaqSection('Shipping', [
        new FaqItem(
          'How are shipping fees calculated?',
          'Shipping is based on total order weight and your delivery province. Hanoi and Ho Chi Minh City start at 22,000 VND for the first 3 kg. Other provinces start at 30,000 VND for the first 0.5 kg. Orders above 100,000 VND receive a discount of up to 25,000 VND.',
          false
        ),
        new FaqItem(
          'What is rush delivery?',
          'Rush delivery is a same-day or expedited option available for eligible products in Hanoi and Ho Chi Minh City. A flat surcharge of 30,000 VND applies. You can select your preferred delivery time at checkout, and the product page will indicate whether rush delivery is available.',
          false
        )
      ]),
      new FaqSection('Returns', [
        new FaqItem(
          'How does the refund process work?',
          'Refunds depend on your payment method. PayPal payments are refunded automatically when an order is cancelled or rejected. VietQR payments require manual processing — our team will contact you to arrange the transfer outside the system.',
          false
        )
      ]),
      new FaqSection('Account', [
        new FaqItem(
          'How do I track my order?',
          'Visit the Orders page on this website and enter the email address you used at checkout. All orders placed with that address will appear, along with their current status.',
          false
        ),
        new FaqItem(
          'What products do you sell?',
          'AIMS specialises in physical media: books, CDs, DVDs, and newspapers. Each product type has its own specific fields — for example, books list author and publisher, CDs list artist and track list, and DVDs list director and runtime.',
          false
        ),
        new FaqItem(
          'I\'m a staff member. How do I log in?',
          'Use the Staff Login button in the top-right corner of the site. Administrators and product managers each have their own dashboard accessible after authentication.',
          false
        )
      ])
    ];
  }

  public toggle(section: FaqSection, item: FaqItem): void {
    item.open = !item.open;
  }
}
