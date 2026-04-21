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
import { DashboardView, FeedsView, ArticlesView, FavoritesView, CategoriesView, StatisticsView, SettingsView } from './components/views'
import { AddFeedDialog, DeleteFeedDialog, EditFeedDialog, EditCategoryDialog, AddCategoryDialog } from './components/dialogs'
import { ArticleReaderDialog } from './components/ArticleReaderDialog'
import { ContentExtractionDialog } from './components/ContentExtractionDialog'
import type { Feed, Category, Article } from './api/client'
import { adminApi } from './api/client'

const drawerWidth = 280

function App() {
  const { theme, isDark, toggleTheme } = useTheme()
  const { feeds, loading: feedsLoading, error: feedsError, loadFeeds, addFeed, deleteFeed, refreshFeed, assignCategories, updateFeed } = useFeeds()
  const { articles, articleStatuses, loadArticles, toggleRead, toggleFavorite, updatingArticleId } = useArticles()
  const { categories, loadCategories, deleteCategory, updateCategory, addCategory } = useCategories()

  const [mobileOpen, setMobileOpen] = useState(false)
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  const [activeView, setActiveView] = useState<'dashboard' | 'feeds' | 'articles' | 'favorites' | 'categories' | 'statistics' | 'settings'>('dashboard')

  // Filter states - initial values from localStorage
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [_dashboardFilter, _setDashboardFilter] = useState<'all' | 'unread' | 'favorites'>(() => getInitialFilterState('dashboard-filter'))
  const [dashboardCategoryFilter, setDashboardCategoryFilter] = useState<string[]>([])
  const [articlesFilter, setArticlesFilter] = useState<'all' | 'unread' | 'favorites'>(() => getInitialFilterState('articles-filter'))
  const [articlesCategoryFilter, setArticlesCategoryFilter] = useState<string[]>([])

  // Dialog states
  const [addDialogOpen, setAddDialogOpen] = useState(false)
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [feedToDelete, setFeedToDelete] = useState<Feed | null>(null)

  // Edit feed dialog state
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [feedToEdit, setFeedToEdit] = useState<Feed | null>(null)
  const [editSelectedCategories, setEditSelectedCategories] = useState<string[]>([])

  // Edit category dialog state
  const [editCategoryDialogOpen, setEditCategoryDialogOpen] = useState(false)
  const [categoryToEdit, setCategoryToEdit] = useState<Category | null>(null)

  // Add category dialog state
  const [addCategoryDialogOpen, setAddCategoryDialogOpen] = useState(false)

  // Reader dialog state
  const [readerDialogOpen, setReaderDialogOpen] = useState(false)
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null)

  // Content extraction dialog state
  const [extractionDialogOpen, setExtractionDialogOpen] = useState(false)
  const [articlesWithoutContent, setArticlesWithoutContent] = useState(0)

  const loadArticlesWithoutContentCount = async () => {
    try {
      const response = await adminApi.getArticlesWithoutContentCount()
      setArticlesWithoutContent(response.count)
    } catch (error) {
      console.error('Failed to load articles without content count:', error)
    }
  }

  // Add feed form
  const [newFeedUrl, setNewFeedUrl] = useState('')
  const [newFeedName, setNewFeedName] = useState('')
  const [selectedCategories, setSelectedCategories] = useState<string[]>([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    await Promise.all([loadFeeds(), loadArticles(), loadCategories()])
    await loadArticlesWithoutContentCount()
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

  const handleOpenEditDialog = (feed: Feed) => {
    setFeedToEdit(feed)
    setEditSelectedCategories(feed.categoryIds || [])
    setEditDialogOpen(true)
  }

  const handleCloseEditDialog = () => {
    setEditDialogOpen(false)
    setFeedToEdit(null)
    setEditSelectedCategories([])
  }

  const handleUpdateFeed = async (name: string, url: string, description: string, categoryIds: string[], extractContent: boolean) => {
    if (!feedToEdit) return
    await updateFeed(feedToEdit.id, name, url, description, extractContent)
    await assignCategories(feedToEdit.id, categoryIds)
    setEditDialogOpen(false)
    setFeedToEdit(null)
    setEditSelectedCategories([])
  }

  const handleOpenEditCategoryDialog = (category: Category) => {
    setCategoryToEdit(category)
    setEditCategoryDialogOpen(true)
  }

  const handleCloseEditCategoryDialog = () => {
    setEditCategoryDialogOpen(false)
    setCategoryToEdit(null)
  }

  const handleUpdateCategory = async (name: string, color: string, icon: string) => {
    if (!categoryToEdit) return
    await updateCategory(categoryToEdit.id, name, color, icon)
    setEditCategoryDialogOpen(false)
    setCategoryToEdit(null)
  }

  const handleOpenAddCategoryDialog = () => {
    setAddCategoryDialogOpen(true)
  }

  const handleCloseAddCategoryDialog = () => {
    setAddCategoryDialogOpen(false)
  }

  const handleAddCategory = async (name: string, color: string, icon: string) => {
    await addCategory(name, color, icon)
    setAddCategoryDialogOpen(false)
  }

  const handleOpenReader = (article: Article) => {
    setSelectedArticle(article)
    setReaderDialogOpen(true)
  }

  const handleCloseReader = () => {
    setReaderDialogOpen(false)
    setSelectedArticle(null)
  }

  const renderContent = () => {
    const loading = feedsLoading

    switch (activeView) {
      case 'dashboard':
        return (
          <DashboardView />
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
            onEdit={handleOpenEditDialog}
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
            onOpenReader={handleOpenReader}
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
            onOpenReader={handleOpenReader}
          />
        )
      case 'categories':
        return (
          <CategoriesView
            categories={categories}
            loading={loading}
            onDelete={deleteCategory}
            onEdit={handleOpenEditCategoryDialog}
          />
        )
      case 'statistics':
        return <StatisticsView />
      case 'settings':
        return (
          <SettingsView
            articlesWithoutContent={articlesWithoutContent}
            onOpenExtractionDialog={() => setExtractionDialogOpen(true)}
          />
        )
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
                NewsWeave
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
            p: { xs: 1, sm: 3 },
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

        {activeView === 'feeds' && (
          <Fab
            color="primary"
            sx={{ position: 'fixed', bottom: 24, right: 24 }}
            onClick={() => setAddDialogOpen(true)}
          >
            <AddIcon />
          </Fab>
        )}

        {activeView === 'categories' && (
          <Fab
            color="primary"
            sx={{ position: 'fixed', bottom: 24, right: 24 }}
            onClick={handleOpenAddCategoryDialog}
          >
            <AddIcon />
          </Fab>
        )}

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

        <EditFeedDialog
          open={editDialogOpen}
          onClose={handleCloseEditDialog}
          onSubmit={handleUpdateFeed}
          feed={feedToEdit}
          loading={feedsLoading}
          categories={categories}
          selectedCategories={editSelectedCategories}
          onToggleCategory={(id) => {
            setEditSelectedCategories(prev =>
              prev.includes(id) ? prev.filter(c => c !== id) : [...prev, id]
            )
          }}
        />

        <EditCategoryDialog
          open={editCategoryDialogOpen}
          onClose={handleCloseEditCategoryDialog}
          onSubmit={handleUpdateCategory}
          category={categoryToEdit}
          loading={feedsLoading}
        />

        <AddCategoryDialog
          open={addCategoryDialogOpen}
          onClose={handleCloseAddCategoryDialog}
          onSubmit={handleAddCategory}
          loading={feedsLoading}
        />

        <ArticleReaderDialog
          article={selectedArticle}
          open={readerDialogOpen}
          onClose={handleCloseReader}
        />

        <ContentExtractionDialog
          open={extractionDialogOpen}
          onClose={() => {
            setExtractionDialogOpen(false)
            loadArticlesWithoutContentCount()
          }}
          articlesWithoutContent={articlesWithoutContent}
          onExtractionComplete={loadArticlesWithoutContentCount}
        />
      </Box>
    </ThemeProvider>
  )
}

export default App
