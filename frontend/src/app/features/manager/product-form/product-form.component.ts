import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MediaApiService, FieldSchema } from '../../../core/services/media-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { AppConfigService } from '../../../core/services/app-config.service';
import { ProductFormModel, createEmptyProductForm } from '../../../core/models/product-form.model';
import { Media } from '../../../core/models/media.model';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.scss'
})
export class ProductFormComponent implements OnInit {
  protected isEdit       = false;
  protected productId: number | null = null;
  protected loading      = false;
  protected saving       = false;
  protected error        = '';
  protected successMsg   = '';
  protected uploadMode   = false;
  protected uploadingImage = false;

  protected step      = 1;
  protected direction: 'forward' | 'back' = 'forward';

  protected form: ProductFormModel = createEmptyProductForm();
  protected availableCategoryFields: Record<string, FieldSchema[]> = {};
  protected schemaLoading = true;

  private static readonly KNOWN_CATEGORY_DEFS: Record<string, { label: string; icon: string; color: string; bg: string; glow: string }> = {
    Book:      { label: 'Book',      icon: 'book',      color: '#60a5fa', bg: 'rgba(96,165,250,0.12)',  glow: '0 0 30px rgba(96,165,250,0.25)'  },
    CD:        { label: 'CD',        icon: 'disc',      color: '#fb923c', bg: 'rgba(251,146,60,0.12)',  glow: '0 0 30px rgba(251,146,60,0.25)'  },
    DVD:       { label: 'DVD',       icon: 'film',      color: '#c084fc', bg: 'rgba(192,132,252,0.12)', glow: '0 0 30px rgba(192,132,252,0.25)' },
    Newspaper: { label: 'Newspaper', icon: 'newspaper', color: '#1DB954', bg: 'rgba(29,185,84,0.12)',   glow: '0 0 30px rgba(29,185,84,0.25)'   }
  };

  protected categories: { id: string; label: string; icon: string; color: string; bg: string; glow: string }[] = [];

  protected readonly performedBy: string;

  constructor(
    private readonly mediaApi:   MediaApiService,
    private readonly auth:       AuthService,
    private readonly route:      ActivatedRoute,
    private readonly router:     Router,
    private readonly appConfig:  AppConfigService
  ) {
    this.performedBy = this.auth.getCurrentUser()?.username ?? 'Manager';
  }

  private categoryColor(label: string): string {
    let h = 0;
    for (let i = 0; i < label.length; i++) {
      h = (Math.imul(31, h) + label.charCodeAt(i)) | 0;
    }
    return `oklch(72% 0.17 ${Math.abs(h) % 360})`;
  }

  private buildCategoryDef(id: string): { id: string; label: string; icon: string; color: string; bg: string; glow: string } {
    const known = ProductFormComponent.KNOWN_CATEGORY_DEFS[id];
    if (known) return { id, ...known };
    const color = this.categoryColor(id);
    const base  = color.slice(0, -1);
    return { id, label: id, icon: 'tag', color, bg: `${base} / 12%)`, glow: `0 0 30px ${base} / 25%)` };
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.mediaApi.getFieldSchema().subscribe({
      next: (schema) => {
        this.availableCategoryFields = schema;
        this.categories = Object.keys(schema).map(k => this.buildCategoryDef(k));
        this.schemaLoading = false;
        if (id) {
          this.isEdit    = true;
          this.productId = Number(id);
          this.loading   = true;
          this.mediaApi.getProduct(this.productId).subscribe({
            next:  (p) => this.onProductLoaded(p),
            error: ()  => { this.error = 'Failed to load product.'; this.loading = false; }
          });
        }
      },
      error: () => { this.schemaLoading = false; }
    });
  }

  private onProductLoaded(p: Media): void {
    this.form = createEmptyProductForm();
    Object.assign(this.form, {
      barcode:            p.barcode,
      title:              p.title,
      category:           p.category as ProductFormModel['category'],
      originalPrice:      p.originalPrice,
      currentPrice:       p.currentPrice,
      generalDescription: p.generalDescription,
      imageUrl:           p.imageUrl,
      quantityInStock:    p.quantityInStock,
      weight:             p.weight,
      dimensions:         p.dimensions,
      supportRushDelivery: p.supportRushDelivery
    });
    for (const field of this.availableCategoryFields[p.category] ?? []) {
      const raw = p.attributes?.[field.attributeKey] ?? '';
      (this.form as any)[field.key] = field.type === 'number'
        ? (raw ? parseInt(raw, 10) : null)
        : raw;
    }
    this.loading = false;
  }

  get currentCategoryFields(): FieldSchema[] {
    return this.availableCategoryFields[this.form.category] ?? [];
  }

  protected selectCategory(id: string): void {
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

  protected getCategoryDef(): { id: string; label: string; icon: string; color: string; bg: string; glow: string } {
    return this.categories.find(c => c.id === this.form.category) ?? this.buildCategoryDef(this.form.category);
  }

  protected getStepLabel(s: number): string {
    return s === 1 ? 'Category' : s === 2 ? 'General Info' : (this.form.category || 'Details');
  }

  protected get priceMin(): number { return Math.round(this.form.originalPrice * this.appConfig.priceMinRatio); }
  protected get priceMax(): number { return Math.round(this.form.originalPrice * this.appConfig.priceMaxRatio); }

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const formData = new FormData();
    formData.append('file', input.files[0]);
    this.uploadingImage = true;
    this.mediaApi.uploadImage(formData).subscribe({
      next:  (res) => { this.form.imageUrl = res.url; this.uploadingImage = false; },
      error: ()    => { this.error = 'Image upload failed.'; this.uploadingImage = false; }
    });
  }

  protected save(): void {
    if (!this.form.title || !this.form.barcode || !this.form.category) {
      this.error = 'Title, barcode and category are required.';
      return;
    }
    if (this.form.originalPrice > 0) {
      if (this.form.currentPrice < this.priceMin || this.form.currentPrice > this.priceMax) {
        const minPct = Math.round(this.appConfig.priceMinRatio * 100);
        const maxPct = Math.round(this.appConfig.priceMaxRatio * 100);
        this.error = `Price must be between ${this.priceMin.toLocaleString('vi-VN')} and ${this.priceMax.toLocaleString('vi-VN')} VND (${minPct}%–${maxPct}% of original price).`;
        return;
      }
    }
    this.saving = true;
    this.error  = '';
    const obs = this.isEdit
      ? this.mediaApi.updateMedia(this.productId!, this.form, this.performedBy)
      : this.mediaApi.addMedia(this.form, this.performedBy);

    obs.subscribe({
      next:  () => {
        this.successMsg = this.isEdit ? 'Product updated successfully!' : 'Product added successfully!';
        setTimeout(() => this.router.navigate(['/manager/products']), 1800);
      },
      error: (err) => {
        this.error  = err.error?.message ?? 'Failed to save product.';
        this.saving = false;
      }
    });
  }
}
