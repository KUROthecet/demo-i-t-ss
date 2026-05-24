import { Component, AfterViewInit, OnDestroy, NgZone, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { ApiService } from '../../../../../core/services/api.service';

gsap.registerPlugin(ScrollTrigger);

export interface StatItem {
  readonly label: string;
  readonly value: string;
  readonly unit: string;
}

@Component({
  selector: 'app-home-stats',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home-stats.component.html',
  styleUrl: './home-stats.component.scss'
})
export class HomeStatsComponent implements OnInit, AfterViewInit, OnDestroy {

  private gsapCtx: gsap.Context | undefined;

  readonly headingChars: string[] = 'We have it'.split('');
  readonly paraWords: string[] =
    'Our handpicked collection spans every genre, artist, and era — curated just for you.'.split(' ');

  stats: StatItem[] = [
    { label: 'Books',      value: '0', unit: '+' },
    { label: 'CDs',        value: '0', unit: '+' },
    { label: 'DVDs',       value: '0', unit: '+' },
    { label: 'Newspapers', value: '0', unit: '+' }
  ];

  constructor(private ngZone: NgZone, private apiService: ApiService) {}

  ngOnInit(): void {
    this.apiService.getCatalogStats().subscribe({
      next: (data: Record<string, number>) => {
        this.stats = [
          { label: 'Books',      value: this.formatNumber(data['Book'] || 0), unit: '+' },
          { label: 'CDs',        value: this.formatNumber(data['CD'] || 0),  unit: '+' },
          { label: 'DVDs',       value: this.formatNumber(data['DVD'] || 0),  unit: '+' },
          { label: 'Newspapers', value: this.formatNumber(data['Newspaper'] || 0), unit: '+' }
        ];
      },
      error: (err: any) => console.error('Failed to load stats', err)
    });
  }

  private formatNumber(num: number): string {
    if (num <= 10) return num.toString();
    // Round to nicest whole numbers (e.g. 4891 -> 4,800, 150 -> 100, 12000 -> 12,000)
    let magnitude = Math.pow(10, Math.floor(Math.log10(num)) - 1);
    if (magnitude < 10) magnitude = 10;
    const rounded = Math.floor(num / magnitude) * magnitude;
    return new Intl.NumberFormat('en-US').format(rounded);
  }

  ngAfterViewInit(): void {
    this.ngZone.runOutsideAngular(() => {
      this.gsapCtx = gsap.context(() => {
        document.fonts.ready.then(() => this.initAnimations());
      });
    });
  }

  private initAnimations(): void {
    const headingChars = gsap.utils.toArray<HTMLElement>('.home-stats__heading-char');
    const paraWords    = gsap.utils.toArray<HTMLElement>('.home-stats__word');
    const clipEl       = document.querySelector<HTMLElement>('.home-stats__clip')!;

    gsap.timeline({
      scrollTrigger: {
        trigger: '.home-stats',
        start: 'top 32%',
        end: 'top 10%',
        scrub: true
      }
    })
    .from(headingChars, {
      stagger: 0.2,
      yPercent: 600,
      rotate: 4,
      ease: 'power1.inOut'
    })
    .from(paraWords, {
      opacity: 0,
      stagger: 0.2,
      yPercent: 30,
      rotate: 4,
      ease: 'power1.inOut'
    }, '-=0.5')
    .to(clipEl, {
      clipPath: 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)',
      duration: 2
    }, '-=0.2');
  }

  ngOnDestroy(): void {
    this.gsapCtx?.revert();
  }
}
