import {
  Component, AfterViewInit, OnDestroy, NgZone,
  ViewChild, ElementRef
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import gsap from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';

gsap.registerPlugin(ScrollTrigger);

export interface CategoryCard {
  readonly id: string;
  readonly name: string;
  readonly label: string;
  readonly category: string;
  readonly imageUrl: string;
  readonly accentColor: string;
  readonly bgColor: string;
  readonly rotation: string;
}

@Component({
  selector: 'app-home-categories',
  standalone: true,
  imports: [RouterLink, CommonModule],
  templateUrl: './home-categories.component.html',
  styleUrl: './home-categories.component.scss'
})
export class HomeCategoriesComponent implements AfterViewInit, OnDestroy {

  @ViewChild('categoriesSection') sectionRef!: ElementRef<HTMLElement>;
  @ViewChild('scrollInner') scrollInnerRef!: ElementRef<HTMLElement>;
  @ViewChild('sliderCards') sliderCardsRef!: ElementRef<HTMLElement>;

  private gsapCtx: gsap.Context | undefined;

  readonly titleLine1Chars: string[] = 'We have'.split('');
  readonly titleLine2Chars: string[] = 'Categories'.split('');

  readonly cards: CategoryCard[] = [
    {
      id: 'books',
      name: 'Books',
      label: 'Books',
      category: 'Book',
      imageUrl: 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?auto=format&fit=crop&w=600&q=80',
      accentColor: '#60A5FA',
      bgColor: '#0d1b2e',
      rotation: '-8deg'
    },
    {
      id: 'cds',
      name: 'CDs',
      label: 'CDs',
      category: 'CD',
      imageUrl: 'https://images.unsplash.com/photo-1619983081593-e2ba5b543168?auto=format&fit=crop&w=600&q=80',
      accentColor: '#FB923C',
      bgColor: '#2e1508',
      rotation: '8deg'
    },
    {
      id: 'dvds',
      name: 'DVDs',
      label: 'DVDs',
      category: 'DVD',
      imageUrl: 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?auto=format&fit=crop&w=600&q=80',
      accentColor: '#C084FC',
      bgColor: '#1a0d2e',
      rotation: '-8deg'
    },
    {
      id: 'newspapers',
      name: 'Newspapers',
      label: 'Newspapers',
      category: 'Newspaper',
      imageUrl: 'https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=600&q=80',
      accentColor: '#34D399',
      bgColor: '#0a1e19',
      rotation: '8deg'
    }
  ];

  constructor(private ngZone: NgZone) {}

  ngAfterViewInit(): void {
    this.ngZone.runOutsideAngular(() => {
      this.gsapCtx = gsap.context(() => {
        document.fonts.ready.then(() => this.initTitleAnimations());
        this.initHorizontalScroll();
        this.initCardParallax();
      });
    });
  }

  private initTitleAnimations(): void {
    const line1Chars = gsap.utils.toArray<HTMLElement>('.home-categories__char--line1');
    const line2Chars = gsap.utils.toArray<HTMLElement>('.home-categories__char--line2');
    const clipEl     = document.querySelector<HTMLElement>('.home-categories__title-clip')!;

    gsap.from(line1Chars, {
      yPercent: 200, stagger: 0.02, ease: 'power1.inOut',
      scrollTrigger: { trigger: '.home-categories', start: 'top 35%' }
    });

    gsap.to(clipEl, {
      clipPath: 'polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%)',
      duration: 1,
      scrollTrigger: { trigger: '.home-categories', start: 'top 18%' }
    });

    gsap.from(line2Chars, {
      yPercent: 200, stagger: 0.02, ease: 'power1.inOut',
      scrollTrigger: { trigger: '.home-categories', start: 'top 3%' }
    });

    const sectionEl = document.querySelector<HTMLElement>('.home-categories')!;
    const innerEl   = document.querySelector<HTMLElement>('.home-categories__scroll-inner')!;

    gsap.timeline({
      scrollTrigger: {
        trigger: sectionEl,
        start: 'top top',
        end: () => `+=${(innerEl?.scrollWidth ?? 0) - window.innerWidth}`,
        scrub: true,
        invalidateOnRefresh: true
      }
    })
    .to('.home-categories__title-line--first', { xPercent: -30, ease: 'power1.inOut' })
    .to('.home-categories__title-clip',         { xPercent: -22, ease: 'power1.inOut' }, '<')
    .to('.home-categories__title-line--second', { xPercent: -10, ease: 'power1.inOut' }, '<');
  }

  private initHorizontalScroll(): void {
    if (window.innerWidth <= 1024) return;

    const sectionEl = this.sectionRef?.nativeElement;
    const innerEl   = this.scrollInnerRef?.nativeElement;
    if (!sectionEl || !innerEl) return;

    /*
     * Defer until after paint so images are rendered and getBoundingClientRect /
     * scrollWidth reflect the real layout (same reason SpyltMilk uses useEffect).
     * Two rAFs ensure we're past the first layout flush.
     */
    const setup = () => {
      requestAnimationFrame(() => {
        /*
         * Use the FULL scrollInner width (title col + all cards) minus viewport.
         * Using only sliderCards.scrollWidth gives the wrong (too-small) amount and
         * causes the section to "get stuck" before showing all four cards.
         */
        const scrollAmount = innerEl.scrollWidth - window.innerWidth;
        if (scrollAmount <= 0) return;         // guard — layout not ready

        gsap.to(innerEl, {
          x: () => -(innerEl.scrollWidth - window.innerWidth),
          ease: 'none',
          scrollTrigger: {
            trigger: sectionEl,
            start: 'top top',
            /* scroll distance ≈ 1.5× translation so the motion feels deliberate */
            end: () => `+=${(innerEl.scrollWidth - window.innerWidth) * 1.5}`,
            scrub: 1.5,
            pin: true,
            anticipatePin: 1,
            invalidateOnRefresh: true
          }
        });

        ScrollTrigger.refresh();
      });
    };

    /* Small timeout as a safety net for slow image loads */
    setTimeout(setup, 100);
  }

  private initCardParallax(): void {
    document.querySelectorAll<HTMLElement>('.home-categories__card').forEach(card => {
      card.addEventListener('mousemove', (e: MouseEvent) => {
        const b = card.getBoundingClientRect();
        const ox = ((e.clientX - b.left)  / b.width  - 0.5) * 30;
        const oy = ((e.clientY - b.top)   / b.height - 0.5) * 30;
        const fg = card.querySelector<HTMLElement>('.home-categories__card-fg-img');
        const bg = card.querySelector<HTMLElement>('.home-categories__card-glow');
        if (fg) gsap.to(fg, { x: -ox, y: 0,  duration: 0.3, ease: 'power2.out' });
        if (bg) gsap.to(bg, { x:  ox, y: oy, duration: 0.3, ease: 'power2.out' });
      });
      card.addEventListener('mouseleave', () => {
        const fg = card.querySelector<HTMLElement>('.home-categories__card-fg-img');
        const bg = card.querySelector<HTMLElement>('.home-categories__card-glow');
        if (fg) gsap.to(fg, { x: 0, y: 0, duration: 0.5, ease: 'power3.out' });
        if (bg) gsap.to(bg, { x: 0, y: 0, duration: 0.5, ease: 'power3.out' });
      });
    });
  }

  ngOnDestroy(): void {
    this.gsapCtx?.revert();
  }
}
