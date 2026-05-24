import { Component, ChangeDetectionStrategy } from '@angular/core';
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
      background: radial-gradient(ellipse at top left, #122c1e 0%, #0d0d0d 40%, #0d0d0d 70%, #122c1e 100%);
      pointer-events: none;
    }

        .ambient-bg__blob {
      position: absolute;
      border-radius: 50%;
      will-change: transform;
    }

    .ambient-bg__blob--tl {
      top: -15%;
      right: -10%;
      width: 700px;
      height: 700px;
      background: radial-gradient(circle, rgba(29, 185, 84, 0.25) 0%, transparent 70%);
      filter: blur(80px);
      opacity: 0.6;
      animation: aims-blob-drift-tl 12s ease-in-out infinite alternate;
    }

    .ambient-bg__blob--br {
      bottom: -25%;
      left: -15%;
      width: 900px;
      height: 900px;
      background: radial-gradient(circle, rgba(29, 185, 84, 0.2) 0%, transparent 70%);
      filter: blur(100px);
      opacity: 0.5;
      animation: aims-blob-drift-br 15s ease-in-out infinite alternate;
    }

    .ambient-bg__blob--mid {
      top: 30%;
      left: 55%;
      width: 500px;
      height: 500px;
      background: radial-gradient(circle, rgba(52, 211, 153, 0.2) 0%, transparent 70%);
      filter: blur(60px);
      opacity: 0.3;
      animation: aims-blob-drift-mid 10s ease-in-out infinite alternate;
    }

        .ambient-bg__particle {
      position: absolute;
      width: 4px;
      height: 4px;
      background: #1DB954;
      border-radius: 50%;
      filter: blur(1px);
      opacity: 0.4;
      animation: aims-particle-float linear infinite;
    }

    .p1 { left: 10%; top: 80%; animation-duration: 8s; animation-delay: 0s; width: 6px; height: 6px; }
    .p2 { left: 30%; top: 90%; animation-duration: 12s; animation-delay: -3s; }
    .p3 { left: 60%; top: 70%; animation-duration: 9s; animation-delay: -5s; width: 5px; height: 5px; }
    .p4 { left: 80%; top: 85%; animation-duration: 14s; animation-delay: -2s; }
    .p5 { left: 45%; top: 95%; animation-duration: 10s; animation-delay: -7s; width: 8px; height: 8px; opacity: 0.2; }

    @keyframes aims-particle-float {
      0% { transform: translateY(0) scale(1); opacity: 0; }
      50% { opacity: 0.6; transform: translateY(-30vh) scale(1.5); }
      100% { transform: translateY(-60vh) scale(0.5); opacity: 0; }
    }

        .ambient-bg__noise {
      position: absolute;
      inset: 0;
      opacity: 0.03;
      mix-blend-mode: overlay;
    }

    .ambient-bg__grid {
      position: absolute;
      inset: 0;
      opacity: 0.02;
      background-image: linear-gradient(rgba(29, 185, 84, 0.6) 1px, transparent 1px), linear-gradient(90deg, rgba(29, 185, 84, 0.6) 1px, transparent 1px);
      background-size: 60px 60px;
    }

        .ambient-bg__waves {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      mix-blend-mode: screen;
      will-change: transform;
    }

    .wave--1 { animation: aims-wave-drift-1 14s ease-in-out infinite alternate; }
    .wave--2 { animation: aims-wave-drift-2 18s ease-in-out infinite alternate; }
    .wave--3 { animation: aims-wave-drift-3 12s ease-in-out infinite alternate; }

        .ambient-bg__eq {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 100%;
      height: 15vh;
      display: flex;
      align-items: flex-end;
      justify-content: space-around;
      opacity: 0.05;
      padding: 0 5%;
    }
    .eq-bar {
      width: 2%;
      background: linear-gradient(to top, #1DB954, transparent);
      animation: aims-eq-bounce ease-in-out infinite alternate;
      border-radius: 4px 4px 0 0;
    }
    .eq-1 { height: 30%; animation-duration: 1.2s; }
    .eq-2 { height: 60%; animation-duration: 0.9s; }
    .eq-3 { height: 40%; animation-duration: 1.5s; }
    .eq-4 { height: 80%; animation-duration: 0.8s; }
    .eq-5 { height: 50%; animation-duration: 1.1s; }
    .eq-6 { height: 70%; animation-duration: 1.4s; }
    .eq-7 { height: 20%; animation-duration: 1.0s; }
    .eq-8 { height: 90%; animation-duration: 0.7s; }

    @keyframes aims-eq-bounce {
      0% { transform: scaleY(0.5); transform-origin: bottom; }
      100% { transform: scaleY(1.5); transform-origin: bottom; }
    }

        @keyframes aims-wave-drift-1 {
      0%   { transform: translate3d(0, 0, 0) scale(1); }
      100% { transform: translate3d(-3%, -1.5%, 0) scale(1.06); }
    }
    @keyframes aims-wave-drift-2 {
      0%   { transform: translate3d(0, 0, 0) scale(1); }
      100% { transform: translate3d(3%, 1.5%, 0) scale(1.04); }
    }
    @keyframes aims-wave-drift-3 {
      0%   { transform: translate3d(0, 0, 0); }
      100% { transform: translate3d(-2%, 1%, 0); }
    }

    @keyframes aims-blob-drift-tl {
      0%   { transform: translate(0, 0) scale(1); }
      100% { transform: translate(-3%, 8%) scale(1.1); }
    }
    @keyframes aims-blob-drift-br {
      0%   { transform: translate(0, 0) scale(1); }
      100% { transform: translate(-3%, -8%) scale(0.9); }
    }
    @keyframes aims-blob-drift-mid {
      0%   { transform: translate(0, 0) scale(1); }
      100% { transform: translate(4%, -4%) scale(0.85); }
    }
  `],
  template: `
    <div class="ambient-bg" aria-hidden="true">
      <div class="ambient-bg__blob ambient-bg__blob--tl"></div>
      <div class="ambient-bg__blob ambient-bg__blob--br"></div>
      <div class="ambient-bg__blob ambient-bg__blob--mid"></div>

      <!-- Musical Particles -->
      <div class="ambient-bg__particle p1"></div>
      <div class="ambient-bg__particle p2"></div>
      <div class="ambient-bg__particle p3"></div>
      <div class="ambient-bg__particle p4"></div>
      <div class="ambient-bg__particle p5"></div>

      <!-- Equalizer Bars -->
      <div class="ambient-bg__eq">
        <div class="eq-bar eq-1"></div>
        <div class="eq-bar eq-2"></div>
        <div class="eq-bar eq-3"></div>
        <div class="eq-bar eq-4"></div>
        <div class="eq-bar eq-5"></div>
        <div class="eq-bar eq-6"></div>
        <div class="eq-bar eq-7"></div>
        <div class="eq-bar eq-8"></div>
      </div>

      <svg class="ambient-bg__waves" viewBox="0 0 1920 1080" preserveAspectRatio="none" aria-hidden="true" xmlns="http://www.w3.org/2000/svg">
        <defs>
          <linearGradient id="waveGradient1" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="#1DB954" stop-opacity="0" />
            <stop offset="50%" stop-color="#1DB954" stop-opacity="0.18" />
            <stop offset="100%" stop-color="#1DB954" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="waveGradient2" x1="0%" y1="100%" x2="100%" y2="0%">
            <stop offset="0%" stop-color="#0d6e30" stop-opacity="0" />
            <stop offset="50%" stop-color="#0d6e30" stop-opacity="0.25" />
            <stop offset="100%" stop-color="#0d6e30" stop-opacity="0" />
          </linearGradient>
          <linearGradient id="waveGradient3" x1="50%" y1="0%" x2="50%" y2="100%">
            <stop offset="0%" stop-color="#1DB954" stop-opacity="0.12" />
            <stop offset="100%" stop-color="#1DB954" stop-opacity="0" />
          </linearGradient>
          <filter id="blurFilter" x="-20%" y="-20%" width="140%" height="140%">
            <feGaussianBlur in="SourceGraphic" stdDeviation="20" />
          </filter>
        </defs>

        <g class="wave wave--1">
          <path d="M 0,400 C 320,300 640,500 960,400 C 1280,300 1600,500 1920,400 L 1920,1080 L 0,1080 Z" fill="url(#waveGradient1)" filter="url(#blurFilter)" />
        </g>
        <g class="wave wave--2">
          <path d="M 0,600 C 480,500 960,700 1440,600 C 1680,550 1800,650 1920,600 L 1920,1080 L 0,1080 Z" fill="url(#waveGradient2)" opacity="0.8" filter="url(#blurFilter)" />
        </g>
        <g class="wave wave--3">
          <path d="M 0,800 C 640,750 1280,850 1920,800 L 1920,1080 L 0,1080 Z" fill="url(#waveGradient3)" opacity="0.6" />
        </g>
        
        <g class="flow-lines" opacity="0.15">
          <path d="M -100,200 Q 400,150 800,250 T 1700,200 T 2200,300" stroke="#1DB954" stroke-width="1" fill="none" />
          <path d="M -100,320 Q 400,270 800,370 T 1700,320 T 2200,420" stroke="#1DB954" stroke-width="1" fill="none" />
          <path d="M -100,440 Q 400,390 800,490 T 1700,440 T 2200,540" stroke="#1DB954" stroke-width="1" fill="none" />
        </g>
      </svg>

      <div class="ambient-bg__noise" style="background-image: url(&quot;data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.85' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E&quot;);"></div>
      <div class="ambient-bg__grid"></div>
    </div>
  `
})
export class AmbientBackgroundComponent {}
