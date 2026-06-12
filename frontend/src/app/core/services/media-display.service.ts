import { Injectable } from '@angular/core';
import { Media, isBook, isCD, isDVD, isNewspaper } from '../models/media.model';

@Injectable({ providedIn: 'root' })
export class MediaDisplayService {
  getImageUrl(media: Media): string {
    if (media.imageUrl) return media.imageUrl;
    return this.getFallbackImage(media.category);
  }

  getSubtitle(media: Media): string {
    if (isBook(media))      return media.author ?? '';
    if (isCD(media))        return media.artist ?? '';
    if (isDVD(media))       return media.director ?? '';
    if (isNewspaper(media)) return media.editorInChief ? `Ed. ${media.editorInChief}` : '';
    return '';
  }

  getFallbackImage(category: string): string {
    const fallbacks: Record<string, string> = {
      Book:      'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=600&q=80',
      CD:        'https://images.unsplash.com/photo-1619983081563-430f63602796?auto=format&fit=crop&w=600&q=80',
      DVD:       'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80',
      Newspaper: 'https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=600&q=80',
    };
    return fallbacks[category] ?? fallbacks['Book'];
  }
}
