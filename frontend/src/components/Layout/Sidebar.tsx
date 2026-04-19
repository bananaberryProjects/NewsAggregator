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
  Divider,
} from '@mui/material'
import {
  RssFeed as FeedIcon,
  Article as ArticleIcon,
  Newspaper as NewspaperIcon,
  Favorite as FavoriteIcon,
  Assessment as StatisticsIcon,
  Close as CloseIcon,
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
  isDark?: boolean
  onToggleTheme?: () => void
  activeCategoryFilter?: string[]
  onCategoryFilterChange?: (categoryIds: string[]) => void
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
  isDark,
  onToggleTheme,
}: SidebarProps) {
  const handleDrawerClose = () => {
    setMobileOpen(false)
  }

  const drawer = (
    <Box sx={{ mt: { xs: 1, md: 2 } }}>
      {/* Mobile Drawer Header with Close Button */}
      <Box sx={{ display: { xs: 'flex', md: 'none' }, alignItems: 'center', justifyContent: 'space-between', px: 2, py: 1 }}>
        <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
          Menü
        </Typography>
        <IconButton onClick={handleDrawerClose}>
          <CloseIcon />
        </IconButton>
      </Box>
      <Divider sx={{ display: { xs: 'block', md: 'none' }, mb: 2 }} />

      {/* Desktop Header */}
      <Box sx={{ px: 2, mb: 3, display: { xs: 'none', md: 'flex' }, alignItems: 'flex-start', justifyContent: 'space-between' }}>
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
          <ListItemButton selected={activeView === 'dashboard'} onClick={() => { setActiveView('dashboard'); handleDrawerClose(); }}>
            <ListItemIcon>
              <NewspaperIcon color="primary" />
            </ListItemIcon>
            <ListItemText primary="Dashboard" />
          </ListItemButton>
        </ListItem>

        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'feeds'} onClick={() => { setActiveView('feeds'); handleDrawerClose(); }}>
            <ListItemIcon>
              <FeedIcon />
            </ListItemIcon>
            <ListItemText primary="Feeds" />
            <Chip size="small" label={feeds.length} />
          </ListItemButton>
        </ListItem>

        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'articles'} onClick={() => { setActiveView('articles'); handleDrawerClose(); }}>
            <ListItemIcon>
              <ArticleIcon />
            </ListItemIcon>
            <ListItemText primary="Artikel" />
            <Chip size="small" label={articleCount} />
          </ListItemButton>
        </ListItem>

        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'favorites'} onClick={() => { setActiveView('favorites'); handleDrawerClose(); }}>
            <ListItemIcon>
              <FavoriteIcon />
            </ListItemIcon>
            <ListItemText primary="Favoriten" />
            <Chip size="small" label={favoriteCount} color="secondary" />
          </ListItemButton>
        </ListItem>

        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'categories'} onClick={() => { setActiveView('categories'); handleDrawerClose(); }}>
            <ListItemIcon>
              <LabelIcon />
            </ListItemIcon>
            <ListItemText primary="Kategorien" />
            <Chip size="small" label={categories.length} />
          </ListItemButton>
        </ListItem>

        <ListItem disablePadding>
          <ListItemButton selected={activeView === 'statistics'} onClick={() => { setActiveView('statistics'); handleDrawerClose(); }}>
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
      sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }}
    >
      {/* Mobile Drawer - Temporary */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerClose}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
      >
        {drawer}
      </Drawer>

      {/* Desktop Drawer - Permanent */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
        }}
        open
      >
        {drawer}
      </Drawer>
    </Box>
  )
}


