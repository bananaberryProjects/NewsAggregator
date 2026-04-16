import {
  Box,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Chip,
  Typography,
  Avatar,
  IconButton,
  CircularProgress,
} from '@mui/material'
import {
  RssFeed as FeedIcon,
  Article as ArticleIcon,
  Newspaper as NewspaperIcon,
  Favorite as FavoriteIcon,
  Label as LabelIcon,
  Refresh as RefreshIcon,
  Delete as DeleteIcon,
  Assessment as AssessmentIcon,
} from '@mui/icons-material'
import type { Feed, Category } from '../../api/client'
import { ThemeToggleButton } from '../ThemeToggleButton'

const drawerWidth = 280

interface SidebarProps {
  mobileOpen: boolean
  setMobileOpen: (open: boolean) => void
  activeView: string
  setActiveView: (view: 'dashboard' | 'feeds' | 'articles' | 'favorites' | 'categories' | 'statistics') => void
  feeds: Feed[]
  categories: Category[]
  articleCount: number
  favoriteCount: number
  refreshingFeedId: string | null
  onRefreshFeed: (feed: Feed) => void
  onDeleteFeed: (feed: Feed) => void
  onAssignCategories: (feed: Feed) => void
  isDark?: boolean
  onToggleTheme?: () => void
}

export function Sidebar({
  mobileOpen,
  setMobileOpen,
  activeView,
  setActiveView,
  feeds,
  categories,
  articleCount,
  favoriteCount,
  refreshingFeedId,
  onRefreshFeed,
  onDeleteFeed,
  onAssignCategories,
  isDark,
  onToggleTheme,
}: SidebarProps) {
  const drawer = (
    <Box sx={{ mt: 2 }}>
      <Box sx={{ px: 2, mb: 3, display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <Box>
          <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
            News Aggregator
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Deine personalisierte News
          </Typography>
        </Box>
        {isDark !== undefined && onToggleTheme && (
          <ThemeToggleButton isDark={isDark} onToggle={onToggleTheme} />
        )}
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
            <Chip size="small" label={articleCount} />
          </ListItemButton>
        </ListItem>
        
        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'favorites'} onClick={() => setActiveView('favorites')}>
            <ListItemIcon>
              <FavoriteIcon />
            </ListItemIcon>
            <ListItemText primary="Favoriten" />
            <Chip size="small" label={favoriteCount} color="secondary" />
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
        
        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'statistics'} onClick={() => setActiveView('statistics')}>
            <ListItemIcon>
              <AssessmentIcon />
            </ListItemIcon>
            <ListItemText primary="Statistiken" />
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
                    onClick={() => onAssignCategories(feed)}
                    sx={{ opacity: 0.6, '&:hover': { opacity: 1 } }}
                    title="Kategorien zuweisen"
                  >
                    <LabelIcon fontSize="small" color="secondary" />
                  </IconButton>
                  <IconButton 
                    edge="end" 
                    size="small"
                    onClick={() => onRefreshFeed(feed)}
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
                    onClick={() => onDeleteFeed(feed)}
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

  return (
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
  )
}


