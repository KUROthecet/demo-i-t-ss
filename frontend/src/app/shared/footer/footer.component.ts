import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MediaApiService } from '../../core/services/media-api.service';

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
  newsletterSubmitting = false;

  constructor(private readonly mediaApi: MediaApiService) {}

  submitNewsletter(): void {
    const email = this.newsletterEmail.trim();
    if (!email) return;
    this.newsletterSubmitting = true;
    this.mediaApi.subscribeNewsletter(email).subscribe({
      next: () => {
        this.newsletterSubmitting = false;
        this.newsletterSubmitted  = true;
      },
      error: () => {
        this.newsletterSubmitting = false;
        this.newsletterSubmitted  = true;
      }
    });
  }
}
