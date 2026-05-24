import { Component, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import { RouterLink } from '@angular/router';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

@Component({
  selector: 'app-home-hero',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home-hero.component.html',
  styleUrl: './home-hero.component.scss'
})
export class HomeHeroComponent implements AfterViewInit, OnDestroy {

  private gsapCtx: gsap.Context | undefined;

  readonly heroImageUrl =
    'https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=1920&q=80';

  readonly titleLine1Chars: string[] = 'ENDLESS'.split('');
  readonly titleLine2Chars: string[] = 'STORIES'.split('');

  constructor(private ngZone: NgZone) {}

  ngAfterViewInit(): void {
    this.ngZone.runOutsideAngular(() => {
      this.gsapCtx = gsap.context(() => {
        document.fonts.ready.then(() => this.initAnimations());
      });
    });
  }

  private initAnimations(): void {
    const chars  = gsap.utils.toArray<HTMLElement>('.home-hero__char');
    const clipEl = document.querySelector<HTMLElement>('.home-hero__subtitle-clip')!;
    const contentEl = document.querySelector<HTMLElement>('.home-hero__content')!;

    gsap.timeline({ delay: 0.5 })
      .to(contentEl, { opacity: 1, y: 0, duration: 0.8, ease: 'power1.inOut' })
      .to(clipEl, {
        clipPath: 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)',
        duration: 0.9,
        ease: 'circ.out'
      }, '-=0.4')
      .from(chars, {
        yPercent: 200,
        stagger: 0.025,
        duration: 0.65,
        ease: 'power2.out'
      }, '-=0.7');

    gsap.timeline({
      scrollTrigger: {
        trigger: '.home-hero__container',
        start: '1% top',
        end: 'bottom top',
        scrub: true
      }
    }).to('.home-hero__container', {
      rotate: 7,
      scale: 0.9,
      yPercent: 30,
      ease: 'power1.inOut'
    });
  }

  ngOnDestroy(): void {
    this.gsapCtx?.revert();
  }
}
