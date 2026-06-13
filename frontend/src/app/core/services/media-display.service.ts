import { Injectable } from '@angular/core';
import { Media, BookMedia, CDMedia, DVDMedia, NewspaperMedia } from '../models/media.model';

type SubtitleExtractor = (m: Media) => string;

@Injectable({ providedIn: 'root' })
export class MediaDisplayService {

  private readonly subtitleExtractors: Record<string, SubtitleExtractor> = {
    Book:      (m) => (m as BookMedia).author ?? '',
    CD:        (m) => (m as CDMedia).artist ?? '',
    DVD:       (m) => `Dir. ${(m as DVDMedia).director ?? ''}`,
    Newspaper: (m) => {
      const chief = (m as NewspaperMedia).editorInChief;
      return chief ? `Ed. ${chief}` : '';
    }
  };

  private readonly fallbackImages: Record<string, string> = {
    Book:      'https://images.unsplash.com/photo-1544947950-fa07a98d237f?auto=format&fit=crop&w=600&q=80',
    CD:        'https://images.unsplash.com/photo-1619983081563-430f63602796?auto=format&fit=crop&w=600&q=80',
    DVD:       'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?auto=format&fit=crop&w=600&q=80',
    Newspaper: 'https://images.unsplash.com/photo-1504711434969-e33886168f5c?auto=format&fit=crop&w=600&q=80',
  };

  getImageUrl(media: Media): string {
    if (media.imageUrl) return media.imageUrl;
    return this.getFallbackImage(media.category);
  }

  getSubtitle(media: Media): string {
    const extractor = this.subtitleExtractors[media.category];
    return extractor ? extractor(media) : '';
  }

  getFallbackImage(category: string): string {
    return this.fallbackImages[category] ?? this.fallbackImages['Book'];
  }
}
