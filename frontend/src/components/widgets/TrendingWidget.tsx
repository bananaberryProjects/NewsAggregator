import { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Card, CardContent, Typography, Box, Skeleton, Chip, Tooltip, Fade, Avatar, Badge,
  IconButton, Divider
} from '@mui/material'
import {
  TrendingUp, TrendingDown, Minus, Flame, RefreshCw, Zap
} from 'lucide-react'

interface TrendingTopic {
  term: string
  count: number
  trend: 'up' | 'down' | 'stable'
  deltaPercent: number
  feeds: number
  breaking: boolean
}

interface BreakingAlert {
  term: string
  newArticles: number
  feedCount: number
  snippet: string
}

interface TrendingData {
  window: string
  generatedAt: string
  topics: TrendingTopic[]
  breakingAlerts: BreakingAlert[]
}

const GRADIENT = 'linear-gradient(135deg, #FFB74D 0%, #FF6D00 20%, #FF1744 50%, #6A0000 100%)'

const TREND_CONFIG = {
  up:     { color: '#66BB6A', icon: TrendingUp,   label: 'Steigend' },
  down:   { color: '#EF5350', icon: TrendingDown, label: 'Fallend' },
  stable: { color: '#FFA726', icon: Minus,        label: 'Stabil' }
}

export function TrendingWidget({ refreshIntervalSeconds = 300 }: { refreshIntervalSeconds?: number }) {
  const navigate = useNavigate()
  const [data, setData] = useState<TrendingData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [mounted, setMounted] = useState(false)

  const fetchTrending = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      // Nutze /fast für schnelle Polling (kein KI-Overhead)
      const response = await fetch('/api/trending/fast?hours=24&limit=20')
      if (!response.ok) throw new Error(`HTTP ${response.status}`)
      const result: TrendingData = await response.json()
      setData(result)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Fehler beim Laden der Trends')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const timer = setTimeout(() => setMounted(true), 50)
    return () => clearTimeout(timer)
  }, [])

  useEffect(() => {
    fetchTrending()
    const interval = setInterval(fetchTrending, refreshIntervalSeconds * 1000)
    return () => clearInterval(interval)
  }, [fetchTrending, refreshIntervalSeconds])

  const navigateToTopic = (term: string) => {
    navigate('/articles', { state: { searchQuery: term } })
  }

  const formatTime = (iso: string) =>
    new Date(iso).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })

  const maxCount = data && data.topics.length > 0
    ? Math.max(...data.topics.map(t => t.count))
    : 1

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
                <Zap size={28} />
              </Avatar>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 600, lineHeight: 1.3, color: '#fff' }}>
                  Trending
                </Typography>
                {!loading && data?.generatedAt && (
                  <Typography variant="body2" sx={{ opacity: 0.85, mt: 0.3 }}>
                    Aktualisiert um {formatTime(data.generatedAt)}
                  </Typography>
                )}
                {loading && (
                  <Skeleton variant="text" width="40%" height={18} sx={{ bgcolor: 'rgba(255,255,255,0.2)', mt: 0.3 }} />
                )}
              </Box>
            </Box>
            <Tooltip title="Aktualisieren">
              <IconButton size="small" onClick={fetchTrending} disabled={loading} sx={{ color: '#fff' }}>
                <Box sx={{ ...(loading && { animation: 'spin 1s linear infinite' }) }}>
                  <RefreshCw size={20} />
                </Box>
              </IconButton>
            </Tooltip>
          </Box>

          {loading && !data ? (
            <Box>
              <Skeleton variant="text" width="60%" height={24} sx={{ bgcolor: 'rgba(255,255,255,0.2)', mb: 2 }} />
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, mb: 2 }}>
                {[1, 2, 3, 4, 5, 6].map((i) => (
                  <Skeleton key={i} variant="rounded" width={80 + i * 15} height={32} sx={{ bgcolor: 'rgba(255,255,255,0.15)' }} />
                ))}
              </Box>
            </Box>
          ) : error ? (
            <Typography variant="body2" sx={{ opacity: 0.9 }}>
              {error}
            </Typography>
          ) : data ? (
            <Box>
              {/* Breaking Alerts */}
              {data.breakingAlerts.length > 0 && (
                <Box sx={{ mb: 2 }}>
                  {data.breakingAlerts.map((alert) => (
                    <Box
                      key={alert.term}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 1.5,
                        py: 1,
                        px: 1.5,
                        borderRadius: 2,
                        bgcolor: 'rgba(255,255,255,0.15)',
                        border: '1px solid',
                        borderColor: 'rgba(255,255,255,0.3)',
                        mb: 1,
                        cursor: 'pointer',
                        transition: 'all 0.2s ease',
                        '&:hover': {
                          bgcolor: 'rgba(255,255,255,0.25)',
                          transform: 'translateX(4px)'
                        }
                      }}
                      onClick={() => navigateToTopic(alert.term)}
                    >
                      <Flame size={20} color="#FFEB3B" />
                      <Box sx={{ flex: 1 }}>
                        <Typography variant="body2" sx={{ fontWeight: 700, color: '#fff' }}>
                          🔥 {alert.term}
                        </Typography>
                        <Typography variant="caption" sx={{ opacity: 0.85 }}>
                          {alert.snippet}
                        </Typography>
                      </Box>
                    </Box>
                  ))}
                </Box>
              )}

              {/* Chip Cloud */}
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1.2, justifyContent: 'flex-start' }}>
                {data.topics.map((topic, idx) => {
                  const trendCfg = TREND_CONFIG[topic.trend]
                  const TrendIcon = trendCfg.icon
                  const fontSize = 0.75 + (topic.count / maxCount) * 0.75
                  const isBreaking = topic.breaking

                  return (
                    <Fade in key={topic.term} timeout={300 + idx * 80}>
                      <Tooltip
                        title={`${topic.count} Artikel · ${topic.feeds} Feeds · ${trendCfg.label}${topic.deltaPercent !== 0 ? ` (${topic.deltaPercent > 0 ? '+' : ''}${topic.deltaPercent}%)` : ''}`}
                        arrow
                      >
                        <Badge
                          overlap="circular"
                          badgeContent={isBreaking ? <Flame size={12} color="#FFEB3B" /> : null}
                          sx={{
                            '& .MuiBadge-badge': {
                              bgcolor: 'transparent',
                              p: 0,
                              minWidth: 0,
                              width: 18,
                              height: 18,
                              top: 2,
                              right: 2,
                            }
                          }}
                        >
                          <Chip
                            label={
                              <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                <Typography
                                  sx={{
                                    fontSize: `clamp(0.75rem, ${fontSize}rem, 1.5rem)`,
                                    fontWeight: topic.count > maxCount * 0.6 ? 700 : 500,
                                    lineHeight: 1.2,
                                  }}
                                >
                                  {topic.term}
                                </Typography>
                                <TrendIcon size={14} color={trendCfg.color} />
                                {topic.deltaPercent !== 0 && (
                                  <Typography
                                    variant="caption"
                                    sx={{
                                      fontSize: '0.65rem',
                                      color: topic.deltaPercent > 0 ? '#66BB6A' : '#EF5350',
                                      fontWeight: 600,
                                    }}
                                  >
                                    {topic.deltaPercent > 0 ? '+' : ''}{topic.deltaPercent}%
                                  </Typography>
                                )}
                              </Box>
                            }
                            onClick={() => navigateToTopic(topic.term)}
                            sx={{
                              bgcolor: isBreaking
                                ? 'rgba(239,83,80,0.35)'
                                : 'rgba(255,255,255,0.12)',
                              color: '#fff',
                              border: '1px solid',
                              borderColor: isBreaking
                                ? 'rgba(255,235,59,0.5)'
                                : 'rgba(255,255,255,0.2)',
                              borderRadius: 2,
                              px: 1,
                              py: 0.5,
                              cursor: 'pointer',
                              transition: 'all 0.2s ease',
                              '&:hover': {
                                bgcolor: 'rgba(255,255,255,0.25)',
                                borderColor: 'rgba(255,255,255,0.5)',
                                transform: 'scale(1.05)',
                              },
                              '& .MuiChip-label': {
                                px: 0.5,
                                py: 0.3,
                              },
                            }}
                          />
                        </Badge>
                      </Tooltip>
                    </Fade>
                  )
                })}
              </Box>

              {data.topics.length === 0 && (
                <Typography variant="body2" sx={{ opacity: 0.8, textAlign: 'center', py: 2 }}>
                  Keine Trending-Themen im letzten Zeitfenster.
                </Typography>
              )}

              {/* Footer */}
              <Divider sx={{ mt: 2, mb: 1, borderColor: 'rgba(255,255,255,0.15)' }} />
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Typography variant="caption" sx={{ opacity: 0.7 }}>
                  Fenster: {data.window}
                </Typography>
                <Typography variant="caption" sx={{ opacity: 0.7 }}>
                  {data.topics.length} {data.topics.length === 1 ? 'Thema' : 'Themen'}
                </Typography>
              </Box>
            </Box>
          ) : null}
        </CardContent>

        <style>{`
          @keyframes spin { 100% { transform: rotate(360deg); } }
        `}</style>
      </Card>
    </Fade>
  )
}
