import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-scroll-to-top',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button 
      class="scroll-to-top" 
      [class.scroll-to-top--visible]="isVisible"
      (click)="scrollToTop()"
      aria-label="Scroll to top"
    >
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 19V5M5 12l7-7 7 7"/>
      </svg>
    </button>
  `,
  styles: [`
    .scroll-to-top {
      position: fixed;
      bottom: 40px;
      right: 40px;
      width: 50px;
      height: 50px;
      border-radius: 50%;
      background: #1DB954;
      color: black;
      border: none;
      box-shadow: 0 4px 20px rgba(29, 185, 84, 0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      z-index: 1000;
      opacity: 0;
      transform: translateY(20px) scale(0.8);
      pointer-events: none;
      transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);

      &.scroll-to-top--visible {
        opacity: 1;
        transform: translateY(0) scale(1);
        pointer-events: auto;
      }

      &:hover {
        transform: translateY(-4px) scale(1.05);
        box-shadow: 0 8px 30px rgba(29, 185, 84, 0.6);
      }

      &:active {
        transform: translateY(0) scale(0.95);
        transition-duration: 0.1s;
      }

      svg {
        width: 24px;
        height: 24px;
        transition: transform 0.3s ease;
      }

      &:hover svg {
        transform: translateY(-2px);
      }
    }
    
    @media (max-width: 768px) {
      .scroll-to-top {
        bottom: 20px;
        right: 20px;
        width: 44px;
        height: 44px;
      }
    }
  `]
})
export class ScrollToTopComponent {
  isVisible = false;

  @HostListener('window:scroll')
  onWindowScroll(): void {
    const yOffset = window.scrollY || document.documentElement.scrollTop;
    this.isVisible = yOffset > 300;
  }

  scrollToTop(): void {
    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }
}
