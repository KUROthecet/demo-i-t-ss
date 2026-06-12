import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MediaApiService } from '../../../core/services/media-api.service';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss'
})
export class ContactComponent {
  form = {
    name: '',
    email: '',
    subject: '',
    message: ''
  };
  submitted = false;
  sending = false;
  submitError = '';

  constructor(private readonly mediaApi: MediaApiService) {}

  submit(): void {
    if (!this.form.name || !this.form.email || !this.form.message) return;
    this.sending = true;
    this.submitError = '';
    this.mediaApi.submitContact(this.form).subscribe({
      next: () => {
        this.sending = false;
        this.submitted = true;
      },
      error: () => {
        this.sending = false;
        this.submitError = 'Failed to send your message. Please try again.';
      }
    });
  }
}
