import { Component, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

export interface BenefitItem {
  readonly title: string;
  readonly bg: string;
  readonly color: string;
  readonly borderColor: string;
  readonly rotation: string;
  readonly translateY: string;
  readonly cssClass: string;
}

@Component({
  selector: 'app-home-benefits',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home-benefits.component.html',
  styleUrl: './home-benefits.component.scss'
})
export class HomeBenefitsComponent implements AfterViewInit, OnDestroy {

  private gsapCtx: gsap.Context | undefined;

  readonly paraWords: string[] =
    'Unlock the advantages — discover why thousands choose AIMS.'.split(' ');

  readonly benefits: BenefitItem[] = [
    {
      title: 'Free Delivery',
      bg: '#1DB954',
      color: '#050505',
      borderColor: '#050505',
      rotation: '3deg',
      translateY: '0',
      cssClass: 'benefit-first'
    },
    {
      title: 'Curated Selection',
      bg: '#f5f5f5',
      color: '#050505',
      borderColor: '#050505',
      rotation: '-1deg',
      translateY: '-1.25rem',
      cssClass: 'benefit-second'
    },
    {
      title: 'Easy Returns',
      bg: '#0d2818',
      color: '#ffffff',
      borderColor: '#050505',
      rotation: '1deg',
      translateY: '-3rem',
      cssClass: 'benefit-third'
    },
    {
      title: 'Expert Support',
      bg: '#ffd700',
      color: '#050505',
      borderColor: '#050505',
      rotation: '-5deg',
      translateY: '-3rem',
      cssClass: 'benefit-fourth'
    }
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
    const paraWords = gsap.utils.toArray<HTMLElement>('.home-benefits__word');

    const tl = gsap.timeline({
      scrollTrigger: {
        trigger: '.home-benefits',
        start: 'top 65%',
        end: 'top -10%',
        scrub: 1.5
      }
    });

    tl.from(paraWords, {
      duration: 1,
      stagger: 0.2,
      opacity: 0,
      rotate: 8,
      yPercent: 30,
      ease: 'power1.inOut'
    });

    ['first', 'second', 'third', 'fourth'].forEach(name => {
      tl.to(`.benefit-${name}`, {
        duration: 1,
        opacity: 1,
        clipPath: 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)',
        ease: 'circ.out'
      });
    });
  }

  ngOnDestroy(): void {
    this.gsapCtx?.revert();
  }
}
