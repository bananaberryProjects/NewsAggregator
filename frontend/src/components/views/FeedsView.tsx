import { Avatar, Box, Card, Grid, IconButton, Skeleton, Typography } from '@mui/material'
import { Refresh as RefreshIcon, Delete as DeleteIcon } from '@mui/icons-material'
import type { Feed } from '../../api/client'

interface FeedsViewProps {
  feeds: Feed[]
  loading: boolean
  refreshingFeedId: string | null
  onRefresh: (feed: Feed) => void
  onDelete: (feed: Feed) => void
}

export function FeedsView({ feeds, loading, refreshingFeedId, onRefresh, onDelete }: FeedsViewProps) {
  return (
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
                  <IconButton onClick={() => onRefresh(feed)} disabled={refreshingFeedId === feed.id} color="primary">
                    {refreshingFeedId === feed.id ? (
                      <Skeleton variant="circular" width={20} height={20} />
                    ) : (
                      <RefreshIcon />
                    )}
                  </IconButton>
                  <IconButton onClick={() => onDelete(feed)} color="error">
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
}
