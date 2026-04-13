import axios from 'axios';
import { type Feed, type Article, type AddFeedRequest } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Feeds
export const getFeeds = (): Promise<Feed[]> =>
  api.get('/feeds').then(r => r.data);

export const getFeed = (id: number): Promise<Feed> =>
  api.get(`/feeds/${id}`).then(r => r.data);

export const addFeed = (feed: AddFeedRequest): Promise<Feed> =>
  api.post('/feeds', feed).then(r => r.data);

export const deleteFeed = (id: number): Promise<void> =>
  api.delete(`/feeds/${id}`).then(r => r.data);

export const fetchFeed = (id: number): Promise<void> =>
  api.post(`/feeds/${id}/fetch`).then(r => r.data);

// Articles
export const getArticles = (): Promise<Article[]> =>
  api.get('/articles').then(r => r.data);

export const getArticle = (id: number): Promise<Article> =>
  api.get(`/articles/${id}`).then(r => r.data);

export const getArticlesByFeed = (feedId: number): Promise<Article[]> =>
  api.get(`/articles/feed/${feedId}`).then(r => r.data);

export const searchArticles = (query: string): Promise<Article[]> =>
  api.get('/articles/search', { params: { query } }).then(r => r.data);

export default api;
