import { type Feed } from '../types';
import { Rss, Trash2, RefreshCw, ExternalLink } from 'lucide-react';

interface FeedCardProps {
  feed: Feed;
  onDelete: (id: number) => void;
  onRefresh: (id: number) => void;
}

export function FeedCard({ feed, onDelete, onRefresh }: FeedCardProps) {
  return (
    <div className="bg-white rounded-lg shadow-md p-4 border border-gray-200 hover:shadow-lg transition-shadow">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
            <Rss className="w-5 h-5 text-blue-500" />
            {feed.name}
          </h3>
          <p className="text-sm text-gray-600 mt-1">{feed.description}</p>
          <a 
            href={feed.url} 
            target="_blank" 
            rel="noopener noreferrer"
            className="text-xs text-blue-500 hover:underline flex items-center gap-1 mt-2"
          >
            {feed.url.substring(0, 50)}...
            <ExternalLink className="w-3 h-3" />
          </a>
        </div>
        <div className="flex flex-col items-end gap-2">
          <span className="bg-blue-100 text-blue-800 text-sm font-medium px-2.5 py-0.5 rounded">
            {feed.articleCount} Artikel
          </span>
          <span className="text-xs text-gray-500">
            {new Date(feed.lastFetched).toLocaleDateString('de-DE')}
          </span>
        </div>
      </div>
      
      <div className="flex gap-2 mt-4 pt-4 border-t border-gray-100">
        <button
          onClick={() => onRefresh(feed.id)}
          className="flex items-center gap-1 px-3 py-1.5 text-sm text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
        >
          <RefreshCw className="w-4 h-4" />
          Aktualisieren
        </button>
        <button
          onClick={() => onDelete(feed.id)}
          className="flex items-center gap-1 px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 rounded-md transition-colors ml-auto"
        >
          <Trash2 className="w-4 h-4" />
          Löschen
        </button>
      </div>
    </div>
  );
}
