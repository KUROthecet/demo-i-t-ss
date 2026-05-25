// Stamp Coupling
// The backend API endpoints accept the entire Media entity as the request body. 
// This full object is then passed directly from MediaController to MediaServiceImpl.addMedia() 
// and updateMedia(). However, the service methods only use a subset of fields 
// (title, currentPrice, originalValue, barcode, status, quantityInStock) while ignoring many others.
/**
 * Media Model — ISP-compliant interface hierarchy for AIMS product catalog.
 *
 * OOP / Interface Segregation Principle (ISP) Design:
 * - Instead of one "God Interface" with 40 optional fields (an Anemic Design anti-pattern),
 *   we define a BaseMedia interface with shared fields, and separate sub-interfaces
 *   for each media type (Book, CD, DVD, Newspaper).
 * - The discriminated union type `Media` allows TypeScript to narrow types
 *   via the `category` field, enabling type-safe access to sub-type fields.
 * - Type guards (isBook, isCD, isDVD, isNewspaper) provide compile-time safety
 *   when working with specific product types.
 *
 * @see BookMedia
 * @see CDMedia
 * @see DVDMedia
 * @see NewspaperMedia
 */

/** Shared fields present on ALL media products (Table 10 from AIMS SRS). */
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
}

/** Book-specific attributes (Table 11 from AIMS SRS). */
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

/** CD-specific attributes (Table 12 from AIMS SRS). */
export interface CDMedia extends BaseMedia {
  category: 'CD';
  artist: string;
  recordLabel?: string;
  trackList?: string;
  releaseDate?: string;
}

/** DVD-specific attributes (Table 13 from AIMS SRS). */
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

/** Newspaper-specific attributes (Table 14 from AIMS SRS). */
export interface NewspaperMedia extends BaseMedia {
  category: 'Newspaper';
  editorInChief?: string;
  issn?: string;
  issueNumber?: string;
  publicationFrequency?: string;
  sections?: string;
}

/**
 * Discriminated union of all media types.
 * TypeScript will narrow this type automatically when checking the `category` field.
 *
 * @example
 * if (isBook(media)) {
 *   console.log(media.author); // type-safe: author is string here
 * }
 */
export type Media = BookMedia | CDMedia | DVDMedia | NewspaperMedia;

// ===== Type Guards =====

/** Returns true if the given media item is a Book. */
export function isBook(media: Media): media is BookMedia {
  return media.category === 'Book';
}

/** Returns true if the given media item is a CD. */
export function isCD(media: Media): media is CDMedia {
  return media.category === 'CD';
}

/** Returns true if the given media item is a DVD. */
export function isDVD(media: Media): media is DVDMedia {
  return media.category === 'DVD';
}

/** Returns true if the given media item is a Newspaper. */
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

/** Cart item extends any media type with a cart quantity. */
export interface CartItem extends BaseMedia {
  cartQty: number;
}
