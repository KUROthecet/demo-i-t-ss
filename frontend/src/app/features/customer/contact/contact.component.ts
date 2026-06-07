import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

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

  submit(): void {
    if (!this.form.name || !this.form.email || !this.form.message) {
      return;
    }
    this.sending = true;
    setTimeout(this.onSubmitComplete.bind(this), 900);
  }

  private onSubmitComplete(): void {
    this.sending   = false;
    this.submitted = true;
  }
}
