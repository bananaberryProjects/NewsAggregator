import { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card, CardContent, Typography, Box, Skeleton, Alert, IconButton, Tooltip,
  Fade, Avatar, Divider
} from '@mui/material'
import {
  RotateCw, Newspaper, Smile, Meh, Frown, ArrowRight
} from 'lucide-react'

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

const GRADIENT = 'linear-gradient(135deg, #7C4DFF 0%, #651FFF 50%, #6200EA 100%)'

const SENTIMENT_CONFIG = {
  positive: { color: '#66BB6A', icon: Smile, label: 'Positiv' },
  neutral:  { color: '#FFA726', icon: Meh,   label: 'Neutral' },
  negative: { color: '#EF5350', icon: Frown, label: 'Negativ' }
}

function SentimentIcon({ sentiment, size = 20 }: { sentiment: string; size?: number }) {
  const cfg = SENTIMENT_CONFIG[sentiment as keyof typeof SENTIMENT_CONFIG] || SENTIMENT_CONFIG.neutral
  const Icon = cfg.icon
  return <Icon size={size} color={cfg.color} />
}

export function AiSummaryWidget({ refreshIntervalSeconds = 600 }: { refreshIntervalSeconds?: number }) {
  const navigate = useNavigate()
  const [summary, setSummary] = useState<AiSummaryData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [mounted, setMounted] = useState(false)

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
    const timer = setTimeout(() => setMounted(true), 50)
    return () => clearTimeout(timer)
  }, [])

  useEffect(() => {
    fetchSummary()
    const interval = setInterval(fetchSummary, refreshIntervalSeconds * 1000)
    return () => clearInterval(interval)
  }, [fetchSummary, refreshIntervalSeconds])

  const navigateToTopic = (topicName: string) => {
    navigate('/articles', { state: { searchQuery: topicName } })
  }

  const formatTime = (iso: string) =>
    new Date(iso).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })

  return (
    <Fade in={mounted} timeout={600}>
      <Card
        sx={{
          borderRadius: 3,
          overflow: 'visible',
          background: GRADIENT,
          color: '#fff',
          position: 'relative',
          mb: 3,
          boxShadow: (theme) => theme.shadows[4],
        }}
      >
        <CardContent sx={{ p: 3, pb: 2, '&:last-child': { pb: 2 } }}>
          {/* Header */}
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Avatar
                sx={{
                  width: 48,
                  height: 48,
                  bgcolor: 'rgba(255,255,255,0.2)',
                  color: '#fff',
                  backdropFilter: 'blur(4px)',
                }}
              >
                <Newspaper size={28} />
              </Avatar>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 600, lineHeight: 1.3, color: '#fff' }}>
                  Tagesüberblick
                </Typography>
                {!loading && summary?.generatedAt && (
                  <Typography variant="body2" sx={{ opacity: 0.85, mt: 0.3 }}>
                    Generiert um {formatTime(summary.generatedAt)}
                  </Typography>
                )}
                {loading && (
                  <Skeleton variant="text" width="40%" height={18} sx={{ bgcolor: 'rgba(255,255,255,0.2)', mt: 0.3 }} />
                )}
              </Box>
            </Box>
            <Tooltip title="Neu generieren">
              <IconButton size="small" onClick={fetchSummary} disabled={loading} sx={{ color: '#fff' }}>
                <Box sx={{ ...(loading && { animation: 'spin 1s linear infinite' }) }}>
                  <RotateCw size={20} />
                </Box>
              </IconButton>
            </Tooltip>
          </Box>

          {loading && !summary ? (
            <Box>
              <Skeleton variant="text" width="90%" height={20} sx={{ bgcolor: 'rgba(255,255,255,0.2)', mb: 1 }} />
              <Skeleton variant="text" width="70%" height={20} sx={{ bgcolor: 'rgba(255,255,255,0.2)', mb: 2 }} />
              <Skeleton variant="text" width="100%" height={40} sx={{ bgcolor: 'rgba(255,255,255,0.15)', mb: 1 }} />
              <Skeleton variant="text" width="100%" height={40} sx={{ bgcolor: 'rgba(255,255,255,0.15)', mb: 1 }} />
              <Skeleton variant="text" width="100%" height={40} sx={{ bgcolor: 'rgba(255,255,255,0.15)', mb: 2 }} />
              <Skeleton variant="text" width="50%" height={18} sx={{ bgcolor: 'rgba(255,255,255,0.2)', mb: 1 }} />
              <Skeleton variant="text" width="100%" height={36} sx={{ bgcolor: 'rgba(255,255,255,0.15)', mb: 1 }} />
              <Skeleton variant="text" width="100%" height={36} sx={{ bgcolor: 'rgba(255,255,255,0.15)' }} />
            </Box>
          ) : error ? (
            <Alert severity="warning" sx={{ m: 0 }}>
              {error}
            </Alert>
          ) : summary ? (
            <Box>
              {/* Kategorien */}
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                {summary.categories.map((cat, idx) => (
                  <Fade in key={cat.name} timeout={300 + idx * 100}>
                    <Box>
                      <Box
                        sx={{
                          display: 'flex',
                          alignItems: 'flex-start',
                          gap: 1.5,
                          py: 1.25,
                          px: 0.5,
                        }}
                      >
                        <SentimentIcon sentiment={cat.sentiment} size={26} />
                        <Box sx={{ flex: 1, minWidth: 0 }}>
                          <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.75, flexWrap: 'wrap' }}>
                            <Typography
                              variant="subtitle1"
                              sx={{
                                fontWeight: 700,
                                color: '#fff',
                              }}
                            >
                              {cat.name}
                            </Typography>
                            <Typography
                              variant="body2"
                              sx={{ lineHeight: 1.4, color: 'rgba(255,255,255,0.85)' }}
                            >
                              {cat.summary}
                            </Typography>
                          </Box>
                        </Box>
                      </Box>
                      {idx < summary.categories.length - 1 && (
                        <Divider sx={{ ml: 4, borderColor: 'rgba(255,255,255,0.15)' }} />
                      )}
                    </Box>
                  </Fade>
                ))}
              </Box>

              {/* Top 3 Themen */}
              {summary.topTopics.length > 0 && (
                <Box sx={{ mt: 2.5 }}>
                  <Divider sx={{ mb: 2, borderColor: 'rgba(255,255,255,0.2)' }} />
                  <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1.5, color: '#fff' }}>
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
                            bgcolor: 'rgba(255,255,255,0.1)',
                            border: '1px solid',
                            borderColor: 'rgba(255,255,255,0.2)',
                            cursor: 'pointer',
                            transition: 'all 0.2s ease',
                            '&:hover': {
                              bgcolor: 'rgba(255,255,255,0.2)',
                              borderColor: 'rgba(255,255,255,0.4)',
                              boxShadow: (theme) => `0 0 12px ${theme.palette.primary.light}40`,
                              transform: 'translateX(4px)'
                            }
                          }}
                        >
                          <Typography
                            variant="body2"
                            sx={{
                              fontWeight: 700,
                              color: '#fff',
                              minWidth: 24,
                              fontSize: '0.95rem'
                            }}
                          >
                            {idx + 1}.
                          </Typography>
                          <Typography variant="body2" sx={{ fontWeight: 500, flex: 1, color: '#fff' }}>
                            {topic.name}
                          </Typography>
                          <Typography variant="caption" sx={{ minWidth: 60, textAlign: 'right', color: 'rgba(255,255,255,0.7)' }}>
                            ({topic.articleCount} {topic.articleCount === 1 ? 'Artikel' : 'Artikel'})
                          </Typography>
                          <ArrowRight size={16} color="rgba(255,255,255,0.7)" />
                        </Box>
                      </Fade>
                    ))}
                  </Box>
                </Box>
              )}
            </Box>
          ) : (
            <Alert severity="info" sx={{ m: 0, bgcolor: 'rgba(255,255,255,0.9)' }}>
              Keine KI-Zusammenfassung verfügbar.
            </Alert>
          )}
        </CardContent>

        <style>{`
          @keyframes spin { 100% { transform: rotate(360deg); } }
          .spin { animation: spin 1s linear infinite; }
        `}</style>
      </Card>
    </Fade>
  )
}
