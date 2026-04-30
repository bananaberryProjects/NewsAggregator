export const API_BASE_URL = '/api';

export interface Feed {
  id: string;
  name: string;
  url: string;
  description: string | null;
  imageUrl: string | null;
  articleCount: number;
  lastFetchedAt: string | null;
  categoryIds?: string[];
  extractContent?: boolean;
  blockedKeywords?: string[];
}

export interface Article {
  id: number; // Changed from string to number (bigint in DB)
  title: string;
  description: string | null;
  link: string;
  imageUrl: string | null;
  publishedAt: string;
  feedName: string;
  categoryIds?: string[];
  contentHtml?: string | null;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface ArticleReadStatus {
  id: number;
  articleId: number; // Changed from string to number (bigint in DB)
  userId: string;
  isRead: boolean;
  isFavorite: boolean;
  readAt: string | null;
  favoritedAt: string | null;
}

export interface ArticleStatus {
  isRead: boolean;
  isFavorite: boolean;
}

export interface Category {
  id: string;
  name: string;
  color: string;
  icon: string;
}

export interface OpmlImportResult {
  importedCount: number;
  failedUrls: string[];
  message?: string;
}

async function fetchApi<T>(path: string, options?: RequestInit): Promise<T> {
  const headers: HeadersInit = {}
  
  if (options?.body) {
    headers['Content-Type'] = 'application/json'
  }
  
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers,
    credentials: 'include',
    ...options,
  });

  if (!response.ok) {
    const text = await response.text();
    console.error(`HTTP error! status: ${response.status}, body: ${text}`);
    throw new Error(`HTTP error! status: ${response.status}${text ? ': ' + text : ''}`);
  }

  if (response.status === 204 || !response.headers.get('content-type')?.includes('json')) {
    return undefined as T;
  }

  return response.json();
}

export async function importOpml(file: File): Promise<OpmlImportResult> {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await fetch(`${API_BASE_URL}/opml/import`, {
    method: 'POST',
    body: formData,
    credentials: 'include',
  });
  
  if (!response.ok) {
    const error = await response.text();
    throw new Error(error || 'OPML Import fehlgeschlagen');
  }
  
  return response.json();
}

export async function exportOpml(): Promise<Blob> {
  const response = await fetch(`${API_BASE_URL}/opml/export`, {
    method: 'GET',
    credentials: 'include',
  });
  
  if (!response.ok) {
    throw new Error('OPML Export fehlgeschlagen');
  }
  
  return response.blob();
}

export function downloadBlob(blob: Blob, filename: string) {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  window.URL.revokeObjectURL(url);
  document.body.removeChild(a);
}

export const feedsApi = {
  getAll: () => fetchApi<Feed[]>('/feeds'),
  add: (feed: { name: string; url: string; blockedKeywords?: string[] }) => 
    fetchApi<Feed>('/feeds', { method: 'POST', body: JSON.stringify(feed) }),
  update: (id: string, feed: { name: string; url: string; description?: string; extractContent?: boolean; blockedKeywords?: string[] }) => 
    fetchApi<Feed>(`/feeds/${id}`, { method: 'PUT', body: JSON.stringify(feed) }),
  delete: (id: string) => 
    fetchApi<void>(`/feeds/${id}`, { method: 'DELETE' }),
  fetchArticles: (id: string) => 
    fetchApi<void>(`/feeds/${id}/fetch`, { 
      method: 'POST',
      headers: {} 
    }),
};

export const articlesApi = {
  getAll: () => fetchApi<Article[]>('/articles'),
  search: (query: string) => fetchApi<Article[]>(`/articles?query=${encodeURIComponent(query)}`),
  searchFullText: (q: string, page: number = 0, size: number = 20, filters: {
    categoryId?: string
    readFilter?: 'READ' | 'UNREAD'
    favoriteFilter?: 'FAVORITE' | 'NOT_FAVORITE'
  } = {}) => {
    let url = `/articles/search?q=${encodeURIComponent(q)}&page=${page}&size=${size}`
    if (filters.categoryId) url += `&categoryId=${encodeURIComponent(filters.categoryId)}`
    if (filters.readFilter) url += `&readFilter=${filters.readFilter}`
    if (filters.favoriteFilter) url += `&favoriteFilter=${filters.favoriteFilter}`
    return fetchApi<Page<Article>>(url)
  },
  getById: (id: number) => fetchApi<Article>(`/articles/${id}`),
  getContent: (id: number) => fetchApi<Article>(`/articles/${id}/content`),
  markAsRead: (id: number) => fetchApi<ArticleReadStatus>(`/articles/${id}/read`, { method: 'POST' }),
  markAsUnread: (id: number) => fetchApi<ArticleReadStatus>(`/articles/${id}/unread`, { method: 'POST' }),
  toggleFavorite: (id: number) => fetchApi<ArticleReadStatus>(`/articles/${id}/favorite`, { method: 'POST' }),
  getStatus: (id: number) => fetchApi<ArticleStatus>(`/articles/${id}/status`),
  getReadArticles: () => fetchApi<ArticleReadStatus[]>('/articles/read'),
  getFavoriteArticles: () => fetchApi<ArticleReadStatus[]>('/articles/favorites'),
};

export const categoriesApi = {
  getAll: () => fetchApi<Category[]>('/categories'),
  create: (category: { name: string; color?: string; icon?: string }) => 
    fetchApi<Category>('/categories', { method: 'POST', body: JSON.stringify(category) }),
  update: (id: string, category: { name: string; color?: string; icon?: string }) => 
    fetchApi<Category>(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(category) }),
  delete: (id: string) => fetchApi<void>(`/categories/${id}`, { method: 'DELETE' }),
};

export const feedCategoriesApi = {
  assign: (feedId: string, categoryIds: string[]) =>
    fetchApi<void>(`/feeds/${feedId}/categories`, { 
      method: 'PUT', 
      body: JSON.stringify({ categoryIds }) 
    }),
};

export interface DailyStats {
  date: string;
  articleCount: number;
  readCount: number;
}

export interface FeedStats {
  feedName: string;
  totalArticles: number;
  readArticles: number;
}

export interface ReadingStatistics {
  totalArticles: number;
  readArticles: number;
  unreadArticles: number;
  favoriteArticles: number;
  readPercentage: number;
  articlesPerDay: DailyStats[];
  articlesPerFeed: FeedStats[];
}

export interface DashboardStats {
  unreadCount: number;
  totalFeeds: number;
  feedsWithNewArticles: number;
  favoriteCount: number;
  newFavoritesToday: number;
  readStreakDays: number;
  articlesReadToday: number;
  lastReadAt: string | null;
}

export const statisticsApi = {
  getStatistics: () => fetchApi<ReadingStatistics>('/stats'),
};

export const dashboardApi = {
  getDashboardStats: () => fetchApi<DashboardStats>('/dashboard/stats'),
};

export interface ExtractionResponse {
  success: boolean;
  message: string;
  errors: unknown;
  processedCount: number;
  successCount: number;
  failedCount: number;
  skippedCount: number;
}

export interface ArticlesWithoutContentCount {
  count: number;
  hasArticlesWithoutContent: boolean;
}

export const adminApi = {
  extractContent: (limit: number = 50, delayMs: number = 2000) =>
    fetchApi<ExtractionResponse>(`/admin/articles/extract-content?limit=${limit}&delayMs=${delayMs}`, { method: 'POST' }),
  getArticlesWithoutContentCount: () =>
    fetchApi<ArticlesWithoutContentCount>('/admin/articles/without-content/count'),
};

// ── Weather Insight ──

export interface WeatherInsight {
  temperature: number;
  weatherCode: number;
  description: string;
  todayMin: number;
  todayMax: number;
  city: string;
  insight: string;
  forecast: WeatherForecastDay[];
  generatedAt: string;
}

export interface WeatherForecastDay {
  day: string;
  maxTemp: number;
  minTemp: number;
  weatherCode: number;
}

export const weatherApi = {
  getInsight: (lat: number, lon: number, city: string) =>
    fetchApi<WeatherInsight>(`/weather/insight?lat=${lat}&lon=${lon}&city=${encodeURIComponent(city)}`),
};

