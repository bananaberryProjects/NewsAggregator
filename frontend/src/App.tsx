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
} from '@mui/icons-material'
import { feedsApi, articlesApi, type Feed, type Article } from './api/client'

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
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [feedToDelete, setFeedToDelete] = useState<Feed | null>(null)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const [addError, setAddError] = useState<string | null>(null)
  const [adding, setAdding] = useState(false)
  const [refreshingFeedId, setRefreshingFeedId] = useState<string | null>(null)
  const [refreshError, setRefreshError] = useState<string | null>(null)
  const [activeView, setActiveView] = useState<'dashboard' | 'feeds' | 'articles'>('dashboard')

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
      const [feedsData, articlesData] = await Promise.all([
        feedsApi.getAll(),
        articlesApi.getAll(),
      ])
      setFeeds(feedsData)
      setArticles(articlesData)
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
      
      await feedsApi.add({ 
        name: newFeedName.trim() || undefined as any, 
        url: newFeedUrl.trim() 
      })
      
      // Erfolg - Dialog schließen und Daten neu laden
      setAddDialogOpen(false)
      setNewFeedUrl('')
      setNewFeedName('')
      loadData() // Feed-Liste aktualisieren
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
      
      // Erfolg - Dialog schließen und Daten neu laden
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
  }

  const handleRefreshFeed = async (feed: Feed) => {
    try {
      setRefreshingFeedId(feed.id)
      setRefreshError(null)
      
      console.log('Refreshing feed:', feed.id, feed.name)
      await feedsApi.fetchArticles(feed.id)
      
      // Erfolg - Daten neu laden
      console.log('Refresh successful, reloading data...')
      await loadData()
    } catch (err) {
      console.error('Refresh error:', err)
      setRefreshError(err instanceof Error ? err.message : 'Fehler beim Aktualisieren des Feeds')
      // Zeige Alert mit Fehler
      alert('Fehler beim Aktualisieren: ' + (err instanceof Error ? err.message : 'Unbekannter Fehler'))
    } finally {
      setRefreshingFeedId(null)
    }
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
  const renderDashboard = () => (
    <Box>
      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
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
        <Grid item xs={12} sm={6} md={3}>
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
        <Grid item xs={12} sm={6} md={3}>
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
        <Grid item xs={12} sm={6} md={3}>
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
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 600 }}>
        Neueste Artikel
      </Typography>

      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Grid item xs={12} sm={6} key={i}>
              <Card sx={{ height: 400 }}>
                <Skeleton variant="rectangular" height={200} />
                <CardContent>
                  <Skeleton variant="text" height={32} />
                  <Skeleton variant="text" height={60} />
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
          {[...filteredArticles]
            .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
            .slice(0, 10)
            .map((article) => (
              <Card
                key={article.id}
                sx={{
                  width: 480,
                  height: 400,
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.2s, box-shadow 0.2s',
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
                  <Chip
                    size="small"
                    label={article.feedName || 'News'}
                    color="primary"
                    sx={{ mb: 1 }}
                  />
                  <Typography variant="h6" sx={{ fontSize: '1rem', fontWeight: 600, mb: 1, lineHeight: 1.3 }}>
                    {article.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1, height: 60, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical' }}>
                    {article.description?.substring(0, 100) || 'Keine Beschreibung verfügbar'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(article.publishedAt).toLocaleDateString('de-DE', {
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric',
                    })}
                  </Typography>
                </CardContent>
                <CardActions>
                  <Button size="small" startIcon={<LaunchIcon />} onClick={() => window.open(article.url, '_blank')}>
                    Lesen
                  </Button>
                </CardActions>
              </Card>
            ))}
        </Box>
      )}
    </Box>
  )

  // Render Feeds View
  const renderFeeds = () => (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 600 }}>
        Alle Feeds
      </Typography>
      
      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid item xs={12} sm={6} key={i}>
              <Card sx={{ height: 150 }}>
                <Skeleton variant="rectangular" height={150} />
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Grid container spacing={3}>
          {feeds.sort((a, b) => a.name.localeCompare(b.name)).map((feed) => (
            <Grid item xs={12} sm={6} key={feed.id}>
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
  const renderArticles = () => (
    <Box>
      <Typography variant="h4" sx={{ mb: 3, fontWeight: 600 }}>
        Alle Artikel ({articles.length})
      </Typography>

      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Grid item xs={12} sm={6} key={i}>
              <Card sx={{ height: 400 }}>
                <Skeleton variant="rectangular" height={200} />
                <CardContent>
                  <Skeleton variant="text" height={32} />
                  <Skeleton variant="text" height={60} />
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, justifyContent: 'center' }}>
          {[...filteredArticles]
            .sort((a, b) => new Date(b.publishedAt).getTime() - new Date(a.publishedAt).getTime())
            .map((article) => (
              <Card
                key={article.id}
                sx={{
                  width: 480,
                  height: 400,
                  display: 'flex',
                  flexDirection: 'column',
                  transition: 'transform 0.2s, box-shadow 0.2s',
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
                  <Chip
                    size="small"
                    label={article.feedName || 'News'}
                    color="primary"
                    sx={{ mb: 1 }}
                  />
                  <Typography variant="h6" sx={{ fontSize: '1rem', fontWeight: 600, mb: 1, lineHeight: 1.3 }}>
                    {article.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1, height: 60, overflow: 'hidden', textOverflow: 'ellipsis', display: '-webkit-box', WebkitLineClamp: 3, WebkitBoxOrient: 'vertical' }}>
                    {article.description?.substring(0, 100) || 'Keine Beschreibung verfügbar'}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {new Date(article.publishedAt).toLocaleDateString('de-DE', {
                      day: 'numeric',
                      month: 'short',
                      year: 'numeric',
                    })}
                  </Typography>
                </CardContent>
                <CardActions>
                  <Button size="small" startIcon={<LaunchIcon />} onClick={() => window.open(article.url, '_blank')}>
                    Lesen
                  </Button>
                </CardActions>
              </Card>
            ))}
        </Box>
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
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: 'rgba(255,255,255,0.7)' }} />
                  </InputAdornment>
                ),
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
          </Container>
        </Box>

        {/* Add Feed FAB */}
        <Fab
          color="primary"
          sx={{ position: 'fixed', bottom: 24, right: 24 }}
          onClick={() => setAddDialogOpen(true)}
        >
          <AddIcon />
        </Fab>

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
      </Box>
    </ThemeProvider>
  )
}

export default App
