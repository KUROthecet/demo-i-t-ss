import { Component, ChangeDetectionStrategy, ElementRef, ViewChild, AfterViewInit, OnDestroy, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-ambient-background',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  styles: [`
    :host {
      display: contents;
    }

    .ambient-bg {
      position: fixed;
      inset: 0;
      z-index: -1;
      overflow: hidden;
      background: radial-gradient(ellipse at 50% 0%, #0d2e1b 0%, #030805 100%);
      pointer-events: none;
    }

    .ambient-bg__canvas {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      opacity: 0.85;
      mix-blend-mode: screen;
      filter: blur(2px);
    }

    .ambient-bg__glow {
      position: absolute;
      top: -20%;
      left: 50%;
      transform: translateX(-50%);
      width: 80%;
      height: 60%;
      background: radial-gradient(circle, rgba(29, 185, 84, 0.15) 0%, transparent 60%);
      filter: blur(80px);
      z-index: 1;
    }

    .ambient-bg__noise {
      position: absolute;
      inset: 0;
      opacity: 0.05;
      mix-blend-mode: overlay;
      background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
      z-index: 2;
    }
  `],
  template: `
    <div class="ambient-bg" aria-hidden="true">
      <div class="ambient-bg__glow"></div>
      <canvas #canvasRef class="ambient-bg__canvas"></canvas>
      <div class="ambient-bg__noise"></div>
    </div>
  `
})
export class AmbientBackgroundComponent implements AfterViewInit, OnDestroy {
  @ViewChild('canvasRef') canvasRef!: ElementRef<HTMLCanvasElement>;

  private ctx!: CanvasRenderingContext2D;
  private animationId = 0;
  private time = 0;
  private resizeListener!: () => void;
  private boundRender!: FrameRequestCallback;

  private readonly colors = [
    'rgba(29, 185, 84, 0.9)',
    'rgba(16, 185, 129, 0.7)',
    'rgba(52, 211, 153, 0.5)',
    'rgba(5, 150, 105, 0.6)',
    'rgba(110, 231, 183, 0.4)'
  ];

  constructor(private ngZone: NgZone) {
    this.resizeListener = this.resize.bind(this);
    this.boundRender    = this.render.bind(this) as FrameRequestCallback;
  }

  ngAfterViewInit(): void {
    this.ctx = this.canvasRef.nativeElement.getContext('2d')!;
    window.addEventListener('resize', this.resizeListener);
    this.resize();
    this.ngZone.runOutsideAngular(this.startRenderLoop.bind(this));
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.resizeListener);
    cancelAnimationFrame(this.animationId);
  }

  private startRenderLoop(): void {
    this.render();
  }

  private resize(): void {
    if (!this.canvasRef) return;
    this.canvasRef.nativeElement.width  = window.innerWidth;
    this.canvasRef.nativeElement.height = window.innerHeight;
  }

  private render(): void {
    if (!this.ctx) return;
    const w = this.ctx.canvas.width;
    const h = this.ctx.canvas.height;

    this.ctx.clearRect(0, 0, w, h);

    this.time += 0.004;

    this.ctx.lineWidth = 2.5;
    this.ctx.lineCap   = 'round';
    this.ctx.lineJoin  = 'round';

    const waves = this.colors.length;
    for (let i = 0; i < waves; i++) {
      this.ctx.beginPath();

      const waveAmplitude = h * 0.22;
      const waveFrequency = 0.0012;
      const yOffset       = h * 0.55 + (i * 15);

      for (let x = 0; x <= w; x += 15) {
        const noise = Math.sin(x * waveFrequency + this.time + i * 0.5) * waveAmplitude +
                      Math.cos(x * waveFrequency * 2.2 - this.time * 1.3 + i) * (waveAmplitude * 0.15);
        const y = yOffset + noise;
        if (x === 0) this.ctx.moveTo(x, y);
        else         this.ctx.lineTo(x, y);
      }

      this.ctx.strokeStyle = this.colors[i];
      this.ctx.stroke();
    }

    this.animationId = requestAnimationFrame(this.boundRender);
  }
}
