import { useState, useEffect } from 'react';
import { type Feed, type AddFeedRequest } from '../types';
import { getFeeds, addFeed, deleteFeed, fetchFeed } from '../api/client';
import { FeedCard } from '../components/FeedCard';
import { AddFeedModal } from '../components/AddFeedModal';
import { Plus, Loader2 } from 'lucide-react';

export function FeedsPage() {
  const [feeds, setFeeds] = useState<Feed[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadFeeds = async () => {
    try {
      setLoading(true);
      const data = await getFeeds();
      setFeeds(data);
      setError(null);
    } catch (err) {
      setError('Fehler beim Laden der Feeds');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadFeeds();
  }, []);

  const handleAddFeed = async (feed: AddFeedRequest) => {
    try {
      await addFeed(feed);
      await loadFeeds();
    } catch (err) {
      alert('Fehler beim Hinzufügen des Feeds');
    }
  };

  const handleDeleteFeed = async (id: number) => {
    if (!confirm('Möchtest du diesen Feed wirklich löschen? Alle Artikel werden ebenfalls gelöscht.')) {
      return;
    }
    try {
      await deleteFeed(id);
      await loadFeeds();
    } catch (err) {
      alert('Fehler beim Löschen des Feeds');
    }
  };

  const handleRefreshFeed = async (id: number) => {
    try {
      await fetchFeed(id);
      await loadFeeds();
    } catch (err) {
      alert('Fehler beim Aktualisieren des Feeds');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
      </div>
    );
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Feeds</h1>
          <p className="text-gray-600">{feeds.length} Feeds • {feeds.reduce((sum, f) => sum + f.articleCount, 0)} Artikel</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-5 h-5" />
          Feed hinzufügen
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
          {error}
        </div>
      )}

      <div className="grid gap-4">
        {feeds.map((feed) => (
          <FeedCard
            key={feed.id}
            feed={feed}
            onDelete={handleDeleteFeed}
            onRefresh={handleRefreshFeed}
          />
        ))}
      </div>

      <AddFeedModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onAdd={handleAddFeed}
      />
    </div>
  );
}
