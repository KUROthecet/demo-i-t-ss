import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.scss'
})
export class FooterComponent {
  newsletterEmail     = '';
  newsletterSubmitted = false;

  submitNewsletter(): void {
    if (!this.newsletterEmail.trim()) return;
    this.newsletterSubmitted = true;
  }
}
