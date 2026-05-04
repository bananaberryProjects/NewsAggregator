import { Box, Grid, Skeleton, Typography, Button } from '@mui/material'
import { UploadFile, Download } from '@mui/icons-material'
import { useRef, useState } from 'react'
import type { Feed, Category } from '../../api/client'
import { importOpml, exportOpml, downloadBlob } from '../../api/client'
import { OpmlImportDialog } from '../OpmlImportDialog'
import { FeedCard } from '../FeedCard'

interface FeedsViewProps {
  feeds: Feed[]
  categories?: Category[]
  loading: boolean
  refreshingFeedId: string | null
  onRefresh: (feed: Feed) => void
  onDelete: (feed: Feed) => void
  onEdit: (feed: Feed) => void
  onImportSuccess?: () => void
}

function FeedSkeleton() {
  return (
    <Box
      sx={{
        height: 280,
        overflow: 'hidden',
        borderRadius: 2,
        border: '1px solid',
        borderColor: 'divider',
        bgcolor: 'background.paper',
      }}
    >
      <Skeleton variant="rectangular" height={80} width="100%" />
      <Box sx={{ p: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1.5 }}>
          <Skeleton variant="circular" width={56} height={56} />
          <Box sx={{ flexGrow: 1 }}>
            <Skeleton variant="text" width="60%" />
            <Skeleton variant="text" width="40%" />
          </Box>
        </Box>
        <Skeleton variant="text" width="80%" />
        <Skeleton variant="text" width="50%" />
      </Box>
    </Box>
  )
}

export function FeedsView({ feeds, categories = [], loading, refreshingFeedId, onRefresh, onDelete, onEdit, onImportSuccess }: FeedsViewProps) {
  const [importDialogOpen, setImportDialogOpen] = useState(false)
  const [importing, setImporting] = useState(false)
  const [exporting, setExporting] = useState(false)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleImportClick = () => {
    fileInputRef.current?.click()
  }

  const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (file) {
      setImporting(true)
      try {
        await importOpml(file)
        onImportSuccess?.()
      } catch (error) {
        console.error('Import failed:', error)
      } finally {
        setImporting(false)
      }
    }
    event.target.value = ''
  }

  const handleExport = async () => {
    setExporting(true)
    try {
      const blob = await exportOpml()
      downloadBlob(blob, 'feeds.opml')
    } catch (error) {
      console.error('Export failed:', error)
    } finally {
      setExporting(false)
    }
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Typography variant="h5" sx={{ fontWeight: 600 }}>
          Feeds ({feeds.length})
        </Typography>
        
        <Box sx={{ display: 'flex', gap: 1 }}>
          <input
            type="file"
            accept=".opml,.xml"
            hidden
            ref={fileInputRef}
            onChange={handleFileChange}
          />
          <Button
            variant="outlined"
            startIcon={importing ? <Skeleton variant="circular" width={20} height={20} /> : <UploadFile />}
            onClick={handleImportClick}
            disabled={importing}
          >
            {importing ? 'Importiere...' : 'OPML Import'}
          </Button>
          <Button
            variant="outlined"
            startIcon={exporting ? <Skeleton variant="circular" width={20} height={20} /> : <Download />}
            onClick={handleExport}
            disabled={exporting}
          >
            {exporting ? 'Exportiere...' : 'OPML Export'}
          </Button>
        </Box>
      </Box>
      
      {loading ? (
        <Grid container spacing={3}>
          {[1, 2, 3, 4].map((i) => (
            <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={i}>
              <FeedSkeleton />
            </Grid>
          ))}
        </Grid>
      ) : (
        <Grid container spacing={3}>
          {feeds.sort((a, b) => a.name.localeCompare(b.name)).map((feed) => (
            <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={feed.id}>
              <FeedCard
                feed={feed}
                categories={categories}
                isRefreshing={refreshingFeedId === feed.id}
                onRefresh={() => onRefresh(feed)}
                onEdit={() => onEdit(feed)}
                onDelete={() => onDelete(feed)}
              />
            </Grid>
          ))}
        </Grid>
      )}

      <OpmlImportDialog 
        open={importDialogOpen} 
        onClose={() => setImportDialogOpen(false)}
        onImport={() => {
          setImportDialogOpen(false)
          onImportSuccess?.()
        }}
      />
    </Box>
  )
}
