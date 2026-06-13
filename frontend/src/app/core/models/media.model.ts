export interface BaseMedia {
  readonly id: number;
  readonly barcode: string;
  title: string;
  category: 'Book' | 'CD' | 'DVD' | 'Newspaper';
  originalPrice: number;
  currentPrice: number;
  generalDescription: string;
  dimensions: string;
  weight: number;
  imageUrl: string;
  quantityInStock: number;
  status: 'ACTIVE' | 'DEACTIVATED';
  supportRushDelivery: boolean;
  attributes?: Record<string, string>;
}

export interface BookMedia extends BaseMedia {
  category: 'Book';
  author: string;
  coverType?: string;
  publicationDate?: string;
  publisher?: string;
  genre?: string;
  language?: string;
  numberOfPages?: number;
}

export interface CDMedia extends BaseMedia {
  category: 'CD';
  artist: string;
  recordLabel?: string;
  trackList?: string;
  genre?: string;
  releaseDate?: string;
}

export interface DVDMedia extends BaseMedia {
  category: 'DVD';
  director: string;
  discType?: string;
  language?: string;
  runtimeMinutes?: number;
  studio?: string;
  subtitles?: string;
  genre?: string;
  releaseDate?: string;
}

export interface NewspaperMedia extends BaseMedia {
  category: 'Newspaper';
  editorInChief?: string;
  publisher?: string;
  publicationDate?: string;
  issueNumber?: string;
  publicationFrequency?: string;
  issn?: string;
  language?: string;
  sections?: string;
}

export type Media = BookMedia | CDMedia | DVDMedia | NewspaperMedia;

export function isBook(media: Media): media is BookMedia {
  return media.category === 'Book';
}

export function isCD(media: Media): media is CDMedia {
  return media.category === 'CD';
}

export function isDVD(media: Media): media is DVDMedia {
  return media.category === 'DVD';
}

export function isNewspaper(media: Media): media is NewspaperMedia {
  return media.category === 'Newspaper';
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface CartItem extends BaseMedia {
  cartQty: number;
}
