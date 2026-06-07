import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MediaApiService } from '../../../core/services/media-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ProductFormModel, createEmptyProductForm } from '../../../core/models/product-form.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.scss'
})
export class ProductFormComponent implements OnInit {
  protected isEdit    = false;
  protected productId: number | null = null;
  protected loading   = false;
  protected saving    = false;
  protected error     = '';
  protected successMsg = '';

  protected step      = 1;
  protected direction: 'forward' | 'back' = 'forward';

  protected uploadMode     = false;
  protected uploadingImage = false;

  protected form: ProductFormModel = createEmptyProductForm();

  readonly categories = [
    { id: 'Book'      as const, label: 'Book',      icon: 'book',      color: '#60a5fa', bg: 'rgba(96,165,250,0.12)',  glow: '0 0 30px rgba(96,165,250,0.25)'  },
    { id: 'CD'        as const, label: 'CD',        icon: 'disc',      color: '#fb923c', bg: 'rgba(251,146,60,0.12)',  glow: '0 0 30px rgba(251,146,60,0.25)'  },
    { id: 'DVD'       as const, label: 'DVD',       icon: 'film',      color: '#c084fc', bg: 'rgba(192,132,252,0.12)', glow: '0 0 30px rgba(192,132,252,0.25)' },
    { id: 'Newspaper' as const, label: 'Newspaper', icon: 'newspaper', color: '#1DB954', bg: 'rgba(29,185,84,0.12)',   glow: '0 0 30px rgba(29,185,84,0.25)'   }
  ];

  protected readonly performedBy: string;

  constructor(
    private readonly mediaApi: MediaApiService,
    private readonly auth:     AuthService,
    private readonly route:    ActivatedRoute,
    private readonly router:   Router
  ) {
    this.performedBy = this.auth.getCurrentUser()?.username ?? 'Manager';
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit    = true;
      this.productId = Number(id);
      this.loading   = true;
      this.mediaApi.getProduct(this.productId).subscribe({
        next:  this.onProductLoaded.bind(this),
        error: this.onProductLoadError.bind(this)
      });
    }
  }

  private onProductLoaded(p: any): void {
    Object.assign(this.form, p);
    this.loading = false;
  }

  private onProductLoadError(): void {
    this.error   = 'Failed to load product.';
    this.loading = false;
  }

  protected selectCategory(id: 'Book' | 'CD' | 'DVD' | 'Newspaper'): void {
    this.form.category = id;
  }

  protected nextStep(): void {
    if (this.step < 3) {
      this.direction = 'forward';
      this.step++;
    } else {
      this.save();
    }
  }

  protected prevStep(): void {
    if (this.step > 1) {
      this.direction = 'back';
      this.step--;
    }
  }

  protected goToStep(s: number): void {
    if (s < this.step) {
      this.direction = 'back';
      this.step = s;
    }
  }

  protected canNext(): boolean {
    if (this.step === 1) return !!this.form.category;
    if (this.step === 2) return !!(this.form.title && this.form.barcode && this.form.originalPrice > 0);
    return true;
  }

  protected getCategoryDef() {
    for (const cat of this.categories) {
      if (cat.id === this.form.category) return cat;
    }
    return this.categories[0];
  }

  protected getStepLabel(s: number): string {
    return s === 1 ? 'Category' : s === 2 ? 'General Info' : (this.form.category || 'Details');
  }

  private onSaveSuccess(): void {
    this.successMsg = this.isEdit ? 'Product updated successfully!' : 'Product added successfully!';
    setTimeout(this.navigateAfterSave.bind(this), 1800);
  }

  private onSaveError(err: any): void {
    this.error  = err.error?.message ?? 'Failed to save product.';
    this.saving = false;
  }

  private navigateAfterSave(): void {
    this.router.navigate(['/manager/products']);
  }

  protected save(): void {
    if (!this.form.title || !this.form.barcode || !this.form.category) {
      this.error = 'Title, barcode and category are required.';
      return;
    }
    this.saving = true;
    this.error  = '';
    const obs = this.isEdit
      ? this.mediaApi.updateMedia(this.productId!, this.form)
      : this.mediaApi.addMedia(this.form);

    obs.subscribe({
      next:  this.onSaveSuccess.bind(this),
      error: this.onSaveError.bind(this)
    });
  }

  private onImageUploaded(res: any): void {
    this.form.imageUrl    = res.url;
    this.uploadingImage   = false;
  }

  private onImageUploadError(): void {
    this.error          = 'Failed to upload image. Make sure the backend is running.';
    this.uploadingImage = false;
  }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file     = input.files[0];
      this.uploadingImage = true;
      const formData = new FormData();
      formData.append('file', file);

      this.mediaApi.uploadImage(formData).subscribe({
        next:  this.onImageUploaded.bind(this),
        error: this.onImageUploadError.bind(this)
      });
    }
  }
}
