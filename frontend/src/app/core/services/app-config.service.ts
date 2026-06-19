import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { lastValueFrom } from 'rxjs';
import { catchError, of } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AppConfig {
  readonly vatRate: number;
  readonly freeShippingThreshold: number;
  readonly freeShippingCap: number;
  readonly priceMinRatio: number;
  readonly priceMaxRatio: number;
  readonly rushEligibleProvinces: string[];
}

const DEFAULT_CONFIG: AppConfig = {
  vatRate:               0.10,
  freeShippingThreshold: 100_000,
  freeShippingCap:       25_000,
  priceMinRatio:         0.30,
  priceMaxRatio:         1.50,
  rushEligibleProvinces: ['Hanoi', 'Ho Chi Minh City']
};

@Injectable({ providedIn: 'root' })
export class AppConfigService {
  private config: AppConfig = { ...DEFAULT_CONFIG };

  constructor(private readonly http: HttpClient) {}

  loadConfig(): Promise<void> {
    return lastValueFrom(
      this.http.get<AppConfig>(`${environment.apiUrl}/config`).pipe(
        catchError(() => of(DEFAULT_CONFIG))
      )
    ).then(cfg => { this.config = cfg; });
  }

  get vatRate(): number { return this.config.vatRate; }
  get freeShippingThreshold(): number { return this.config.freeShippingThreshold; }
  get freeShippingCap(): number { return this.config.freeShippingCap; }
  get priceMinRatio(): number { return this.config.priceMinRatio; }
  get priceMaxRatio(): number { return this.config.priceMaxRatio; }
  get rushEligibleProvinces(): string[] { return this.config.rushEligibleProvinces; }
}
