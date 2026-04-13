import { type Article } from '../types';
import { Calendar, ExternalLink, Newspaper } from 'lucide-react';

interface ArticleCardProps {
  article: Article;
}

export function ArticleCard({ article }: ArticleCardProps) {
  const placeholderImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300"%3E%3Crect fill="%23e5e7eb" width="400" height="300"/%3E%3Ctext fill="%239ca3af" font-family="sans-serif" font-size="20" dy="10.5" font-weight="bold" x="50%25" y="50%25" text-anchor="middle"%3E📰 Kein Bild%3C/text%3E%3C/svg%3E';

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200 hover:shadow-lg transition-shadow">
      <img
        src={article.imageUrl || placeholderImage}
        alt={article.title}
        className="w-full h-48 object-cover"
        onError={(e) => {
          (e.target as HTMLImageElement).src = placeholderImage;
        }}
      />
      <div className="p-4">
        <div className="flex items-center gap-2 text-sm text-gray-500 mb-2">
          <Newspaper className="w-4 h-4" />
          <span>{article.feedName}</span>
          <span className="mx-1">•</span>
          <Calendar className="w-4 h-4" />
          <span>{new Date(article.publishedAt).toLocaleDateString('de-DE')}</span>
        </div>
        <h3 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
          {article.title}
        </h3>
        <p className="text-gray-600 text-sm line-clamp-3 mb-4">
          {article.description}
        </p>
        <a
          href={article.link}
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-1 text-blue-600 hover:text-blue-800 text-sm font-medium"
        >
          Weiterlesen
          <ExternalLink className="w-4 h-4" />
        </a>
      </div>
    </div>
  );
}
