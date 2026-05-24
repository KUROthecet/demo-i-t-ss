import { Component, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import { RouterLink } from '@angular/router';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-home-cta',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home-cta.component.html',
  styleUrl: './home-cta.component.scss'
})
export class HomeCtaComponent implements AfterViewInit, OnDestroy {

  private gsapCtx: gsap.Context | undefined;

  readonly titleChars: string[] = 'Your Next'.split('');

  constructor(private ngZone: NgZone) {}

  ngAfterViewInit(): void {
    this.ngZone.runOutsideAngular(() => {
      this.gsapCtx = gsap.context(() => {
        document.fonts.ready.then(() => this.initAnimations());
      });
    });
  }

  private initAnimations(): void {
    const chars  = gsap.utils.toArray<HTMLElement>('.home-cta__char');
    const clipEl = document.querySelector<HTMLElement>('.home-cta__clip')!;

    gsap.timeline({
      scrollTrigger: {
        trigger: '.home-cta',
        start: 'top 52%',
        end: 'top 10%',
        scrub: 1.5
      }
    })
    .from(chars, {
      stagger: 0.2,
      opacity: 0,
      rotate: 3,
      yPercent: 30,
      ease: 'power1.inOut'
    })
    .to(clipEl, {
      opacity: 1,
      clipPath: 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)',
      ease: 'circ.out'
    });
  }

  ngOnDestroy(): void {
    this.gsapCtx?.revert();
  }
}
