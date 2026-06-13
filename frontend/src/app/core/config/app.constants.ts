export const PAYPAL_CLIENT_ID =
  'AS80_ZkaQeM3a3jW8ymmla5sNV-j0j5wiyh2nvRfhtFn5x1dFZ26UgpWL7yB6eJMW-FUc1-3LQr3LRVB';

export const VIETQR = {
  bankId:      'MB',
  accountNo:   '0975452106',
  accountName: 'BUI TRUNG HIEU'
} as const;

export class AppConstants {
  static readonly PAGE_SIZE_PENDING_ORDERS        = 30;
  static readonly PAGE_SIZE_PRODUCTS              = 20;
  static readonly PAGE_SIZE_SEARCH                = 20;
  static readonly NOTIFICATION_POLL_INTERVAL_MS   = 30_000;
  static readonly MAX_BATCH_DELETE                = 10;
  static readonly MAX_DAILY_DELETE                = 20;
  static readonly DAILY_DELETE_WARNING_THRESHOLD  = 15;
  static readonly VAT_RATE                        = 0.10;
  static readonly PRICE_MIN_RATIO                 = 0.30;
  static readonly PRICE_MAX_RATIO                 = 1.50;
  static readonly FREE_SHIPPING_THRESHOLD         = 100_000;
  static readonly FREE_SHIPPING_CAP               = 25_000;
}
