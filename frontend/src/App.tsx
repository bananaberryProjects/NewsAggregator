import { useState, useEffect, useMemo } from 'react'

function getInitialFilterState(storageKey: string): 'all' | 'unread' | 'favorites' {
  try {
    const saved = localStorage.getItem(storageKey)
    if (saved) {
      const validFilters: Array<'all' | 'unread' | 'favorites'> = ['all', 'unread', 'favorites']
      if (validFilters.includes(saved as 'all' | 'unread' | 'favorites')) {
        return saved as 'all' | 'unread' | 'favorites'
      }
    }
  } catch {
    // localStorage nicht verfügbar, Fallback auf 'all'
  }
  return 'all'
}
import {
  ThemeProvider,
  CssBaseline,
  Box,
  Container,
  Fab,
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  useMediaQuery,
} from '@mui/material'
import { Add as AddIcon, Menu as MenuIcon } from '@mui/icons-material'
import { useTheme, useFeeds, useArticles, useCategories } from './hooks'
import { Sidebar } from './components'
import { DashboardView, FeedsView, ArticlesView, FavoritesView, CategoriesView, StatisticsView } from './components/views'
import { AddFeedDialog, DeleteFeedDialog, EditFeedCategoriesDialog } from './components/dialogs'
import type { Feed } from './api/client'

const drawerWidth = 280

function App() {
  const { theme, isDark, toggleTheme } = useTheme()
  const { feeds, loading: feedsLoading, error: feedsError, loadFeeds, addFeed, deleteFeed, refreshFeed, assignCategories } = useFeeds()
  const { articles, articleStatuses, loadArticles, toggleRead, toggleFavorite, updatingArticleId } = useArticles()
  const { categories, loadCategories, deleteCategory } = useCategories()

  const [mobileOpen, setMobileOpen] = useState(false)
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [activeView, setActiveView] = useState<'dashboard' | 'feeds' | 'articles' | 'favorites' | 'categories' | 'statistics'>('dashboard')

  // Filter states - initial values from localStorage
  const [dashboardFilter, setDashboardFilter] = useState<'all' | 'unread' | 'favorites'>(() => getInitialFilterState('dashboard-filter'))
  const [dashboardCategoryFilter, setDashboardCategoryFilter] = useState<string[]>([])
  const [articlesFilter, setArticlesFilter] = useState<'all' | 'unread' | 'favorites'>(() => getInitialFilterState('articles-filter'))
  const [articlesCategoryFilter, setArticlesCategoryFilter] = useState<string[]>([])

  // Dialog states
  const [addDialogOpen, setAddDialogOpen] = useState(false)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [feedToDelete, setFeedToDelete] = useState<Feed | null>(null)

  // Edit categories dialog state
  const [editCategoriesDialogOpen, setEditCategoriesDialogOpen] = useState(false)
  const [feedToEditCategories, setFeedToEditCategories] = useState<Feed | null>(null)
  const [editSelectedCategories, setEditSelectedCategories] = useState<string[]>([])

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

  const handleOpenEditCategories = (feed: Feed) => {
    setFeedToEditCategories(feed)
    setEditSelectedCategories(feed.categoryIds || [])
    setEditCategoriesDialogOpen(true)
  }

  const handleCloseEditCategories = () => {
    setEditCategoriesDialogOpen(false)
    setFeedToEditCategories(null)
    setEditSelectedCategories([])
  }

  const handleSaveCategories = async () => {
    if (!feedToEditCategories) return
    await assignCategories(feedToEditCategories.id, editSelectedCategories)
    setEditCategoriesDialogOpen(false)
    setFeedToEditCategories(null)
    setEditSelectedCategories([])
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
        {/* Mobile AppBar */}
        {isMobile && (
          <AppBar
            position="fixed"
            sx={{
              display: { md: 'none' },
              zIndex: (theme) => theme.zIndex.drawer + 1,
            }}
          >
            <Toolbar>
              <IconButton
                color="inherit"
                aria-label="open drawer"
                edge="start"
                onClick={() => setMobileOpen(true)}
                sx={{ mr: 2 }}
              >
                <MenuIcon />
              </IconButton>
              <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
                News Aggregator
              </Typography>
            </Toolbar>
          </AppBar>
        )}

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
          onAssignCategories={handleOpenEditCategories}
          isDark={isDark}
          onToggleTheme={toggleTheme}
          // Category filter - use appropriate filter based on active view
          activeCategoryFilter={activeView === 'articles' ? articlesCategoryFilter : dashboardCategoryFilter}
          onCategoryFilterChange={(categoryIds) => {
            if (activeView === 'articles') {
              setArticlesCategoryFilter(categoryIds)
            } else {
              // Apply to dashboard for dashboard, feeds, favorites, categories, statistics views
              setDashboardCategoryFilter(categoryIds)
            }
          }}
        />

        <Box
          component="main"
          sx={{
            flexGrow: 1,
            p: { xs: 0, sm: 3 },
            width: { md: `calc(100% - ${drawerWidth}px)` },
            minHeight: '100vh',
            bgcolor: 'background.default',
          }}
        >
          {/* Spacer for mobile AppBar */}
          {isMobile && <Toolbar />}
          <Container maxWidth="xl" sx={{ px: { xs: 0, sm: 3 } }}>
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

        <EditFeedCategoriesDialog
          open={editCategoriesDialogOpen}
          onClose={handleCloseEditCategories}
          onSubmit={handleSaveCategories}
          feed={feedToEditCategories}
          selectedCategories={editSelectedCategories}
          toggleCategory={(id) => {
            setEditSelectedCategories(prev =>
              prev.includes(id) ? prev.filter(c => c !== id) : [...prev, id]
            )
          }}
          categories={categories}
          loading={feedsLoading}
        />
      </Box>
    </ThemeProvider>
  )
}

export default App
