import {
  Component, Input, AfterViewInit, OnDestroy,
  ElementRef, ViewChild, NgZone
} from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-product-image-frame',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-image-frame.component.html',
  styleUrl: './product-image-frame.component.scss'
})
export class ProductImageFrameComponent implements AfterViewInit, OnDestroy {
  @Input() imageUrl  = '';
  @Input() category  = '';

  @ViewChild('frameEl') private frameEl!: ElementRef<HTMLElement>;

  protected isHovered = false;

  private boundMouseMove!:  (e: MouseEvent) => void;
  private boundMouseEnter!: () => void;
  private boundMouseLeave!: () => void;

  constructor(private readonly ngZone: NgZone) {
    this.boundMouseMove  = this.handleMouseMove.bind(this);
    this.boundMouseEnter = this.handleMouseEnter.bind(this);
    this.boundMouseLeave = this.handleMouseLeave.bind(this);
  }

  ngAfterViewInit(): void {
    if (!this.frameEl?.nativeElement) return;
    this.ngZone.runOutsideAngular(this.attachEvents.bind(this));
  }

  ngOnDestroy(): void {
    if (!this.frameEl?.nativeElement) return;
    const el = this.frameEl.nativeElement;
    el.removeEventListener('mousemove',  this.boundMouseMove);
    el.removeEventListener('mouseenter', this.boundMouseEnter);
    el.removeEventListener('mouseleave', this.boundMouseLeave);
  }

  private attachEvents(): void {
    const el = this.frameEl.nativeElement;
    el.addEventListener('mousemove',  this.boundMouseMove);
    el.addEventListener('mouseenter', this.boundMouseEnter);
    el.addEventListener('mouseleave', this.boundMouseLeave);
  }

  private handleMouseMove(e: MouseEvent): void {
    const el   = this.frameEl.nativeElement;
    const rect = el.getBoundingClientRect();
    const x    = ((e.clientX - rect.left) / rect.width)  * 100;
    const y    = ((e.clientY - rect.top)  / rect.height) * 100;
    el.style.setProperty('--glare-x', `${x.toFixed(1)}%`);
    el.style.setProperty('--glare-y', `${y.toFixed(1)}%`);
    const rotX = (((e.clientY - rect.top)  / rect.height) - 0.5) * -24;
    const rotY = (((e.clientX - rect.left) / rect.width)  - 0.5) *  24;
    el.style.setProperty('--rot-x', rotX.toFixed(2));
    el.style.setProperty('--rot-y', rotY.toFixed(2));
  }

  private handleMouseEnter(): void {
    this.ngZone.run(this.setHovered.bind(this, true));
  }

  private handleMouseLeave(): void {
    const el = this.frameEl.nativeElement;
    el.style.setProperty('--rot-x', '0');
    el.style.setProperty('--rot-y', '0');
    this.ngZone.run(this.setHovered.bind(this, false));
  }

  private setHovered(value: boolean): void {
    this.isHovered = value;
  }

  protected isDisc(): boolean {
    return this.category === 'CD' || this.category === 'DVD';
  }

  protected isBook(): boolean {
    return this.category === 'Book';
  }

  protected isNewspaper(): boolean {
    return this.category === 'Newspaper';
  }
}
