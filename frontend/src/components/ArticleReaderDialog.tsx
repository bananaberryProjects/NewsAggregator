import { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Typography,
  Button,
  Box,
  Chip,
  useTheme,
} from '@mui/material';
import {
  Close as CloseIcon,
  Launch as LaunchIcon,
  Share as ShareIcon,
} from '@mui/icons-material';
import DOMPurify from 'dompurify';
import type { Article } from '../api/client';

interface ArticleReaderDialogProps {
  article: Article | null;
  open: boolean;
  onClose: () => void;
}

const ALLOWED_TAGS = [
  'p',
  'h1',
  'h2',
  'h3',
  'h4',
  'h5',
  'h6',
  'img',
  'a',
  'strong',
  'em',
  'ul',
  'ol',
  'li',
  'blockquote',
  'br',
  'span',
  'div',
];

const ALLOWED_ATTR = ['href', 'src', 'alt', 'title', 'class', 'id', 'style'];

export function ArticleReaderDialog({
  article,
  open,
  onClose,
}: ArticleReaderDialogProps) {
  const theme = useTheme();
  const [sanitizedContent, setSanitizedContent] = useState<string>('');

  useEffect(() => {
    if (article?.contentHtml) {
      const sanitized = DOMPurify.sanitize(article.contentHtml, {
        ALLOWED_TAGS,
        ALLOWED_ATTR,
      });
      setSanitizedContent(sanitized);
    } else {
      setSanitizedContent('');
    }
  }, [article?.contentHtml]);

  const handleOpenOriginal = () => {
    if (article?.link) {
      window.open(article.link, '_blank');
    }
  };

  const handleShare = async () => {
    if (article?.link) {
      if (navigator.share) {
        try {
          await navigator.share({
            title: article.title,
            url: article.link,
          });
        } catch {
          // User cancelled or share failed
        }
      } else {
        await navigator.clipboard.writeText(article.link);
      }
    }
  };

  if (!article) return null;

  const formattedDate = article.publishedAt
    ? new Date(article.publishedAt).toLocaleDateString('de-DE', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      })
    : 'Kein Datum verfügbar';

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="md"
      fullWidth
      sx={{
        '& .MuiDialog-paper': {
          maxWidth: 800,
          minHeight: '70vh',
          maxHeight: '90vh',
        },
      }}
    >
      {/* Header */}
      <DialogTitle
        sx={{
          display: 'flex',
          alignItems: 'flex-start',
          justifyContent: 'space-between',
          pb: 1,
          borderBottom: `1px solid ${theme.palette.divider}`,
        }}
      >
        <Box sx={{ flex: 1, pr: 2 }}>
          <Typography
            variant="h5"
            component="h1"
            sx={{
              fontWeight: 600,
              lineHeight: 1.3,
              fontSize: '1.5rem',
            }}
          >
            {article.title}
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 1 }}>
            <Chip
              size="small"
              label={article.feedName || 'News'}
              color="primary"
              variant="outlined"
            />
            <Typography variant="caption" color="text.secondary">
              {formattedDate}
            </Typography>
          </Box>
        </Box>
        <IconButton
          onClick={onClose}
          size="small"
          sx={{
            mt: 0.5,
            '&:hover': {
              backgroundColor: theme.palette.action.hover,
            },
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      {/* Content */}
      <DialogContent
        sx={{
          px: 4,
          py: 3,
          '&::-webkit-scrollbar': {
            width: '8px',
          },
          '&::-webkit-scrollbar-track': {
            background: theme.palette.action.hover,
          },
          '&::-webkit-scrollbar-thumb': {
            background: theme.palette.divider,
            borderRadius: '4px',
          },
        }}
      >
        {sanitizedContent ? (
          <Box
            sx={{
              '& p': {
                fontSize: '1rem',
                lineHeight: 1.6,
                mb: 2,
                color: theme.palette.text.primary,
              },
              '& h1, & h2, & h3, & h4, & h5, & h6': {
                fontWeight: 600,
                mt: 3,
                mb: 1.5,
                lineHeight: 1.3,
                color: theme.palette.text.primary,
              },
              '& h1': { fontSize: '1.5rem' },
              '& h2': { fontSize: '1.375rem' },
              '& h3': { fontSize: '1.25rem' },
              '& h4': { fontSize: '1.125rem' },
              '& h5, & h6': { fontSize: '1rem' },
              '& img': {
                maxWidth: '100%',
                height: 'auto',
                borderRadius: '8px',
                my: 2,
              },
              '& a': {
                color: theme.palette.primary.main,
                textDecoration: 'none',
                '&:hover': {
                  textDecoration: 'underline',
                },
              },
              '& strong': {
                fontWeight: 600,
                color: theme.palette.text.primary,
              },
              '& em': {
                fontStyle: 'italic',
              },
              '& ul, & ol': {
                pl: 3,
                mb: 2,
              },
              '& li': {
                fontSize: '1rem',
                lineHeight: 1.6,
                mb: 0.5,
              },
              '& blockquote': {
                borderLeft: `4px solid ${theme.palette.primary.main}`,
                pl: 2,
                ml: 0,
                my: 2,
                color: theme.palette.text.secondary,
                fontStyle: 'italic',
              },
            }}
            dangerouslySetInnerHTML={{ __html: sanitizedContent }}
          />
        ) : (
          <Typography variant="body1" color="text.secondary" align="center" sx={{ py: 4 }}>
            Kein Inhalt verfügbar. Bitte öffnen Sie den Artikel über den Original-Link.
          </Typography>
        )}
      </DialogContent>

      {/* Footer */}
      <DialogActions
        sx={{
          px: 3,
          py: 2,
          borderTop: `1px solid ${theme.palette.divider}`,
          justifyContent: 'space-between',
        }}
      >
        <Button
          variant="outlined"
          size="small"
          startIcon={<ShareIcon />}
          onClick={handleShare}
        >
          Teilen
        </Button>
        <Button
          variant="contained"
          size="small"
          startIcon={<LaunchIcon />}
          onClick={handleOpenOriginal}
        >
          Original öffnen
        </Button>
      </DialogActions>
    </Dialog>
  );
}
