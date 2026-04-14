import { useState, useEffect } from 'react'
import {
  ThemeProvider,
  createTheme,
  CssBaseline,
  Box,
  AppBar,
  Toolbar,
  Typography,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Card,
  CardContent,
  CardMedia,
  CardActions,
  Button,
  Chip,
  Grid,
  Container,
  Skeleton,
  Alert,
  CircularProgress,
  TextField,
  InputAdornment,
  Fab,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Paper,
  Avatar,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material'
import {
  Menu as MenuIcon,
  RssFeed as FeedIcon,
  Article as ArticleIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
  Refresh as RefreshIcon,
  Add as AddIcon,
  Search as SearchIcon,
  Launch as LaunchIcon,
  Delete as DeleteIcon,
  Newspaper as NewspaperIcon,
  Computer as ComputerIcon,
  TrendingUp as TrendingUpIcon,
  CalendarToday as CalendarTodayIcon,
  Bookmark as BookmarkIcon,
  BookmarkBorder as BookmarkBorderIcon,
  Label as LabelIcon,
  Visibility as VisibilityIcon,
  VisibilityOff as VisibilityOffIcon,
  CheckCircle as CheckCircleIcon,
} from '@mui/icons-material'
import { feedsApi, articlesApi, categoriesApi, feedCategoriesApi, type Feed, type Article, type Category } from './api/client'

const drawerWidth = 280
const PLACEHOLDER_IMAGE = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDgwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iNDgwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iIzMzNzVkYiIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE4IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5ld3M8L3RleHQ+PC9zdmc+'

function App() {
  const [feeds, setFeeds] = useState<Feed[]>([])
  const [articles, setArticles] = useState<Article[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [mobileOpen, setMobileOpen] = useState(false)
  const [isDark, setIsDark] = useState(true)
  const [searchQuery, setSearchQuery] = useState('')
  const [addDialogOpen, setAddDialogOpen] = useState(false)
  const [newFeedUrl, setNewFeedUrl] = useState('')
  const [newFeedName, setNewFeedName] = useState('')
  const [selectedCategories, setSelectedCategories] = useState<string[]>([])
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [feedToDelete, setFeedToDelete] = useState<Feed | null>(null)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [addError, setAddError] = useState<string | null>(null)
  const [adding, setAdding] = useState(false)
  const [refreshingFeedId, setRefreshingFeedId] = useState<string | null>(null)
  const [activeView, setActiveView] = useState<'dashboard' | 'feeds' | 'articles' | 'favorites' | 'categories'>('dashboard')

  // Zusätzliche States für Artikel-Status
  const [articleStatuses, setArticleStatuses] = useState<Record<string, { isRead?: boolean; isFavorite?: boolean }>>({})
  const [updatingArticleId, setUpdatingArticleId] = useState<string | null>(null)
  const [dashboardFilter, setDashboardFilter] = useState<'all' | 'unread' | 'favorites'>('all')
  const [articlesFilter, setArticlesFilter] = useState<'all' | 'unread' | 'favorites'>('all')
  const [dashboardCategoryFilter, setDashboardCategoryFilter] = useState<string[]>([])
  const [articlesCategoryFilter, setArticlesCategoryFilter] = useState<string[]>([])

  // Kategorien-States
  const [categories, setCategories] = useState<Category[]>([])
  const [categoryDialogOpen, setCategoryDialogOpen] = useState(false)
  const [newCategoryName, setNewCategoryName] = useState('')
  const [newCategoryColor, setNewCategoryColor] = useState('#667eea')
  const [addingCategory, setAddingCategory] = useState(false)
  
  // Feed-Kategorie-Zuweisung
  const [assignCategoryDialogOpen, setAssignCategoryDialogOpen] = useState(false)
  const [feedForCategories, setFeedForCategories] = useState<Feed | null>(null)
  const [selectedFeedCategories, setSelectedFeedCategories] = useState<string[]>([])
  const [assigningCategories, setAssigningCategories] = useState(false)

  const theme = createTheme({
    palette: {
      mode: isDark ? 'dark' : 'light',
      primary: {
        main: '#667eea',
      },
      secondary: {
        main: '#764ba2',
      },
      background: {
        default: isDark ? '#0f172a' : '#f8fafc',
        paper: isDark ? '#1e293b' : '#ffffff',
      },
    },
    typography: {
      fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    },
  })

  const loadData = async () => {
    try {
      setLoading(true)
      setError(null)
      const [feedsData, articlesData, readStatuses, favoriteStatuses, categoriesData] = await Promise.all([
        feedsApi.getAll(),
        articlesApi.getAll(),
        articlesApi.getReadArticles().catch(() => []),
        articlesApi.getFavoriteArticles().catch(() => []),
        categoriesApi.getAll().catch(() => []),
      ])
      setFeeds(feedsData)
      setArticles(articlesData)
      setCategories(categoriesData)
      
      // Status-Map aus API-Antworten aufbauen
      const statusMap: Record<string, { isRead?: boolean; isFavorite?: boolean }> = {}
      readStatuses.forEach((status) => {
        statusMap[status.articleId] = { ...statusMap[status.articleId], isRead: true }
      })
      favoriteStatuses.forEach((status) => {
        statusMap[status.articleId] = { ...statusMap[status.articleId], isFavorite: true }
      })
      setArticleStatuses(statusMap)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const filteredArticles = searchQuery
    ? articles.filter(
        (a) =>
          a.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          a.description?.toLowerCase().includes(searchQuery.toLowerCase())
      )
    : articles

  const handleAddFeed = async () => {
    if (!newFeedUrl.trim()) {
      setAddError('Bitte gib eine Feed-URL ein')
      return
    }
    
    try {
      setAdding(true)
      setAddError(null)
      
      const newFeed = await feedsApi.add({ 
        name: newFeedName.trim() || undefined as any, 
        url: newFeedUrl.trim() 
      })
      
      // Kategorien zuweisen wenn ausgewählt
      if (selectedCategories.length > 0) {
        await feedCategoriesApi.assign(newFeed.id, selectedCategories)
      }
      
      setAddDialogOpen(false)
      setNewFeedUrl('')
      setNewFeedName('')
      setSelectedCategories([])
      loadData()
    } catch (err) {
      setAddError(err instanceof Error ? err.message : 'Fehler beim Hinzufügen des Feeds')
    } finally {
      setAdding(false)
    }
  }

  const handleDeleteClick = (feed: Feed) => {
    setFeedToDelete(feed)
    setDeleteDialogOpen(true)
    setDeleteError(null)
  }

  const handleConfirmDelete = async () => {
    if (!feedToDelete) return
    
    try {
      setDeleting(true)
      setDeleteError(null)
      
      await feedsApi.delete(feedToDelete.id)
      
      setDeleteDialogOpen(false)
      setFeedToDelete(null)
      loadData()
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : 'Fehler beim Löschen des Feeds')
    } finally {
      setDeleting(false)
    }
  }

  const handleCloseDeleteDialog = () => {
    setDeleteDialogOpen(false)
    setFeedToDelete(null)
    setDeleteError(null)
  }

  const handleCloseAddDialog = () => {
    setAddDialogOpen(false)
    setAddError(null)
    setNewFeedUrl('')
    setNewFeedName('')
    setSelectedCategories([])
  }

  // Handler für Feed-Kategorie-Zuweisung
  const handleOpenAssignCategories = (feed: Feed) => {
    setFeedForCategories(feed)
    setSelectedFeedCategories(feed.categoryIds || [])
    setAssignCategoryDialogOpen(true)
  }

  const handleCloseAssignCategories = () => {
    setAssignCategoryDialogOpen(false)
    setFeedForCategories(null)
    setSelectedFeedCategories([])
    setAssigningCategories(false)
  }

  const handleAssignCategories = async () => {
    if (!feedForCategories) return
    try {
      setAssigningCategories(true)
      await feedCategoriesApi.assign(feedForCategories.id, selectedFeedCategories)
      handleCloseAssignCategories()
      loadData()
    } catch (err) {
      console.error('Error assigning categories:', err)
      alert('Fehler beim Zuweisen der Kategorien')
    } finally {
      setAssigningCategories(false)
    }
  }

  const handleToggleFeedCategory = (categoryId: string) => {
    setSelectedFeedCategories(prev =>
      prev.includes(categoryId)
        ? prev.filter(id => id !== categoryId)
        : [...prev, categoryId]
    )
  }

  const handleRefreshFeed = async (feed: Feed) => {
    try {
      setRefreshingFeedId(feed.id)
      
      console.log('Refreshing feed:', feed.id, feed.name)
      await feedsApi.fetchArticles(feed.id)
      
      console.log('Refresh successful, reloading data...')
      await loadData()
    } catch (err) {
      console.error('Refresh error:', err)
      alert('Fehler beim Aktualisieren: ' + (err instanceof Error ? err.message : 'Unbekannter Fehler'))
    } finally {
      setRefreshingFeedId(null)
    }
  }

  // Handler-Funktionen für Kategorien
  const handleAddCategory = async () => {
    if (!newCategoryName.trim()) {
      return
    }
    try {
      setAddingCategory(true)
      await categoriesApi.create({ 
        name: newCategoryName.trim(), 
        color: newCategoryColor 
      })
      setCategoryDialogOpen(false)
      setNewCategoryName('')
      setNewCategoryColor('#667eea')
      loadData()
    } catch (err) {
      console.error('Error adding category:', err)
    } finally {
      setAddingCategory(false)
    }
  }

  const handleDeleteCategory = async (id: string) => {
    if (!confirm('Kategorie wirklich löschen?')) return
    try {
      await categoriesApi.delete(id)
      loadData()
    } catch (err) {
      console.error('Error deleting category:', err)
    }
  }

  // Handler-Funktionen für Lesen/Gelesen und Favoriten
  const handleToggleRead = async (articleId: string) => {
    setUpdatingArticleId(articleId)
    try {
      const isCurrentlyRead = articleStatuses[articleId]?.isRead
      if (isCurrentlyRead) {
        await articlesApi.markAsUnread(articleId)
      } else {
        await articlesApi.markAsRead(articleId)
      }
      setArticleStatuses(prev => ({
        ...prev,
        [articleId]: {
          ...prev[articleId],
          isRead: !prev[articleId]?.isRead
        }
      }))
    } catch (err) {
      console.error('Error toggling read status:', err)
    } finally {
      setUpdatingArticleId(null)
    }
  }

  const handleToggleFavorite = async (articleId: string) => {
    setUpdatingArticleId(articleId)
    try {
      await articlesApi.toggleFavorite(articleId)
      setArticleStatuses(prev => ({
        ...prev,
        [articleId]: {
          ...prev[articleId],
          isFavorite: !prev[articleId]?.isFavorite
        }
      }))
    } catch (err) {
      console.error('Error toggling favorite:', err)
    } finally {
      setUpdatingArticleId(null)
    }
  }

  // Hilfsfunktion zum Filtern von Artikeln
  const getFilteredArticles = (filter: 'all' | 'unread' | 'favorites', articleList: Article[], categoryFilter?: string[]) => {
    let result = articleList
    
    // Kategorie-Filter anwenden (Mehrfachauswahl - OR Logik)
    if (categoryFilter && categoryFilter.length > 0) {
      result = result.filter(a => 
        a.categoryIds?.some(catId => categoryFilter.includes(catId))
      )
    }
    
    // Lesestatus-Filter anwenden
    switch (filter) {
      case 'unread':
        return result.filter(a => !articleStatuses[a.id]?.isRead)
      case 'favorites':
        return result.filter(a => articleStatuses[a.id]?.isFavorite)
      default:
        return result
    }
  }

  // Hilfsfunktion zum Rendern einer Artikelkarte
  const renderArticleCard = (article: Article) => {
    const isRead = articleStatuses[article.id]?.isRead
    const isFavorite = articleStatuses[article.id]?.isFavorite

    return (
      <Card
        key={article.id}
        sx={{
          width: 480,
          height: 420,
          display: 'flex',
          flexDirection: 'column',
          transition: 'transform 0.2s, box-shadow 0.2s',
          opacity: isRead ? 0.85 : 1,
          '&:hover': {
            transform: 'translateY(-4px)',
            boxShadow: 6,
          },
        }}
      >
        <CardMedia
          component="img"
          height="200"
          image={article.imageUrl || PLACEHOLDER_IMAGE}
          alt={article.title}
          sx={{ objectFit: 'cover' }}
        />
        <CardContent sx={{ flexGrow: 1, overflow: 'hidden' }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
            <Chip size="small" label={article.feedName || 'News'} color="primary" />
            <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
              <CalendarTodayIcon fontSize="inherit" />
              {article.publishedAt ? new Date(article.publishedAt).toLocaleDateString('de-DE', {
                day: 'numeric', month: 'short', year: 'numeric',
              }) : 'Kein Datum'}
            </Typography>
          </Box>
          <Typography variant="h6" sx={{ fontSize: '1rem', fontWeight: 600, mb: 1, lineHeight: 1.3 }}>
            {isRead && (
              <CheckCircleIcon 
                fontSize="small" 
                color="success" 
                sx={{ mr: 0.5, verticalAlign: 'middle' }} 
              />
            )}
            {article.title}
          </Typography>
          <Typography 
            variant="body2" 
            color="text.secondary" 
            sx={{ 
              mb: 1, 
              maxHeight: 60, 
              overflow: 'hidden', 
              textOverflow: 'ellipsis', 
              display: '-webkit-box', 
              WebkitLineClamp: 3, 
              WebkitBoxOrient: 'vertical',
              lineHeight: 1.4
            }}
            title={article.description || ''}
          >
            {article.description || 'Keine Beschreibung verfügbar'}
          </Typography>
        </CardContent>
        <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
          <Button 
            size="small" 
            startIcon={<LaunchIcon />} 
            onClick={() => window.open(article.link, '_blank')}
          >
            Lesen
          </Button>
          <Box>
            <IconButton
              size="small"
              onClick={() => handleToggleRead(article.id)}
              disabled={updatingArticleId === article.id}
              sx={{ mr: 0.5 }}
              title={isRead ? 'Als ungelesen markieren' : 'Als gelesen markieren'}
            >
              {updatingArticleId === article.id ? (
                <CircularProgress size={20} />
              ) : isRead ? (
                <VisibilityOffIcon color="action" />
              ) : (
                <VisibilityIcon color="primary" />
              )}
            </IconButton>
            <IconButton
              size="small"
              onClick={() => handleToggleFavorite(article.id)}
              disabled={updatingArticleId === article.id}
              title={isFavorite ? 'Aus Favoriten entfernen' : 'Zu Favoriten hinzufügen'}
            >
              {updatingArticleId === article.id ? (
                <CircularProgress size={20} />
              ) : isFavorite ? (
                <BookmarkIcon color="secondary" />
              ) : (
                <BookmarkBorderIcon color="action" />
              )}
            </IconButton>
          </Box>
        </CardActions>
      </Card>
    )
  }

  const drawer = (
    <Box sx={{ mt: 2 }}>
      <Box sx={{ px: 2, mb: 3 }}>
        <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
          News Aggregator
        </Typography>
        <Typography variant="caption" color="text.secondary">
          Deine personalisierte News
        </Typography>
      </Box>

      <List>
        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'dashboard'} onClick={() => setActiveView('dashboard')}>
            <ListItemIcon>
              <NewspaperIcon color="primary" />
            </ListItemIcon>
            <ListItemText primary="Dashboard" />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'feeds'} onClick={() => setActiveView('feeds')}>
            <ListItemIcon>
              <FeedIcon />
            </ListItemIcon>
            <ListItemText primary="Feeds" />
            <Chip size="small" label={feeds.length} />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'articles'} onClick={() => setActiveView('articles')}>
            <ListItemIcon>
              <ArticleIcon />
            </ListItemIcon>
            <ListItemText primary="Artikel" />
            <Chip size="small" label={articles.length} />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'favorites'} onClick={() => setActiveView('favorites')}>
            <ListItemIcon>
              <BookmarkIcon />
            </ListItemIcon>
            <ListItemText primary="Favoriten" />
            <Chip 
              size="small" 
              label={Object.values(articleStatuses).filter(s => s.isFavorite).length} 
              color="secondary"
            />
          </ListItemButton>
        </ListItem>
        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'categories'} onClick={() => setActiveView('categories')}>
            <ListItemIcon>
              <LabelIcon />
            </ListItemIcon>
            <ListItemText primary="Kategorien" />
            <Chip size="small" label={categories.length} />
          </ListItemButton>
        </ListItem>
      </List>

      <Box sx={{ mt: 4, px: 2, maxHeight: 'calc(100vh - 400px)', overflow: 'auto' }}>
        <Typography variant="subtitle2" color="text.secondary" gutterBottom>
          Abonnierte Feeds
        </Typography>
        <List dense>
          {feeds.sort((a, b) => a.name.localeCompare(b.name)).map((feed) => (
            <ListItem 
              key={feed.id} 
              disablePadding
              secondaryAction={
                <Box sx={{ display: 'flex', gap: 0.5 }}>
                  <IconButton 
                    edge="end" 
                    size="small"
                    onClick={() => handleOpenAssignCategories(feed)}
                    sx={{ opacity: 0.6, '&:hover': { opacity: 1 } }}
                    title="Kategorien zuweisen"
                  >
                    <LabelIcon fontSize="small" color="secondary" />
                  </IconButton>
                  <IconButton 
                    edge="end" 
                    size="small"
                    onClick={() => handleRefreshFeed(feed)}
                    disabled={refreshingFeedId === feed.id}
                    sx={{ opacity: 0.6, '&:hover': { opacity: 1 } }}
                  >
                    {refreshingFeedId === feed.id ? (
                      <CircularProgress size={16} />
                    ) : (
                      <RefreshIcon fontSize="small" color="primary" />
                    )}
                  </IconButton>
                  <IconButton 
                    edge="end" 
                    size="small"
                    onClick={() => handleDeleteClick(feed)}
                    sx={{ opacity: 0.6, '&:hover': { opacity: 1 } }}
                  >
                    <DeleteIcon fontSize="small" color="error" />
                  </IconButton>
                </Box>
              }
            >
              <ListItemButton>
                <Avatar sx={{ width: 24, height: 24, mr: 1, bgcolor: 'primary.main' }}>
                  {feed.name.charAt(0)}
                </Avatar>
                <ListItemText
                  primary={feed.name}
                  secondary={`${feed.articleCount} Artikel`}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Box>
    </Box>
  )

  // Render Dashboard View
  const renderDashboard = () => {
    const dashboardArticles = getFilteredArticles(dashboardFilter, filteredArticles, dashboardCategoryFilter)
      .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
      .slice(0, 10)

    return (
      <Box>
        {/* Stats Cards */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper elevation={2} sx={{ p: 2, background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <FeedIcon sx={{ fontSize: 40, mr: 2, opacity: 0.8 }} />
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 700 }}>
                    {loading ? <Skeleton width={60} /> : feeds.length}
                  </Typography>
                  <Typography variant="body2">Feeds</Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper elevation={2} sx={{ p: 2, background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', color: 'white' }}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <ArticleIcon sx={{ fontSize: 40, mr: 2, opacity: 0.8 }} />
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 700 }}>
                    {loading ? <Skeleton width={60} /> : articles.length}
                  </Typography>
                  <Typography variant="body2">Artikel</Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper elevation={2} sx={{ p: 2, background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', color: 'white' }}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <ComputerIcon sx={{ fontSize: 40, mr: 2, opacity: 0.8 }} />
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 700 }}>
                    {loading ? <Skeleton width={60} /> : articles.filter(a => a.feedName?.toLowerCase().includes('tech') || a.feedName?.toLowerCase().includes('heise') || a.feedName?.toLowerCase().includes('golem')).length}
                  </Typography>
                  <Typography variant="body2">Tech News</Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Paper elevation={2} sx={{ p: 2, background: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)', color: 'white' }}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <TrendingUpIcon sx={{ fontSize: 40, mr: 2, opacity: 0.8 }} />
                <Box>
                  <Typography variant="h4" sx={{ fontWeight: 700 }}>
                    {loading ? <Skeleton width={60} /> : articles.filter(a => a.feedName?.toLowerCase().includes('crypto') || a.feedName?.toLowerCase().includes('coin')).length}
                  </Typography>
                  <Typography variant="body2">Crypto</Typography>
                </Box>
              </Box>
            </Paper>
          </Grid>
        </Grid>

        {/* Error Alert */}
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {/* Articles Grid */}
        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
            <Typography variant="h5" sx={{ fontWeight: 600 }}>
              Neueste Artikel
            </Typography>
            
            {/* Filterleiste - rechtsbündig */}
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1 }}>
              {/* Lesestatus-Filter als Chips */}
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-end' }}>
                <Chip
                  label="Alle"
                  onClick={() => setDashboardFilter('all')}
                  color={dashboardFilter === 'all' ? 'primary' : 'default'}
                  variant={dashboardFilter === 'all' ? 'filled' : 'outlined'}
                  size="small"
                />
                <Chip
                  label="Ungelesen"
                  onClick={() => setDashboardFilter('unread')}
                  color={dashboardFilter === 'unread' ? 'primary' : 'default'}
                  variant={dashboardFilter === 'unread' ? 'filled' : 'outlined'}
                  size="small"
                />
                <Chip
                  label="Favoriten"
                  onClick={() => setDashboardFilter('favorites')}
                  color={dashboardFilter === 'favorites' ? 'primary' : 'default'}
                  variant={dashboardFilter === 'favorites' ? 'filled' : 'outlined'}
                  size="small"
                />
              </Box>
              
              {/* Kategorie-Filter */}
              {categories.length > 0 && (
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-end' }}>
                  <Chip
                    label="Alle"
                    onClick={() => setDashboardCategoryFilter([])}
                    color={dashboardCategoryFilter.length === 0 ? 'primary' : 'default'}
                    variant={dashboardCategoryFilter.length === 0 ? 'filled' : 'outlined'}
                    size="small"
                  />
                  {categories.map((category) => (
                    <Chip
                      key={category.id}
                      label={category.name}
                      onClick={() => {
                        setDashboardCategoryFilter(prev => 
                          prev.includes(category.id)
                            ? prev.filter(id => id !== category.id)
                            : [...prev, category.id]
                        )
                      }}
                      sx={{
                        backgroundColor: dashboardCategoryFilter.includes(category.id) ? category.color : 'transparent',
                        color: dashboardCategoryFilter.includes(category.id) ? '#fff' : 'inherit',
                        borderColor: category.color,
                        '&:hover': {
                          backgroundColor: dashboardCategoryFilter.includes(category.id) ? category.color : 'rgba(0,0,0,0.04)',
                        }
                      }}
                      variant={dashboardCategoryFilter.includes(category.id) ? 'filled' : 'outlined'}
                      size="small"
                    />
                  ))}
                </Box>
              )}
            </Box>
          </Box>

        {loading ? (
          <Grid container spacing={3}>
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <Grid size={{ xs: 12, sm: 6 }} key={i}>
                <Card sx={{ height: 420 }}>
                  <Skeleton variant="rectangular" height={200} />
                  <CardContent>
                    <Skeleton variant="text" height={32} />
                    <Skeleton variant="text" height={60} />
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        ) : dashboardArticles.length === 0 ? (
          <Alert severity="info">
            Keine Artikel gefunden für den aktuellen Filter.
          </Alert>
        ) : (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
            {[...dashboardArticles]
              .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
              .map((article) => renderArticleCard(article))}
          </Box>
        )}
      </Box>
    </Box>
    )
  }

  // Render Feeds View
  const renderFeeds = () => (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 600 }}>
        Alle Feeds
      </Typography>
      
      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid size={{ xs: 12, sm: 6 }} key={i}>
              <Card sx={{ height: 150 }}>
                <Skeleton variant="rectangular" height={150} />
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Grid container spacing={3}>
          {feeds.sort((a, b) => a.name.localeCompare(b.name)).map((feed) => (
            <Grid size={{ xs: 12, sm: 6 }} key={feed.id}>
              <Card sx={{ display: 'flex', alignItems: 'center', p: 2 }}>
                <Avatar sx={{ width: 56, height: 56, mr: 2, bgcolor: 'primary.main' }}>
                  {feed.name.charAt(0)}
                </Avatar>
                <Box sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    {feed.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {feed.articleCount} Artikel
                  </Typography>
                </Box>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <IconButton
                    onClick={() => handleRefreshFeed(feed)}
                    disabled={refreshingFeedId === feed.id}
                    color="primary"
                  >
                    {refreshingFeedId === feed.id ? (
                      <CircularProgress size={20} />
                    ) : (
                      <RefreshIcon />
                    )}
                  </IconButton>
                  <IconButton
                    onClick={() => handleDeleteClick(feed)}
                    color="error"
                  >
                    <DeleteIcon />
                  </IconButton>
                </Box>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  )

  // Render Articles View
  const renderArticles = () => {
    const articlesList = getFilteredArticles(articlesFilter, filteredArticles, articlesCategoryFilter)

    return (
      <Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 3 }}>
          <Typography variant="h4" sx={{ fontWeight: 600 }}>
            Alle Artikel ({articlesList.length})
          </Typography>
          
          {/* Filterleiste - rechtsbündig */}
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 1 }}>
            {/* Lesestatus-Filter als Chips */}
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-end' }}>
              <Chip
                label="Alle"
                onClick={() => setArticlesFilter('all')}
                color={articlesFilter === 'all' ? 'primary' : 'default'}
                variant={articlesFilter === 'all' ? 'filled' : 'outlined'}
                size="small"
              />
              <Chip
                label="Ungelesen"
                onClick={() => setArticlesFilter('unread')}
                color={articlesFilter === 'unread' ? 'primary' : 'default'}
                variant={articlesFilter === 'unread' ? 'filled' : 'outlined'}
                size="small"
              />
              <Chip
                label="Favoriten"
                onClick={() => setArticlesFilter('favorites')}
                color={articlesFilter === 'favorites' ? 'primary' : 'default'}
                variant={articlesFilter === 'favorites' ? 'filled' : 'outlined'}
                size="small"
              />
            </Box>
            
            {/* Kategorie-Filter */}
            {categories.length > 0 && (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, justifyContent: 'flex-end' }}>
                <Chip
                  label="Alle"
                  onClick={() => setArticlesCategoryFilter([])}
                  color={articlesCategoryFilter.length === 0 ? 'primary' : 'default'}
                  variant={articlesCategoryFilter.length === 0 ? 'filled' : 'outlined'}
                  size="small"
                />
                {categories.map((category) => (
                  <Chip
                    key={category.id}
                    label={category.name}
                    onClick={() => {
                      setArticlesCategoryFilter(prev => 
                        prev.includes(category.id)
                          ? prev.filter(id => id !== category.id)
                          : [...prev, category.id]
                      )
                    }}
                    sx={{
                      backgroundColor: articlesCategoryFilter.includes(category.id) ? category.color : 'transparent',
                      color: articlesCategoryFilter.includes(category.id) ? '#fff' : 'inherit',
                      borderColor: category.color,
                      '&:hover': {
                        backgroundColor: articlesCategoryFilter.includes(category.id) ? category.color : 'rgba(0,0,0,0.04)',
                      }
                    }}
                    variant={articlesCategoryFilter.includes(category.id) ? 'filled' : 'outlined'}
                    size="small"
                  />
                ))}
              </Box>
            )}
          </Box>
        </Box>

        {loading ? (
          <Grid container spacing={3}>
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <Grid size={{ xs: 12, sm: 6 }} key={i}>
                <Card sx={{ height: 420 }}>
                  <Skeleton variant="rectangular" height={200} />
                  <CardContent>
                    <Skeleton variant="text" height={32} />
                    <Skeleton variant="text" height={60} />
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        ) : articlesList.length === 0 ? (
          <Alert severity="info">
            Keine Artikel gefunden für den aktuellen Filter.
          </Alert>
        ) : (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
            {[...articlesList]
              .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
              .map((article) => renderArticleCard(article))}
          </Box>
        )}
      </Box>
    )
  }

  // Render Favorites View
  const renderFavorites = () => {
    const favoriteArticles = filteredArticles.filter(a => articleStatuses[a.id]?.isFavorite)

    return (
      <Box>
        <Typography variant="h4" sx={{ mb: 3, fontWeight: 600 }}>
          Favoriten ({favoriteArticles.length})
        </Typography>

        {loading ? (
          <Grid container spacing={3}>
            {[1, 2, 3, 4].map((i) => (
              <Grid size={{ xs: 12, sm: 6 }} key={i}>
                <Card sx={{ height: 420 }}>
                  <Skeleton variant="rectangular" height={200} />
                  <CardContent>
                    <Skeleton variant="text" height={32} />
                    <Skeleton variant="text" height={60} />
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        ) : favoriteArticles.length === 0 ? (
          <Alert severity="info">
            Noch keine Favoriten. Klicke auf das Bookmark-Symbol bei einem Artikel, um ihn zu den Favoriten hinzuzufügen.
          </Alert>
        ) : (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
            {[...favoriteArticles]
              .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
              .map((article) => renderArticleCard(article))}
          </Box>
        )}
      </Box>
    )
  }

  // Render Categories View
  const renderCategories = () => (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 600 }}>
        Kategorien ({categories.length})
      </Typography>

      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid size={{ xs: 12, sm: 6 }} key={i}>
              <Card sx={{ height: 120 }}>
                <Skeleton variant="rectangular" height={120} />
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : categories.length === 0 ? (
        <Alert severity="info">
          Noch keine Kategorien vorhanden.
        </Alert>
      ) : (
        <Grid container spacing={3}>
          {categories.map((category) => (
            <Grid size={{ xs: 12, sm: 6 }} key={category.id}>
              <Card sx={{ 
                display: 'flex', 
                alignItems: 'center', 
                p: 2,
                borderLeft: `4px solid ${category.color}`
              }}>
                <Avatar sx={{ 
                  width: 48, 
                  height: 48, 
                  mr: 2, 
                  bgcolor: category.color 
                }}>
                  {category.name.charAt(0)}
                </Avatar>
                <Box sx={{ flexGrow: 1 }}>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    {category.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {category.icon}
                  </Typography>
                </Box>
                <IconButton
                  onClick={() => handleDeleteCategory(category.id)}
                  color="error"
                >
                  <DeleteIcon />
                </IconButton>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  )

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex' }}>
        {/* App Bar */}
        <AppBar
          position="fixed"
          sx={{
            width: { sm: `calc(100% - ${drawerWidth}px)` },
            ml: { sm: `${drawerWidth}px` },
            background: isDark
              ? 'linear-gradient(135deg, #1e293b 0%, #0f172a 100%)'
              : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          }}
        >
          <Toolbar>
            <IconButton
              color="inherit"
              edge="start"
              onClick={() => setMobileOpen(!mobileOpen)}
              sx={{ mr: 2, display: { sm: 'none' } }}
            >
              <MenuIcon />
            </IconButton>

            <TextField
              size="small"
              placeholder="Artikel durchsuchen..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              sx={{
                flexGrow: 1,
                maxWidth: 400,
                mr: 2,
                '& .MuiOutlinedInput-root': {
                  backgroundColor: 'rgba(255,255,255,0.1)',
                  color: 'white',
                  '& fieldset': { borderColor: 'rgba(255,255,255,0.3)' },
                  '&:hover fieldset': { borderColor: 'rgba(255,255,255,0.5)' },
                },
              }}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: 'rgba(255,255,255,0.7)' }} />
                    </InputAdornment>
                  ),
                },
              }}
            />

            <Box sx={{ flexGrow: 1 }} />

            <IconButton color="inherit" onClick={loadData}>
              <RefreshIcon />
            </IconButton>
            <IconButton color="inherit" onClick={() => setIsDark(!isDark)}>
              {isDark ? <LightModeIcon /> : <DarkModeIcon />}
            </IconButton>
          </Toolbar>
        </AppBar>

        {/* Sidebar */}
        <Box
          component="nav"
          sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
        >
          <Drawer
            variant="temporary"
            open={mobileOpen}
            onClose={() => setMobileOpen(false)}
            ModalProps={{ keepMounted: true }}
            sx={{
              display: { xs: 'block', sm: 'none' },
              '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
            }}
          >
            {drawer}
          </Drawer>
          <Drawer
            variant="permanent"
            sx={{
              display: { xs: 'none', sm: 'block' },
              '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
            }}
            open
          >
            {drawer}
          </Drawer>
        </Box>

        {/* Main Content */}
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            p: 3,
            width: { sm: `calc(100% - ${drawerWidth}px)` },
            mt: 8,
          }}
        >
          <Container maxWidth="xl">
            {activeView === 'dashboard' && renderDashboard()}
            {activeView === 'feeds' && renderFeeds()}
            {activeView === 'articles' && renderArticles()}
            {activeView === 'favorites' && renderFavorites()}
            {activeView === 'categories' && renderCategories()}
          </Container>
        </Box>

        {/* Add FAB - conditional based on active view */}
        {activeView === 'categories' ? (
          <Fab
            color="primary"
            sx={{ position: 'fixed', bottom: 24, right: 24 }}
            onClick={() => setCategoryDialogOpen(true)}
          >
            <AddIcon />
          </Fab>
        ) : (
          <Fab
            color="primary"
            sx={{ position: 'fixed', bottom: 24, right: 24 }}
            onClick={() => setAddDialogOpen(true)}
          >
            <AddIcon />
          </Fab>
        )}

        {/* Add Feed Dialog */}
        <Dialog open={addDialogOpen} onClose={handleCloseAddDialog} maxWidth="sm" fullWidth>
          <DialogTitle>Neuen Feed hinzufügen</DialogTitle>
          <DialogContent>
            {addError && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {addError}
              </Alert>
            )}
            <TextField
              autoFocus
              margin="dense"
              label="Feed URL"
              fullWidth
              variant="outlined"
              placeholder="https://example.com/rss.xml"
              value={newFeedUrl}
              onChange={(e) => setNewFeedUrl(e.target.value)}
              error={!!addError && !newFeedUrl.trim()}
              helperText={!newFeedUrl.trim() ? "Pflichtfeld" : "RSS oder Atom Feed URL"}
            />
            <TextField
              margin="dense"
              label="Name (optional)"
              fullWidth
              variant="outlined"
              placeholder="Mein Feed"
              value={newFeedName}
              onChange={(e) => setNewFeedName(e.target.value)}
              helperText="Wenn leer, wird der Name automatisch ermittelt"
            />
            
            {/* Kategorie-Auswahl */}
            {categories.length > 0 && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Kategorien (optional)
                </Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {categories.map((category) => (
                    <Chip
                      key={category.id}
                      label={category.name}
                      onClick={() => {
                        setSelectedCategories(prev =>
                          prev.includes(category.id)
                            ? prev.filter(id => id !== category.id)
                            : [...prev, category.id]
                        )
                      }}
                      sx={{
                        backgroundColor: selectedCategories.includes(category.id) 
                          ? category.color 
                          : 'transparent',
                        color: selectedCategories.includes(category.id) ? '#fff' : 'inherit',
                        border: `1px solid ${category.color}`,
                        cursor: 'pointer',
                      }}
                    />
                  ))}
                </Box>
              </Box>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseAddDialog} disabled={adding}>
              Abbrechen
            </Button>
            <Button 
              variant="contained" 
              onClick={handleAddFeed}
              disabled={adding || !newFeedUrl.trim()}
              startIcon={adding ? <CircularProgress size={20} /> : undefined}
            >
              {adding ? 'Wird hinzugefügt...' : 'Hinzufügen'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Assign Categories Dialog */}
        <Dialog open={assignCategoryDialogOpen} onClose={handleCloseAssignCategories} maxWidth="sm" fullWidth>
          <DialogTitle>Kategorien zuweisen: {feedForCategories?.name}</DialogTitle>
          <DialogContent>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Wähle die Kategorien für diesen Feed aus:
            </Typography>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {categories.map((category) => (
                <Chip
                  key={category.id}
                  label={category.name}
                  onClick={() => handleToggleFeedCategory(category.id)}
                  sx={{
                    backgroundColor: selectedFeedCategories.includes(category.id) 
                      ? category.color 
                      : 'transparent',
                    color: selectedFeedCategories.includes(category.id) ? '#fff' : 'inherit',
                    border: `1px solid ${category.color}`,
                    cursor: 'pointer',
                  }}
                />
              ))}
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseAssignCategories} disabled={assigningCategories}>
              Abbrechen
            </Button>
            <Button 
              variant="contained" 
              onClick={handleAssignCategories}
              disabled={assigningCategories}
              startIcon={assigningCategories ? <CircularProgress size={20} /> : undefined}
            >
              {assigningCategories ? 'Speichern...' : 'Speichern'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Delete Feed Dialog */}
        <Dialog open={deleteDialogOpen} onClose={handleCloseDeleteDialog} maxWidth="sm" fullWidth>
          <DialogTitle>Feed löschen? </DialogTitle>
          <DialogContent>
            {deleteError && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {deleteError}
              </Alert>
            )}
            <Typography>
              Möchtest du den Feed <strong>"{feedToDelete?.name}"</strong> wirklich löschen?
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Alle {feedToDelete?.articleCount} Artikel dieses Feeds werden ebenfalls gelöscht.
            </Typography>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDeleteDialog} disabled={deleting}>
              Abbrechen
            </Button>
            <Button 
              variant="contained" 
              color="error"
              onClick={handleConfirmDelete}
              disabled={deleting}
              startIcon={deleting ? <CircularProgress size={20} /> : <DeleteIcon />}
            >
              {deleting ? 'Wird gelöscht...' : 'Löschen'}
            </Button>
          </DialogActions>
        </Dialog>

        {/* Add Category Dialog */}
        <Dialog open={categoryDialogOpen} onClose={() => setCategoryDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>Neue Kategorie hinzufügen</DialogTitle>
          <DialogContent>
            <TextField
              autoFocus
              margin="dense"
              label="Name"
              fullWidth
              variant="outlined"
              placeholder="z.B. Technik"
              value={newCategoryName}
              onChange={(e) => setNewCategoryName(e.target.value)}
            />
            <TextField
              margin="dense"
              label="Farbe (Hex)"
              fullWidth
              variant="outlined"
              placeholder="#667eea"
              value={newCategoryColor}
              onChange={(e) => setNewCategoryColor(e.target.value)}
            />
            <Box sx={{ mt: 2, display: 'flex', alignItems: 'center', gap: 1 }}>
              <Typography variant="body2">Vorschau:</Typography>
              <Chip 
                label={newCategoryName || 'Kategorie'} 
                sx={{ 
                  backgroundColor: newCategoryColor,
                  color: '#fff'
                }} 
              />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCategoryDialogOpen(false)} disabled={addingCategory}>
              Abbrechen
            </Button>
            <Button 
              variant="contained" 
              onClick={handleAddCategory}
              disabled={addingCategory || !newCategoryName.trim()}
              startIcon={addingCategory ? <CircularProgress size={20} /> : undefined}
            >
              {addingCategory ? 'Wird hinzugefügt...' : 'Hinzufügen'}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </ThemeProvider>
  )
}

export default App
