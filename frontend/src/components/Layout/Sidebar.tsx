import {
  Box,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Chip,
  type ChipProps,
  Typography,
  Avatar,
  IconButton,
  Divider,
  Tooltip,
  Badge,
} from '@mui/material'
import {
  RssFeed as FeedIcon,
  Article as ArticleIcon,
  Newspaper as NewspaperIcon,
  Favorite as FavoriteIcon,
  Label as LabelIcon,
  Assessment as AssessmentIcon,
  Settings as SettingsIcon,
  Close as CloseIcon,
  ChevronLeft as ChevronLeftIcon,
  ChevronRight as ChevronRightIcon,
} from '@mui/icons-material'
import { Link } from 'react-router-dom'
import type { Feed, Category } from '../../api/client'
import { SearchBar } from '../SearchBar'

const drawerWidth = 280
const drawerWidthCollapsed = 64

interface SidebarProps {
  mobileOpen: boolean
  setMobileOpen: (open: boolean) => void
  activeView: string
  feeds: Feed[]
  categories: Category[]
  articleCount: number
  favoriteCount: number
  isSearchActive?: boolean
  filters?: {
    categoryId?: string
    readFilter?: 'READ' | 'UNREAD'
    favoriteFilter?: 'FAVORITE' | 'NOT_FAVORITE'
  }
  searchQuery: string
  onSearchQueryChange: (q: string) => void
  searchLoading: boolean
  onSearch: (q: string, filters?: { categoryId?: string; readFilter?: 'READ' | 'UNREAD'; favoriteFilter?: 'FAVORITE' | 'NOT_FAVORITE' }) => void
  onSearchReset: () => void
  collapsed: boolean
  onToggleCollapse: () => void
}

export function Sidebar({
  mobileOpen,
  setMobileOpen,
  activeView,
  feeds,
  categories,
  articleCount,
  favoriteCount,
  isSearchActive,
  filters,
  searchQuery,
  onSearchQueryChange,
  searchLoading,
  onSearch,
  onSearchReset,
  collapsed,
  onToggleCollapse,
}: SidebarProps) {
  const handleDrawerClose = () => {
    setMobileOpen(false)
  }

  const currentDrawerWidth = collapsed ? drawerWidthCollapsed : drawerWidth

  const navItems = [
    { key: 'dashboard', to: '/', label: 'Dashboard', icon: <NewspaperIcon />, badge: null as number | null, badgeColor: undefined as ChipProps['color'] },
    { key: 'feeds', to: '/feeds', label: 'Feeds', icon: <FeedIcon color="success" />, badge: feeds.length || null, badgeColor: 'success' as const },
    { key: 'articles', to: '/articles', label: 'Artikel', icon: <ArticleIcon color="warning" />, badge: articleCount || null, badgeColor: 'warning' as const },
    { key: 'favorites', to: '/favorites', label: 'Favoriten', icon: <FavoriteIcon color="error" />, badge: favoriteCount || null, badgeColor: 'error' as const },
    { key: 'categories', to: '/categories', label: 'Kategorien', icon: <LabelIcon color="primary" />, badge: categories.length || null, badgeColor: 'primary' as const },
    { key: 'statistics', to: '/statistics', label: 'Statistiken', icon: <AssessmentIcon sx={{ color: '#9c27b0' }} />, badge: null, badgeColor: undefined },
    { key: 'settings', to: '/settings', label: 'Einstellungen', icon: <SettingsIcon sx={{ color: '#757575' }} />, badge: null, badgeColor: undefined },
  ]

  const drawer = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      {/* Mobile Drawer Header */}
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
      <Box
        sx={{
          px: collapsed ? 1 : 2,
          py: collapsed ? 1 : 0,
          mb: collapsed ? 1 : 3,
          display: { xs: 'none', md: 'flex' },
          alignItems: 'center',
          justifyContent: collapsed ? 'center' : 'space-between',
          minHeight: 48,
        }}
      >
        {!collapsed && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.7 }}>
            <img src="/newsweave.png" width="40" height="40" alt="NewsWeave Logo" />
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main', letterSpacing: '-0.2px', lineHeight: 1.5 }}>
                NewsWeave
              </Typography>
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: -0.5 }}>
                Deine personalisierte News
              </Typography>
            </Box>
          </Box>
        )}
        <Tooltip title={collapsed ? 'Expandieren' : 'Einklappen'} placement="right">
          <IconButton onClick={onToggleCollapse} size="small" sx={{ mx: collapsed ? 'auto' : 0 }}>
            {collapsed ? <ChevronRightIcon /> : <ChevronLeftIcon />}
          </IconButton>
        </Tooltip>
      </Box>

      {/* Search - hidden when collapsed */}
      {!collapsed && (
        <>
          <Box sx={{ px: 2, mb: 1 }}>
            <SearchBar
              loading={searchLoading}
              query={searchQuery}
              onQueryChange={onSearchQueryChange}
              onSearch={onSearch}
              onReset={onSearchReset}
              filters={filters}
              isSearchActive={isSearchActive}
            />
          </Box>
          <Divider sx={{ my: 1, mx: 2 }} />
        </>
      )}

      {/* Navigation */}
      <List sx={{ flexGrow: 0, px: collapsed ? 0.5 : 0 }}>
        {navItems.map((item) => (
          <ListItem key={item.key} disablePadding>
            <Tooltip title={collapsed ? item.label : ''} placement="right" arrow>
              <ListItemButton
                component={Link}
                to={item.to}
                selected={activeView === item.key}
                onClick={handleDrawerClose}
                sx={{
                  justifyContent: collapsed ? 'center' : 'flex-start',
                  px: collapsed ? 1.5 : 2,
                  minHeight: 48,
                }}
              >
                <ListItemIcon
                  sx={{
                    minWidth: collapsed ? 0 : 40,
                    justifyContent: 'center',
                    mr: collapsed ? 0 : 2,
                  }}
                >
                  {collapsed && item.badge !== null && item.badge > 0 ? (
                    <Badge badgeContent={item.badge} color={item.badgeColor || 'default'}>
                      {item.icon}
                    </Badge>
                  ) : (
                    item.icon
                  )}
                </ListItemIcon>
                {!collapsed && (
                  <>
                    <ListItemText primary={item.label} />
                    {item.badge !== null && item.badge > 0 && (
                      <Chip size="small" label={item.badge} color={item.badgeColor} />
                    )}
                  </>
                )}
              </ListItemButton>
            </Tooltip>
          </ListItem>
        ))}
      </List>

      <Divider sx={{ my: 1 }} />

      {/* Feed List - hidden when collapsed */}
      {!collapsed ? (
        <Box sx={{ flexGrow: 1, overflow: 'auto', px: 2, mt: 2 }}>
          <Typography variant="subtitle2" color="text.secondary" gutterBottom>
            Abonnierte Feeds
          </Typography>
          <List dense>
            {feeds.sort((a, b) => a.name.localeCompare(b.name)).map((feed) => (
              <ListItem key={feed.id} disablePadding>
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
      ) : (
        <Box sx={{ flexGrow: 1 }} />
      )}
    </Box>
  )

  return (
    <Box
      component="nav"
      sx={{
        width: { md: currentDrawerWidth },
        flexShrink: { md: 0 },
        transition: (theme) => theme.transitions.create('width', {
          easing: theme.transitions.easing.sharp,
          duration: theme.transitions.duration.leavingScreen,
        }),
      }}
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
          '& .MuiDrawer-paper': {
            boxSizing: 'border-box',
            width: currentDrawerWidth,
            overflowX: 'hidden',
            transition: (theme) => theme.transitions.create('width', {
              easing: theme.transitions.easing.sharp,
              duration: theme.transitions.duration.enteringScreen,
            }),
          },
        }}
        open
      >
        {drawer}
      </Drawer>
    </Box>
  )
}
