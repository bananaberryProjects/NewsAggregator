const API_BASE_URL = '/api';

export interface Feed {
  id: string;
  name: string;
  url: string;
  description: string | null;
  imageUrl: string | null;
  articleCount: number;
  lastFetchedAt: string | null;
  categoryIds?: string[];
}

export interface Article {
  id: string;
  title: string;
  description: string | null;
  link: string;
  imageUrl: string | null;
  publishedAt: string;
  feedName: string;
  categoryIds?: string[];
}

export interface ArticleReadStatus {
  id: number;
  articleId: string;
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

async function fetchApi<T>(path: string, options?: RequestInit): Promise<T> {
  const headers: HeadersInit = {}
  
  // Nur Content-Type setzen wenn Body vorhanden
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

  // Bei 204 No Content oder leerem Body nicht parsen
  if (response.status === 204 || !response.headers.get('content-type')?.includes('json')) {
    return undefined as T;
  }

  return response.json();
}

export const feedsApi = {
  getAll: () => fetchApi<Feed[]>('/feeds'),
  add: (feed: { name: string; url: string }) => 
    fetchApi<Feed>('/feeds', { method: 'POST', body: JSON.stringify(feed) }),
  delete: (id: string) => 
    fetchApi<void>(`/feeds/${id}`, { method: 'DELETE' }),
  fetchArticles: (id: string) => 
    fetchApi<void>(`/feeds/${id}/fetch`, { 
      method: 'POST',
      headers: {} // Kein Content-Type, da kein Body
    }),
};

export const articlesApi = {
  getAll: () => fetchApi<Article[]>('/articles'),
  search: (query: string) => fetchApi<Article[]>(`/articles?query=${encodeURIComponent(query)}`),
  markAsRead: (id: string) => fetchApi<ArticleReadStatus>(`/articles/${id}/read`, { method: 'POST' }),
  markAsUnread: (id: string) => fetchApi<ArticleReadStatus>(`/articles/${id}/unread`, { method: 'POST' }),
  toggleFavorite: (id: string) => fetchApi<ArticleReadStatus>(`/articles/${id}/favorite`, { method: 'POST' }),
  getStatus: (id: string) => fetchApi<ArticleStatus>(`/articles/${id}/status`),
  getReadArticles: () => fetchApi<ArticleReadStatus[]>('/articles/read'),
  getFavoriteArticles: () => fetchApi<ArticleReadStatus[]>('/articles/favorites'),
};

export const categoriesApi = {
  getAll: () => fetchApi<Category[]>('/categories'),
  create: (category: { name: string; color?: string; icon?: string }) => 
    fetchApi<Category>('/categories', { method: 'POST', body: JSON.stringify(category) }),
  delete: (id: string) => fetchApi<void>(`/categories/${id}`, { method: 'DELETE' }),
};

export const feedCategoriesApi = {
  assign: (feedId: string, categoryIds: string[]) =>
    fetchApi<void>(`/feeds/${feedId}/categories`, { 
      method: 'PUT', 
      body: JSON.stringify({ categoryIds }) 
    }),
};
