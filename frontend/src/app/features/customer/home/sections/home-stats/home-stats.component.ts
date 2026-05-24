import { Component, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

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
export class HomeStatsComponent implements AfterViewInit, OnDestroy {

  private gsapCtx: gsap.Context | undefined;

  readonly headingChars: string[] = 'We have it'.split('');
  readonly paraWords: string[] =
    'Our handpicked collection spans every genre, artist, and era — curated just for you.'.split(' ');

  readonly stats: StatItem[] = [
    { label: 'Books',      value: '10,000', unit: '+' },
    { label: 'CDs',        value: '2,500',  unit: '+' },
    { label: 'DVDs',       value: '3,000',  unit: '+' },
    { label: 'Newspapers', value: '500',    unit: '+' }
  ];

  constructor(private ngZone: NgZone) {}

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
