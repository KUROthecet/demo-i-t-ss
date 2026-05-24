import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'vndCurrency',
  standalone: true,
  pure: true // Stateless: same input always produces same output — safe for memoization
})
export class VndCurrencyPipe implements PipeTransform {

  private static readonly FORMATTER = new Intl.NumberFormat('vi-VN', {
    style:    'currency',
    currency: 'VND'
  });

  transform(value: number | null | undefined): string {
    if (value == null) return '0 ₫';
    return VndCurrencyPipe.FORMATTER.format(value);
  }
}
