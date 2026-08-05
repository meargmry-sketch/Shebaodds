// utils/ethiopianCalendar.js
import EthiopianDate from 'ethiopian-date';

export const ethiopianMonths = {
  en: ['Meskerem', 'Tikimt', 'Hidar', 'Tahsas', 'Tir', 'Yekatit', 'Megabit', 'Miyazya', 'Ginbot', 'Sene', 'Hamle', 'Nehase', 'Pagume'],
  am: ['መስከረም', 'ጥቅምት', 'ህዳር', 'ታህሳስ', 'ጥር', 'የካቲት', 'መጋቢት', 'ሚያዝያ', 'ግንቦት', 'ሰኔ', 'ሐምሌ', 'ነሐሴ', 'ጳጉሜ']
};

export const ethiopianDays = {
  en: ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'],
  am: ['እሑድ', 'ሰኞ', 'ማክሰኞ', 'ረቡዕ', 'ሐሙስ', 'ዓርብ', 'ቅዳሜ']
};

export function toEthiopian(date) {
  // date can be Date object or ISO string
  const d = new Date(date);
  const year = d.getFullYear();
  const month = d.getMonth() + 1; // 1-12
  const day = d.getDate();
  // Use EthiopianDate library
  const eth = EthiopianDate.toEthiopian(year, month, day);
  return { year: eth.year, month: eth.month, day: eth.day };
}

export function getEthiopianMonthName(month, lang = 'en') {
  const months = lang === 'am' ? ethiopianMonths.am : ethiopianMonths.en;
  return months[month - 1] || '';
}

export function getEthiopianDayName(dayIndex, lang = 'en') {
  const days = lang === 'am' ? ethiopianDays.am : ethiopianDays.en;
  return days[dayIndex] || '';
}

export function getDaysInEthiopianMonth(year, month) {
  // Months 1-12 have 30 days; month 13 (Pagume) has 5 or 6 depending on leap year.
  if (month >= 1 && month <= 12) return 30;
  if (month === 13) {
    // Ethiopian leap year: every 4 years
    return (year % 4 === 0) ? 6 : 5;
  }
  return 0;
}