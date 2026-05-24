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

  readonly firstMessageWords: string[] = 'Start your journey with'.split(' ');
  readonly secondMessageWords: string[] = 'waiting to be discovered'.split(' ');
  readonly paraWords: string[] =
    'Dive into thousands of books, albums, and films — your next favourite story is already here.'.split(' ');

  constructor(private ngZone: NgZone) {}

  ngAfterViewInit(): void {
    this.ngZone.runOutsideAngular(() => {
      this.gsapCtx = gsap.context(() => {
        document.fonts.ready.then(() => this.initAnimations());
      });
    });
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
