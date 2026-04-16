import { useState, useEffect, useMemo } from 'react'
import {
  ThemeProvider,
  CssBaseline,
  Box,
  Container,
  Fab,
} from '@mui/material'
import { Add as AddIcon } from '@mui/icons-material'
import { useTheme, useFeeds, useArticles, useCategories } from './hooks'
import { Sidebar } from './components'
import { DashboardView, FeedsView, ArticlesView, FavoritesView, CategoriesView, StatisticsView } from './components/views'
import { AddFeedDialog, DeleteFeedDialog } from './components/dialogs'
import type { Feed } from './api/client'

const drawerWidth = 280

function App() {
  const { theme, isDark, toggleTheme } = useTheme()
  const { feeds, loading: feedsLoading, error: feedsError, loadFeeds, addFeed, deleteFeed, refreshFeed } = useFeeds()
  const { articles, articleStatuses, loadArticles, toggleRead, toggleFavorite, updatingArticleId } = useArticles()
  const { categories, loadCategories, deleteCategory } = useCategories()

  const [mobileOpen, setMobileOpen] = useState(false)
  const [activeView, setActiveView] = useState<'dashboard' | 'feeds' | 'articles' | 'favorites' | 'categories' | 'statistics'>('dashboard')

  // Filter states
  const [dashboardFilter, setDashboardFilter] = useState<'all' | 'unread' | 'favorites'>('all')
  const [dashboardCategoryFilter, setDashboardCategoryFilter] = useState<string[]>([])
  const [articlesFilter, setArticlesFilter] = useState<'all' | 'unread' | 'favorites'>('all')
  const [articlesCategoryFilter, setArticlesCategoryFilter] = useState<string[]>([])

  // Dialog states
  const [addDialogOpen, setAddDialogOpen] = useState(false)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [feedToDelete, setFeedToDelete] = useState<Feed | null>(null)

  // Add feed form
  const [newFeedUrl, setNewFeedUrl] = useState('')
  const [newFeedName, setNewFeedName] = useState('')
  const [selectedCategories, setSelectedCategories] = useState<string[]>([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    await Promise.all([loadFeeds(), loadArticles(), loadCategories()])
  }

  const favoriteCount = useMemo(() => {
    return Object.values(articleStatuses).filter(s => s.isFavorite).length
  }, [articleStatuses])

  const handleAddFeed = async () => {
    if (!newFeedUrl.trim()) return
    await addFeed(newFeedUrl, newFeedName, selectedCategories)
    setAddDialogOpen(false)
    setNewFeedUrl('')
    setNewFeedName('')
    setSelectedCategories([])
  }

  const handleDeleteFeed = async () => {
    if (!feedToDelete) return
    await deleteFeed(feedToDelete.id)
    setDeleteDialogOpen(false)
    setFeedToDelete(null)
  }

  const renderContent = () => {
    const loading = feedsLoading
    const error = feedsError

    switch (activeView) {
      case 'dashboard':
        return (
          <DashboardView
            feeds={feeds}
            articles={articles}
            categories={categories}
            loading={loading}
            error={error}
            articleStatuses={articleStatuses}
            updatingArticleId={updatingArticleId}
            dashboardFilter={dashboardFilter}
            dashboardCategoryFilter={dashboardCategoryFilter}
            onFilterChange={setDashboardFilter}
            onCategoryFilterChange={setDashboardCategoryFilter}
            onToggleRead={toggleRead}
            onToggleFavorite={toggleFavorite}
          />
        )
      case 'feeds':
        return (
          <FeedsView
            feeds={feeds}
            loading={loading}
            refreshingFeedId={null}
            onRefresh={refreshFeed}
            onDelete={(feed) => {
              setFeedToDelete(feed)
              setDeleteDialogOpen(true)
            }}
            onImportSuccess={loadFeeds}
          />
        )
      case 'articles':
        return (
          <ArticlesView
            articles={articles}
            categories={categories}
            loading={loading}
            articleStatuses={articleStatuses}
            updatingArticleId={updatingArticleId}
            articlesFilter={articlesFilter}
            articlesCategoryFilter={articlesCategoryFilter}
            onFilterChange={setArticlesFilter}
            onCategoryFilterChange={setArticlesCategoryFilter}
            onToggleRead={toggleRead}
            onToggleFavorite={toggleFavorite}
          />
        )
      case 'favorites':
        return (
          <FavoritesView
            articles={articles}
            loading={loading}
            articleStatuses={articleStatuses}
            updatingArticleId={updatingArticleId}
            onToggleRead={toggleRead}
            onToggleFavorite={toggleFavorite}
          />
        )
      case 'categories':
        return (
          <CategoriesView
            categories={categories}
            loading={loading}
            onDelete={deleteCategory}
          />
        )
      case 'statistics':
        return <StatisticsView />
      default:
        return null
    }
  }

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex' }}>
        <Sidebar
          mobileOpen={mobileOpen}
          setMobileOpen={setMobileOpen}
          activeView={activeView}
          setActiveView={setActiveView}
          feeds={feeds}
          categories={categories}
          articleCount={articles.length}
          favoriteCount={favoriteCount}
          refreshingFeedId={null}
          onRefreshFeed={refreshFeed}
          onDeleteFeed={(feed) => {
            setFeedToDelete(feed)
            setDeleteDialogOpen(true)
          }}
          onAssignCategories={() => {}}
          isDark={isDark}
          onToggleTheme={toggleTheme}
        />

        <Box
          component="main"
          sx={{
            flexGrow: 1,
            p: 3,
            width: { sm: `calc(100% - ${drawerWidth}px)` },
            minHeight: '100vh',
            bgcolor: 'background.default',
          }}
        >
          <Container maxWidth="xl">
            {renderContent()}
          </Container>
        </Box>

        <Fab
          color="primary"
          sx={{ position: 'fixed', bottom: 24, right: 24 }}
          onClick={() => setAddDialogOpen(true)}
        >
          <AddIcon />
        </Fab>

        <AddFeedDialog
          open={addDialogOpen}
          onClose={() => {
            setAddDialogOpen(false)
            setNewFeedUrl('')
            setNewFeedName('')
            setSelectedCategories([])
          }}
          onSubmit={handleAddFeed}
          url={newFeedUrl}
          setUrl={setNewFeedUrl}
          name={newFeedName}
          setName={setNewFeedName}
          selectedCategories={selectedCategories}
          toggleCategory={(id) => {
            setSelectedCategories(prev =>
              prev.includes(id) ? prev.filter(c => c !== id) : [...prev, id]
            )
          }}
          categories={categories}
          loading={feedsLoading}
          error={feedsError}
        />

        <DeleteFeedDialog
          open={deleteDialogOpen}
          onClose={() => {
            setDeleteDialogOpen(false)
            setFeedToDelete(null)
          }}
          onConfirm={handleDeleteFeed}
          feed={feedToDelete}
          loading={feedsLoading}
          error={feedsError}
        />
      </Box>
    </ThemeProvider>
  )
}

export default App
