import { Avatar, Box, Button, Card, Grid, IconButton, Skeleton, Typography } from '@mui/material'
import { Refresh as RefreshIcon, Delete as DeleteIcon, Edit as EditIcon, UploadFile, Download } from '@mui/icons-material'
import { useRef, useState } from 'react'
import type { Feed } from '../../api/client'
import { importOpml, exportOpml, downloadBlob } from '../../api/client'
import { OpmlImportDialog } from '../OpmlImportDialog'

interface FeedsViewProps {
  feeds: Feed[]
  loading: boolean
  refreshingFeedId: string | null
  onRefresh: (feed: Feed) => void
  onDelete: (feed: Feed) => void
  onEdit: (feed: Feed) => void
  onImportSuccess?: () => void
}

export function FeedsView({ feeds, loading, refreshingFeedId, onRefresh, onDelete, onEdit, onImportSuccess }: FeedsViewProps) {
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
                  <IconButton onClick={() => onEdit(feed)} color="primary" title="Feed bearbeiten">
                    <EditIcon />
                  </IconButton>
                  <IconButton onClick={() => onRefresh(feed)} disabled={refreshingFeedId === feed.id} color="primary" title="Feed aktualisieren">
                    {refreshingFeedId === feed.id ? (
                      <Skeleton variant="circular" width={20} height={20} />
                    ) : (
                      <RefreshIcon />
                    )}
                  </IconButton>
                  <IconButton onClick={() => onDelete(feed)} color="error" title="Feed löschen">
                    <DeleteIcon />
                  </IconButton>
                </Box>
              </Card>
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
