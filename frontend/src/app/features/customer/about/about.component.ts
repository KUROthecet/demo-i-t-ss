import { Component } from '@angular/core';

interface TeamMember {
  name: string;
  role: string;
  imageUrl: string;
}

@Component({
  selector: 'app-about',
  standalone: true,
  imports: [],
  templateUrl: './about.component.html',
  styleUrl: './about.component.scss'
})
export class AboutComponent {
  teamMembers: TeamMember[] = [
    { name: 'NGUYỄN MINH ĐỨC', role: 'Student ID: 20235915', imageUrl: 'https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Jayce_0.jpg' },
    { name: 'PHẠM ĐỖ MẠNH ĐỨC', role: 'Student ID: 20235917', imageUrl: 'https://ddragon.leagueoflegends.com/cdn/img/champion/splash/LeeSin_0.jpg' },
    { name: 'BÙI ĐỨC HIẾU', role: 'Student ID: 20235931', imageUrl: 'https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Azir_0.jpg' },
    { name: 'TRẦN HÀ TUẤN MINH', role: 'Student ID: 20235978', imageUrl: 'https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Jinx_0.jpg' },
    { name: 'LÊ MINH TUẤN', role: 'Student ID: 20236007', imageUrl: 'https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Bard_0.jpg' }
  ];

  onMouseMove(event: MouseEvent, card: HTMLElement) {
    const rect = card.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;
    
    const rotateX = ((y - centerY) / centerY) * -15; 
    const rotateY = ((x - centerX) / centerX) * 15;
    
    card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(1.05, 1.05, 1.05)`;
    
    const glare = card.querySelector('.team__glare') as HTMLElement;
    if (glare) {
      glare.style.opacity = '1';
      glare.style.transform = `translate(${x}px, ${y}px)`;
    }
  }

  onMouseLeave(card: HTMLElement) {
    card.style.transform = `perspective(1000px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)`;
    const glare = card.querySelector('.team__glare') as HTMLElement;
    if (glare) {
      glare.style.opacity = '0';
    }
  }
}
