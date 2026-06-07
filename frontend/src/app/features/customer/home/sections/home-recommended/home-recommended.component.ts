import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MediaApiService } from '../../../../../core/services/media-api.service';
import { Media } from '../../../../../core/models/media.model';
import { ProductCardComponent } from '../../../../../shared/product-card/product-card.component';

@Component({
  selector: 'app-home-recommended',
  standalone: true,
  imports: [CommonModule, RouterLink, ProductCardComponent],
  templateUrl: './home-recommended.component.html',
  styleUrl: './home-recommended.component.scss'
})
export class HomeRecommendedComponent implements OnInit {
  products: Media[] = [];
  loading = true;

  readonly skeletons = Array(8).fill(0);

  constructor(private readonly mediaApi: MediaApiService) {}

  ngOnInit(): void {
    this.mediaApi.getProducts(20).subscribe({
      next:  this.onProductsLoaded.bind(this),
      error: this.onProductsError.bind(this)
    });
  }

  private onProductsLoaded(data: Media[]): void {
    this.products = data;
    this.loading  = false;
  }

  private onProductsError(): void {
    this.loading = false;
  }
}
