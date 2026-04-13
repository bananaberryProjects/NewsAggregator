export interface Feed {
  id: number;
  name: string;
  url: string;
  description: string;
  lastFetched: string;
  createdAt: string;
  status: string;
  articleCount: number;
}

export interface Article {
  id: number;
  title: string;
  description: string;
  link: string;
  imageUrl: string | null;
  publishedAt: string;
  createdAt: string;
  feedId: number;
  feedName: string;
}

export interface AddFeedRequest {
  name: string;
  url: string;
  description: string;
}
