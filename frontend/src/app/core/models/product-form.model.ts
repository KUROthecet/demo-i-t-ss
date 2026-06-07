export interface ProductFormModel {
  category: 'Book' | 'CD' | 'DVD' | 'Newspaper';

  title: string;
  barcode: string;
  originalPrice: number;
  currentPrice: number;
  generalDescription: string;
  dimensions: string;
  weight: number;
  imageUrl: string;
  quantityInStock: number;
  supportRushDelivery: boolean;

  author?: string;
  coverType?: string;
  publicationDate?: string;
  publisher?: string;
  genre?: string;
  language?: string;
  numberOfPages?: number;

  artist?: string;
  recordLabel?: string;
  trackList?: string;
  releaseDate?: string;

  director?: string;
  discType?: string;
  runtimeMinutes?: number;
  studio?: string;
  subtitles?: string;

  editorInChief?: string;
  issn?: string;
  issueNumber?: string;
  publicationFrequency?: string;
  sections?: string;
}

export function createEmptyProductForm(): ProductFormModel {
  return {
    category:          'Book',
    title:             '',
    barcode:           '',
    originalPrice:     0,
    currentPrice:      0,
    generalDescription:'',
    dimensions:        '',
    weight:            0,
    imageUrl:          '',
    quantityInStock:   0,
    supportRushDelivery: false,
    author: '', coverType: 'Paperback', publicationDate: '', publisher: '',
    genre: '', language: '', numberOfPages: 0,
    artist: '', recordLabel: '', trackList: '', releaseDate: '',
    director: '', discType: 'Blu-ray', runtimeMinutes: 0, studio: '', subtitles: '',
    editorInChief: '', issn: '', issueNumber: '', publicationFrequency: '', sections: ''
  };
}
