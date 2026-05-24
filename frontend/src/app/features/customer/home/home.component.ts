import { Component } from '@angular/core';
import { HomeHeroComponent }        from './sections/home-hero/home-hero.component';
import { HomeMessageComponent }     from './sections/home-message/home-message.component';
import { HomeCategoriesComponent }  from './sections/home-categories/home-categories.component';
import { HomeStatsComponent }       from './sections/home-stats/home-stats.component';
import { HomeBenefitsComponent }    from './sections/home-benefits/home-benefits.component';
import { HomeCtaComponent }         from './sections/home-cta/home-cta.component';
import { HomeRecommendedComponent } from './sections/home-recommended/home-recommended.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    HomeHeroComponent,
    HomeMessageComponent,
    HomeCategoriesComponent,
    HomeStatsComponent,
    HomeBenefitsComponent,
    HomeCtaComponent,
    HomeRecommendedComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {}
