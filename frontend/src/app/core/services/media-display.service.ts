import { Injectable } from '@angular/core';
import { Media } from '../models/media.model';

type SubtitleExtractor = (m: Media) => string;

@Injectable({ providedIn: 'root' })
export class MediaDisplayService {

  private readonly subtitleExtractors: Record<string, SubtitleExtractor> = {
    Book:      (m) => m.attributes?.['Author'] ?? '',
    CD:        (m) => m.attributes?.['Artist'] ?? '',
    DVD:       (m) => `Dir. ${m.attributes?.['Director'] ?? ''}`,
    Newspaper: (m) => {
      const chief = m.attributes?.['Editor-in-Chief'];
      return chief ? `Ed. ${chief}` : '';
    }
  };

  getImageUrl(media: Media): string {
    return media.imageUrl;
  }

  getSubtitle(media: Media): string {
    const extractor = this.subtitleExtractors[media.category];
    if (extractor) return extractor(media);
    return Object.values(media.attributes ?? {})[0] ?? '';
  }
}
