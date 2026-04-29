import { useEffect, useRef, useState, useCallback } from 'react'
import {
  Paper, CardContent, Typography, Box, Skeleton, Alert, IconButton, Button,
  Fade, Tooltip, Avatar, List, ListItem, ListItemAvatar, ListItemText, Divider
} from '@mui/material'
import {
  Refresh, SmartToy, VolumeUp, Stop,
  SentimentSatisfiedAlt, SentimentNeutral, SentimentVeryDissatisfied,
  ArrowForward, PlayArrow
} from '@mui/icons-material'

interface AiCategory {
  name: string
  summary: string
  articleCount: number
  sentiment: 'positive' | 'neutral' | 'negative'
}

interface AiTopic {
  name: string
  articleCount: number
  trending: boolean
}

interface AiSummaryData {
  categories: AiCategory[]
  topTopics: AiTopic[]
  generatedAt: string
}

const SENTIMENT_CONFIG = {
  positive: { color: '#66BB6A', iconColor: '#4CAF50', icon: SentimentSatisfiedAlt, trackColor: '#E8F5E9', label: 'Positiv' },
  neutral:  { color: '#FFA726', iconColor: '#FF9800', icon: SentimentNeutral,       trackColor: '#FFF3E0', label: 'Neutral' },
  negative: { color: '#EF5350', iconColor: '#F44336', icon: SentimentVeryDissatisfied, trackColor: '#FFEBEE', label: 'Negativ' }
}

/** Sentiment-Balken als 10-Segment-Visualisierung (wie im Sketch) */
function SentimentBar({ sentiment }: { sentiment: string }) {
  const config = SENTIMENT_CONFIG[sentiment as keyof typeof SENTIMENT_CONFIG] || SENTIMENT_CONFIG.neutral

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, width: '100%' }}>
      <Box
        sx={{
          flex: 1,
          display: 'flex',
          gap: 0.4,
          alignItems: 'center',
          maxWidth: 140
        }}
      >
        {Array.from({ length: 10 }).map((_, i) => {
          const filled = sentiment === 'positive'
            ? i < 8
            : sentiment === 'negative'
            ? i < 3
            : i < 5
          return (
            <Box
              key={i}
              sx={{
                flex: 1,
                height: 10,
                borderRadius: 1,
                bgcolor: filled ? config.color : config.trackColor,
                transition: 'background-color 0.3s ease'
              }}
            />
          )
        })}
      </Box>
    </Box>
  )
}

export function AiSummaryWidget({ refreshIntervalSeconds = 600 }: { refreshIntervalSeconds?: number }) {
  const [summary, setSummary] = useState<AiSummaryData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isSpeaking, setIsSpeaking] = useState(false)
  const [speakingCategory, setSpeakingCategory] = useState<string | null>(null)
  const utteranceRef = useRef<SpeechSynthesisUtterance | null>(null)

  const fetchSummary = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch('/api/summary/v2')
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const data: AiSummaryData = await response.json()
      setSummary(data)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Fehler beim Laden der KI-Zusammenfassung')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchSummary()
    const interval = setInterval(fetchSummary, refreshIntervalSeconds * 1000)
    return () => clearInterval(interval)
  }, [fetchSummary, refreshIntervalSeconds])

  const handleSpeak = (categorySummary?: string) => {
    if (!summary || !window.speechSynthesis) return
    if (isSpeaking) {
      window.speechSynthesis.cancel()
      setIsSpeaking(false)
      setSpeakingCategory(null)
      return
    }

    const text = categorySummary || (
      summary.categories
        .map(c => `${c.name}: ${c.summary}`)
        .join('.\n') +
      '\n\nTop Themen: ' + summary.topTopics.map(t => t.name).join(', ')
    )

    const utterance = new SpeechSynthesisUtterance(text)
    utterance.lang = 'de-DE'
    utterance.onend = () => {
      setIsSpeaking(false)
      setSpeakingCategory(null)
    }
    utterance.onerror = () => {
      setIsSpeaking(false)
      setSpeakingCategory(null)
    }
    utteranceRef.current = utterance
    window.speechSynthesis.speak(utterance)
    setIsSpeaking(true)
    if (categorySummary) {
      setSpeakingCategory(summary.categories.find(c => c.summary === categorySummary)?.name || null)
    }
  }

  const handleSpeakCategory = (cat: AiCategory) => {
    if (isSpeaking) {
      window.speechSynthesis.cancel()
      setIsSpeaking(false)
      setSpeakingCategory(null)
      return
    }
    handleSpeak(`${cat.name}: ${cat.summary}`)
    setSpeakingCategory(cat.name)
  }

  const formatTime = (iso: string) =>
    new Date(iso).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })

  const navigateToCategory = (categoryName: string) => {
    window.dispatchEvent(new CustomEvent('navigate-to', {
      detail: { view: 'articles', search: categoryName }
    }))
  }

  const navigateToTopic = (topicName: string) => {
    window.dispatchEvent(new CustomEvent('navigate-to', {
      detail: { view: 'articles', search: topicName }
    }))
  }

  // Sentiment-Dot Farbe
  const getSentimentDot = (sentiment: string) => {
    const cfg = SENTIMENT_CONFIG[sentiment as keyof typeof SENTIMENT_CONFIG] || SENTIMENT_CONFIG.neutral
    return cfg.iconColor
  }

  return (
    <Paper
      elevation={0}
      sx={{
        minHeight: 200,
        display: 'flex',
        flexDirection: 'column',
        borderRadius: 3,
        bgcolor: 'background.paper',
        border: 1,
        borderColor: 'divider',
        overflow: 'hidden',
        transition: 'transform 0.25s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.25s ease',
        '&:hover': {
          boxShadow: (theme) => theme.shadows[4],
          transform: 'translateY(-2px)'
        }
      }}
    >
      {/* Header mit Gradient */}
      <Box sx={{
        height: 56,
        background: 'linear-gradient(135deg, #7C4DFF 0%, #651FFF 50%, #6200EA 100%)',
        display: 'flex', alignItems: 'center', px: 2.5, justifyContent: 'space-between'
      }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <SmartToy sx={{ color: 'white', fontSize: 26 }} />
          <Typography variant="h6" sx={{ color: 'white', fontWeight: 500, fontSize: '1.1rem' }}>
            KI-Tagesüberblick
          </Typography>
        </Box>
        <Tooltip title="Neu generieren">
          <IconButton size="small" onClick={fetchSummary} disabled={loading} sx={{ color: 'white' }}>
            <Refresh className={loading ? 'spin' : ''} />
          </IconButton>
        </Tooltip>
      </Box>

      <CardContent sx={{ flex: 1, p: 0 }}>
        {loading && !summary ? (
          <Box sx={{ p: 2 }}>
            <Skeleton width="80%" height={24} sx={{ mb: 1 }} />
            <Skeleton width="60%" height={18} sx={{ mb: 2 }} />
            <Skeleton width="100%" height={48} sx={{ mb: 1 }} />
            <Skeleton width="100%" height={48} sx={{ mb: 1 }} />
            <Skeleton width="100%" height={48} />
            <Divider sx={{ my: 2 }} />
            <Skeleton width="50%" height={18} sx={{ mb: 1 }} />
            <Skeleton width="100%" height={36} sx={{ mb: 1 }} />
            <Skeleton width="100%" height={36} />
          </Box>
        ) : error ? (
          <Alert severity="warning" sx={{ m: 2 }}>
            {error}
          </Alert>
        ) : summary ? (
          <Box>
            {/* Kategorien als Listen-Zeilen */}
            <List sx={{ py: 0 }}>
              {summary.categories.map((cat, idx) => (
                <Fade in key={cat.name} timeout={300 + idx * 100}>
                  <Box>
                    <ListItem
                      sx={{
                        py: 1.5,
                        px: 2.5,
                        transition: 'background-color 0.2s ease',
                        '&:hover': { bgcolor: 'action.hover' },
                        cursor: 'pointer'
                      }}
                      onClick={() => navigateToCategory(cat.name)}
                    >
                      <ListItemAvatar sx={{ minWidth: 36 }}>
                        <Avatar
                          sx={{
                            width: 12,
                            height: 12,
                            bgcolor: getSentimentDot(cat.sentiment)
                          }}
                        />
                      </ListItemAvatar>

                      <ListItemText
                        primary={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                            <Typography
                              variant="body1"
                              sx={{
                                fontWeight: 600,
                                color: 'text.primary',
                                fontSize: '0.95rem'
                              }}
                            >
                              {cat.name}
                            </Typography>
                          </Box>
                        }
                        secondary={
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, mt: 0.5, width: '100%' }}>
                            <Typography variant="caption" color="text.secondary" sx={{ minWidth: 70 }}>
                              {cat.articleCount} {cat.articleCount === 1 ? 'Artikel' : 'Artikel'}
                            </Typography>
                            <SentimentBar sentiment={cat.sentiment} />
                          </Box>
                        }
                      />

                      <Tooltip title={speakingCategory === cat.name ? 'Stoppen' : 'Zusammenfassung vorlesen'}>
                        <IconButton
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation()
                            handleSpeakCategory(cat)
                          }}
                          sx={{
                            color: speakingCategory === cat.name ? 'primary.main' : 'text.secondary',
                            bgcolor: speakingCategory === cat.name ? 'primary.lighter' : 'transparent',
                            transition: 'all 0.2s ease',
                            '&:hover': { bgcolor: 'action.selected', color: 'primary.main' }
                          }}
                        >
                          {speakingCategory === cat.name ? <Stop fontSize="small" /> : <PlayArrow fontSize="small" />}
                        </IconButton>
                      </Tooltip>
                    </ListItem>
                    {idx < summary.categories.length - 1 && (
                      <Divider component="li" variant="inset" sx={{ ml: 7 }} />
                    )}
                  </Box>
                </Fade>
              ))}
            </List>

            {/* Top 3 Themen */}
            {summary.topTopics.length > 0 && (
              <Box sx={{ px: 2.5, pb: 2, pt: 0.5 }}>
                <Divider sx={{ mb: 2 }} />
                <Typography variant="subtitle2" sx={{ fontWeight: 700, mb: 1.5, fontSize: '0.9rem', color: 'text.primary' }}>
                  Top 3 Themen
                </Typography>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
                  {summary.topTopics.slice(0, 3).map((topic, idx) => (
                    <Fade in key={topic.name} timeout={500 + idx * 150}>
                      <Box
                        onClick={() => navigateToTopic(topic.name)}
                        sx={{
                          display: 'flex',
                          alignItems: 'center',
                          gap: 1.5,
                          py: 1,
                          px: 1.5,
                          borderRadius: 2,
                          bgcolor: 'background.default',
                          border: '1px solid',
                          borderColor: 'divider',
                          cursor: 'pointer',
                          transition: 'all 0.2s ease',
                          '&:hover': {
                            bgcolor: 'action.hover',
                            borderColor: 'primary.light',
                            boxShadow: 1,
                            transform: 'translateX(4px)'
                          }
                        }}
                      >
                        <Typography
                          variant="body2"
                          sx={{
                            fontWeight: 700,
                            color: 'primary.main',
                            minWidth: 24,
                            fontSize: '0.95rem'
                          }}
                        >
                          {idx + 1}.
                        </Typography>
                        <Typography variant="body2" sx={{ fontWeight: 500, flex: 1, color: 'text.primary' }}>
                          {topic.name}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" sx={{ minWidth: 60, textAlign: 'right' }}>
                          ({topic.articleCount} {topic.articleCount === 1 ? 'Artikel' : 'Artikel'})
                        </Typography>
                        <ArrowForward fontSize="small" sx={{ color: 'text.secondary', fontSize: 16 }} />
                      </Box>
                    </Fade>
                  ))}
                </Box>
              </Box>
            )}

            {/* Footer: Vorlesen + Neu generieren */}
            <Box sx={{
              px: 2.5,
              py: 1.5,
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              borderTop: '1px solid',
              borderColor: 'divider'
            }}>
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button
                  size="small"
                  variant="outlined"
                  startIcon={isSpeaking ? <Stop /> : <VolumeUp />}
                  onClick={() => handleSpeak()}
                  disabled={!summary}
                  sx={{
                    textTransform: 'none',
                    borderRadius: 2,
                    px: 2,
                    py: 0.5,
                    borderColor: 'divider',
                    fontSize: '0.8rem'
                  }}
                >
                  {isSpeaking ? 'Stoppen' : '🔊 Zusammenfassung vorlesen'}
                </Button>
              </Box>

              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                {summary.generatedAt && (
                  <Typography variant="caption" color="text.secondary" sx={{ fontSize: '0.7rem' }}>
                    {formatTime(summary.generatedAt)}
                  </Typography>
                )}
                <Button
                  size="small"
                  variant="text"
                  startIcon={<Refresh className={loading ? 'spin' : ''} fontSize="small" />}
                  onClick={fetchSummary}
                  disabled={loading}
                  sx={{ textTransform: 'none', fontSize: '0.8rem', px: 1 }}
                >
                  Neu generieren
                </Button>
              </Box>
            </Box>
          </Box>
        ) : (
          <Alert severity="info" sx={{ m: 2 }}>
            Keine KI-Zusammenfassung verfügbar.
          </Alert>
        )}
      </CardContent>

      <style>{`
        @keyframes spin { 100% { transform: rotate(360deg); } }
        .spin { animation: spin 1s linear infinite; }
      `}</style>
    </Paper>
  )
}
