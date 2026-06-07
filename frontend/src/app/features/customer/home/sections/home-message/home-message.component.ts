import { Component, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-home-message',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home-message.component.html',
  styleUrl: './home-message.component.scss'
})
export class HomeMessageComponent implements AfterViewInit, OnDestroy {

  private gsapCtx: gsap.Context | undefined;

  readonly firstMessageWords: string[] = 'Four formats. One store.'.split(' ');
  readonly secondMessageWords: string[] = 'to your door. Every week.'.split(' ');
  readonly paraWords: string[] =
    'Books for quiet mornings. CDs for the drive home. DVDs for Friday night. Newspapers for the desk. AIMS carries them all — physical, real, and reliable.'.split(' ');

  constructor(private ngZone: NgZone) {}

  ngAfterViewInit(): void {
    this.ngZone.runOutsideAngular(this.setupGsapContext.bind(this));
  }

  private setupGsapContext(): void {
    this.gsapCtx = gsap.context(this.registerGsapAnimations.bind(this));
  }

  private registerGsapAnimations(): void {
    document.fonts.ready.then(this.initAnimations.bind(this));
  }

  private initAnimations(): void {
    gsap.to('.home-message__word--first', {
      color: '#ffffff',
      ease: 'power1.in',
      stagger: 1,
      scrollTrigger: {
        trigger: '.home-message__content',
        start: 'top center',
        end: '35% center',
        scrub: true
      }
    });

    gsap.to('.home-message__word--second', {
      color: '#ffffff',
      ease: 'power1.in',
      stagger: 1,
      scrollTrigger: {
        trigger: '.home-message__second-wrapper',
        start: 'top center',
        end: 'bottom center',
        scrub: true
      }
    });

    gsap.to('.home-message__clip', {
      clipPath: 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)',
      duration: 0.55,
      ease: 'circ.inOut',
      scrollTrigger: {
        trigger: '.home-message__clip',
        start: 'top 62%'
      }
    });

    gsap.from('.home-message__word--para', {
      yPercent: 300,
      rotate: 3,
      duration: 1,
      stagger: 0.012,
      ease: 'power1.inOut',
      scrollTrigger: {
        trigger: '.home-message__para',
        start: 'top 62%'
      }
    });
  }

  ngOnDestroy(): void {
    this.gsapCtx?.revert();
  }
}
