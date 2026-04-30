import { useState, useEffect, useMemo } from 'react'
import { Routes, Route, useNavigate, useLocation, Navigate } from 'react-router-dom'
import { useSearch } from './hooks/useSearch'

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
} from '@mui/material'
import { Add as AddIcon, Menu as MenuIcon } from '@mui/icons-material'

function getInitialCollapsedState(): boolean {
  try {
    const saved = localStorage.getItem('sidebar-collapsed')
    return saved === 'true'
  } catch {
    return false
  }
}

import { useTheme, useFeeds, useArticles, useCategories } from './hooks'
import { Sidebar, PWAInstallPrompt } from './components'
import { DashboardView, FeedsView, ArticlesView, FavoritesView, CategoriesView, StatisticsView, SettingsView } from './components/views'
import { AddFeedDialog, DeleteFeedDialog, EditFeedDialog, EditCategoryDialog, AddCategoryDialog } from './components/dialogs'
import { ArticleReaderDialog } from './components/ArticleReaderDialog'
import { ContentExtractionDialog } from './components/ContentExtractionDialog'
import type { Feed, Category, Article } from './api/client'
import { adminApi } from './api/client'

function App() {
  const { theme, isDark, toggleTheme } = useTheme()
  const { feeds, loading: feedsLoading, loadFeeds, addFeed, deleteFeed, refreshFeed, assignCategories, updateFeed } = useFeeds()
  const { articles, articleStatuses, loadArticles, toggleRead, toggleFavorite, updatingArticleId } = useArticles()
  const { categories, loadCategories, deleteCategory, updateCategory, addCategory } = useCategories()

  // Sidebar collapse state (persisted in localStorage)
  const [collapsed, setCollapsed] = useState(getInitialCollapsedState)
  const handleToggleCollapse = () => {
    const next = !collapsed
    setCollapsed(next)
    try { localStorage.setItem('sidebar-collapsed', String(next)) } catch { /* ignore */ }
  }
  const drawerWidth = 280
  const drawerWidthCollapsed = 64

  // === Search state (lifted from SearchBar into App) ===
  const {
    results: searchResults,
    loading: searchLoading,
    query: searchQuery,
    pageData: searchPageData,
    hasMore: searchHasMore,
    search,
    nextPage: searchNextPage,
    reset: searchReset,
  } = useSearch()

  const isSearchActive = searchResults !== null || searchQuery !== ''
  const searchTotalElements = searchPageData?.totalElements ?? 0

  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()
  const navigate = useNavigate()
  const activeView = useMemo(() => {
    const path = location.pathname
    if (path === '/feeds') return 'feeds'
    if (path === '/articles') return 'articles'
    if (path === '/favorites') return 'favorites'
    if (path === '/categories') return 'categories'
    if (path === '/statistics') return 'statistics'
    if (path === '/settings') return 'settings'
    return 'dashboard'
  }, [location.pathname])

  // Filter states - initial values from localStorage
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [_dashboardFilter, _setDashboardFilter] = useState<'all' | 'unread' | 'favorites'>(() => getInitialFilterState('dashboard-filter'))
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
  const [newFeedBlockedKeywords, setNewFeedBlockedKeywords] = useState<string[]>([])

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    await loadFeeds()
    await loadArticles()
    await loadCategories()
    await loadArticlesWithoutContentCount()
  }

  const handleAddFeed = async () => {
    if (!newFeedUrl.trim() || !newFeedName.trim()) return
    await addFeed(newFeedUrl.trim(), newFeedName.trim(), selectedCategories, newFeedBlockedKeywords)
    setNewFeedUrl('')
    setNewFeedName('')
    setSelectedCategories([])
    setNewFeedBlockedKeywords([])
    setAddDialogOpen(false)
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

  const handleUpdateFeed = async (name: string, url: string, description: string, categoryIds: string[], extractContent: boolean, blockedKeywords: string[]) => {
    if (!feedToEdit) return
    await updateFeed(feedToEdit.id, name, url, description, extractContent, blockedKeywords)
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

  const handleSearchReset = () => {
    searchReset()
  }

  // Build search filters from current article filters
  const searchFilters = useMemo(() => {
    const filters: {
      categoryId?: string
      readFilter?: 'READ' | 'UNREAD'
      favoriteFilter?: 'FAVORITE' | 'NOT_FAVORITE'
    } = {}
    if (articlesCategoryFilter.length === 1) {
      filters.categoryId = articlesCategoryFilter[0]
    }
    if (articlesFilter === 'unread') {
      filters.readFilter = 'UNREAD'
    } else if (articlesFilter === 'favorites') {
      filters.favoriteFilter = 'FAVORITE'
    }
    return filters
  }, [articlesFilter, articlesCategoryFilter])

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex', bgcolor: 'background.default', minHeight: '100vh' }}>
        {/* App Bar (Mobile) */}
        <AppBar
          position="fixed"
          elevation={0}
          sx={{
            display: { md: 'none' },
            bgcolor: 'background.paper',
            borderBottom: '1px solid',
            borderColor: 'divider',
            color: 'text.primary',
          }}
        >
          <Toolbar>
            <IconButton
              color="inherit"
              edge="start"
              onClick={() => setMobileOpen(!mobileOpen)}
              sx={{ mr: 2 }}
            >
              <MenuIcon />
            </IconButton>
            <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1, fontWeight: 700, color: 'primary.main' }}>
              NewsWeave
            </Typography>
          </Toolbar>
        </AppBar>

        <Sidebar
          mobileOpen={mobileOpen}
          setMobileOpen={setMobileOpen}
          activeView={activeView}
          feeds={feeds}
          categories={categories}
          articleCount={articles.length}
          favoriteCount={useMemo(() => articles.filter(a => articleStatuses[a.id]?.isFavorite).length, [articles, articleStatuses])}
          isSearchActive={isSearchActive}
          filters={searchFilters}
          searchQuery={searchQuery}
          onSearchQueryChange={(q) => {
            if (q.trim()) {
              search(q.trim(), searchFilters)
              navigate('/articles')
            } else {
              searchReset()
            }
          }}
          searchLoading={searchLoading}
          onSearch={search}
          onSearchReset={searchReset}
          collapsed={collapsed}
          onToggleCollapse={handleToggleCollapse}
        />

        {/* Main Content */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            p: { xs: 0.5, sm: 2, md: 3 },
            width: {
              md: `calc(100% - ${collapsed ? drawerWidthCollapsed : drawerWidth}px)`,
            },
            mt: { xs: 8, md: 0 },
            minHeight: '100vh',
            bgcolor: 'background.default',
            transition: (theme) => theme.transitions.create('width', {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.enteringScreen,
            }),
          }}
        >
          <Container maxWidth="xl" sx={{ px: { xs: 0, sm: 2, md: 3 } }}>
            <Routes>
              <Route path="/" element={<DashboardView />} />
              <Route path="/feeds" element={
                <FeedsView
                  feeds={feeds}
                  loading={feedsLoading}
                  refreshingFeedId={null}
                  onRefresh={refreshFeed}
                  onDelete={(feed: Feed) => {
                    setFeedToDelete(feed)
                    setDeleteDialogOpen(true)
                  }}
                  onEdit={handleOpenEditDialog}
                  onImportSuccess={loadFeeds}
                />
              } />
              <Route path="/articles" element={
                <ArticlesView
                  articles={articles}
                  categories={categories}
                  searchResults={searchResults}
                  isSearchActive={isSearchActive}
                  searchTotalElements={searchTotalElements}
                  onSearchReset={handleSearchReset}
                  onSearchNextPage={searchNextPage}
                  searchHasMore={searchHasMore}
                  loading={feedsLoading}
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
              } />
              <Route path="/favorites" element={
                <FavoritesView
                  articles={articles}
                  loading={feedsLoading}
                  articleStatuses={articleStatuses}
                  updatingArticleId={updatingArticleId}
                  onToggleRead={toggleRead}
                  onToggleFavorite={toggleFavorite}
                  onOpenReader={handleOpenReader}
                />
              } />
              <Route path="/categories" element={
                <CategoriesView
                  categories={categories}
                  loading={feedsLoading}
                  onEdit={handleOpenEditCategoryDialog}
                  onDelete={deleteCategory}
                />
              } />
              <Route path="/statistics" element={<StatisticsView />} />
              <Route path="/settings" element={
                <SettingsView
                  articlesWithoutContent={articlesWithoutContent}
                  onOpenExtractionDialog={() => setExtractionDialogOpen(true)}
                  isDark={isDark}
                  onToggleTheme={toggleTheme}
                />
              } />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
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
            onClick={() => setAddCategoryDialogOpen(true)}
          >
            <AddIcon />
          </Fab>
        )}

        {/* Dialogs */}
        <AddFeedDialog
          open={addDialogOpen}
          onClose={() => setAddDialogOpen(false)}
          onSubmit={handleAddFeed}
          url={newFeedUrl}
          setUrl={setNewFeedUrl}
          name={newFeedName}
          setName={setNewFeedName}
          blockedKeywords={newFeedBlockedKeywords}
          setBlockedKeywords={setNewFeedBlockedKeywords}
          selectedCategories={selectedCategories}
          toggleCategory={(id) => setSelectedCategories(prev => prev.includes(id) ? prev.filter(c => c !== id) : [...prev, id])}
          categories={categories}
          loading={false}
          error={null}
        />

        <DeleteFeedDialog
          open={deleteDialogOpen}
          onClose={() => setDeleteDialogOpen(false)}
          onConfirm={handleDeleteFeed}
          feed={feedToDelete}
          loading={false}
          error={null}
        />

        <EditFeedDialog
          open={editDialogOpen}
          onClose={handleCloseEditDialog}
          onSubmit={handleUpdateFeed}
          feed={feedToEdit}
          loading={false}
          categories={categories}
          selectedCategories={editSelectedCategories}
          onToggleCategory={(id) => setEditSelectedCategories(prev => prev.includes(id) ? prev.filter(c => c !== id) : [...prev, id])}
        />

        <EditCategoryDialog
          open={editCategoryDialogOpen}
          onClose={handleCloseEditCategoryDialog}
          onSubmit={handleUpdateCategory}
          category={categoryToEdit}
          loading={false}
        />

        <AddCategoryDialog
          open={addCategoryDialogOpen}
          onClose={handleCloseAddCategoryDialog}
          onSubmit={handleAddCategory}
          loading={false}
        />

        <ArticleReaderDialog
          open={readerDialogOpen}
          onClose={handleCloseReader}
          article={selectedArticle}
        />

        <ContentExtractionDialog
          open={extractionDialogOpen}
          onClose={() => setExtractionDialogOpen(false)}
          articlesWithoutContent={articlesWithoutContent}
          onExtractionComplete={() => {
            loadArticlesWithoutContentCount()
            loadArticles()
          }}
        />

        {/* PWA Install Prompt */}
        <PWAInstallPrompt />
      </Box>
    </ThemeProvider>
  )
}

export default App