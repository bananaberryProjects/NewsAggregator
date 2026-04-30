import { useEffect, useState, useCallback } from 'react'
import {
  Card, CardContent, Typography, Box, Skeleton, Alert, IconButton, Fade, Divider, Avatar
} from '@mui/material'
import {
  Refresh, TrendingUp, TrendingDown, Remove, ShowChart,
  Psychology as PsychologyIcon
} from '@mui/icons-material'
import { type MarketInsight, marketApi } from '../../api/client'

const STORAGE_KEY = 'market-config'

export interface MarketConfig {
  stockSymbols: string[]
  cryptoIds: string[]
}

export function loadMarketConfig(): MarketConfig {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed.stockSymbols) && Array.isArray(parsed.cryptoIds)) {
        return parsed as MarketConfig
      }
    } catch {}
  }
  return {
    stockSymbols: ['DAX', 'S&P500', 'NASDAQ'],
    cryptoIds: ['bitcoin', 'ethereum'],
  }
}

export function saveMarketConfig(config: MarketConfig) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
  window.dispatchEvent(new CustomEvent('market-config-changed'))
}

const SENTIMENT_CONFIG = {
  bullish: {
    gradient: 'linear-gradient(135deg, #4caf50 0%, #81c784 100%)',
    icon: <TrendingUp sx={{ fontSize: 32 }} />,
    label: 'Bullisch',
  },
  bearish: {
    gradient: 'linear-gradient(135deg, #f44336 0%, #e57373 100%)',
    icon: <TrendingDown sx={{ fontSize: 32 }} />,
    label: 'Bärisch',
  },
  neutral: {
    gradient: 'linear-gradient(135deg, #607d8b 0%, #90a4ae 100%)',
    icon: <Remove sx={{ fontSize: 32 }} />,
    label: 'Neutral',
  },
}

function formatNumber(num: number, decimals = 0): string {
  return new Intl.NumberFormat('de-DE', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(num)
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
}

export function MarketWidget({ refreshIntervalSeconds = 300 }: { refreshIntervalSeconds?: number }) {
  const [market, setMarket] = useState<MarketInsight | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [mounted, setMounted] = useState(false)
  const [config] = useState<MarketConfig>(loadMarketConfig)

  useEffect(() => {
    const timer = setTimeout(() => setMounted(true), 50)
    return () => clearTimeout(timer)
  }, [])

  const fetchMarket = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await marketApi.getInsight(config.stockSymbols, config.cryptoIds)
      setMarket(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Fehler beim Laden der Marktdaten')
    } finally {
      setLoading(false)
    }
  }, [config])

  useEffect(() => {
    fetchMarket()
  }, [fetchMarket])

  // Auto-refresh
  useEffect(() => {
    const interval = setInterval(() => {
      fetchMarket()
    }, refreshIntervalSeconds * 1000)
    return () => clearInterval(interval)
  }, [fetchMarket, refreshIntervalSeconds])

  // Subscribe to config changes
  useEffect(() => {
    const handler = () => {
      fetchMarket()
    }
    window.addEventListener('market-config-changed', handler)
    return () => window.removeEventListener('market-config-changed', handler)
  }, [fetchMarket])

  const sentiment = SENTIMENT_CONFIG[market?.marketSentiment || 'neutral']

  return (
    <Fade in={mounted} timeout={500}>
      <Card
        sx={{
          borderRadius: 3,
          overflow: 'hidden',
          background: sentiment.gradient,
          color: '#fff',
          position: 'relative',
          boxShadow: (t) => t.shadows[4],
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <CardContent sx={{ p: 3, pb: '16px !important', flex: 1, display: 'flex', flexDirection: 'column' }}>
          {/* Header */}
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
              <Avatar sx={{ width: 40, height: 40, bgcolor: 'rgba(255,255,255,0.2)', color: '#fff' }}>
                <ShowChart sx={{ fontSize: 22 }} />
              </Avatar>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 600, lineHeight: 1.2, color: '#fff' }}>
                  Markt
                </Typography>
                {market && (
                  <Typography variant="caption" sx={{ opacity: 0.85 }}>
                    {sentiment.label} • {formatTime(market.updatedAt)}
                  </Typography>
                )}
              </Box>
            </Box>
            <IconButton
              size="small"
              onClick={fetchMarket}
              disabled={loading}
              sx={{ color: '#fff' }}
            >
              <Refresh sx={{ fontSize: 20, ...(loading && { animation: 'spin 1s linear infinite' }) }} />
            </IconButton>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2, bgcolor: 'rgba(255,255,255,0.9)' }}>
              {error}
            </Alert>
          )}

          {loading && !market ? (
            <Box>
              {[1, 2, 3].map((i) => (
                <Box key={i} sx={{ mb: 2 }}>
                  <Skeleton variant="text" width="80%" height={28} sx={{ bgcolor: 'rgba(255,255,255,0.2)' }} />
                  <Skeleton variant="text" width="60%" height={20} sx={{ bgcolor: 'rgba(255,255,255,0.2)' }} />
                </Box>
              ))}
            </Box>
          ) : market ? (
            <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
              {/* 2-Spalten Layout */}
              {(market.stocks.length > 0 || market.cryptos.length > 0) && (
                <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
                  {/* Stocks */}
                  {market.stocks.length > 0 && (
                    <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 1 }}>
                      <Typography
                        variant="overline"
                        sx={{ fontWeight: 600, opacity: 0.7, fontSize: '0.7rem', lineHeight: 1, mb: 0.5 }}
                      >
                        Indizes
                      </Typography>
                      {market.stocks.map((stock) => (
                        <Box
                          key={stock.symbol}
                          sx={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            py: 0.5,
                          }}
                        >
                          <Box>
                            <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#fff' }}>
                              {stock.name}
                            </Typography>
                            <Typography variant="caption" sx={{ opacity: 0.8 }}>
                              {stock.symbol}
                            </Typography>
                          </Box>
                          <Box sx={{ textAlign: 'right' }}>
                            <Typography variant="body2" sx={{ fontWeight: 500, color: '#fff' }}>
                              {formatNumber(stock.value, 0)} {stock.currency}
                            </Typography>
                            <Box
                              sx={{
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'flex-end',
                                gap: 0.3,
                                mt: 0.3,
                                color: stock.changePercent >= 0 ? '#c8e6c9' : '#ffcdd2',
                              }}
                            >
                              {stock.changePercent >= 0 ? (
                                <TrendingUp sx={{ fontSize: 14 }} />
                              ) : (
                                <TrendingDown sx={{ fontSize: 14 }} />
                              )}
                              <Typography variant="caption" sx={{ fontWeight: 600 }}>
                                {stock.changePercent >= 0 ? '+' : ''}{formatNumber(stock.changePercent, 2)}%
                              </Typography>
                            </Box>
                          </Box>
                        </Box>
                      ))}
                    </Box>
                  )}

                  {market.stocks.length > 0 && market.cryptos.length > 0 && (
                    <Divider
                      orientation="vertical"
                      flexItem
                      sx={{ borderColor: 'rgba(255,255,255,0.3)' }}
                    />
                  )}

                  {/* Cryptos */}
                  {market.cryptos.length > 0 && (
                    <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 1 }}>
                      <Typography
                        variant="overline"
                        sx={{ fontWeight: 600, opacity: 0.7, fontSize: '0.7rem', lineHeight: 1, mb: 0.5 }}
                      >
                        Krypto
                      </Typography>
                      {market.cryptos.map((crypto) => (
                        <Box
                          key={crypto.coinId}
                          sx={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            py: 0.5,
                          }}
                        >
                          <Box>
                            <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#fff' }}>
                              {crypto.name}
                            </Typography>
                            <Typography variant="caption" sx={{ opacity: 0.8 }}>
                              {crypto.symbol}
                            </Typography>
                          </Box>
                          <Box sx={{ textAlign: 'right' }}>
                            <Typography variant="body2" sx={{ fontWeight: 500, color: '#fff' }}>
                              {crypto.symbol === 'BTC' ? formatNumber(crypto.priceUsd, 0) : formatNumber(crypto.priceUsd)} $
                            </Typography>
                            <Box
                              sx={{
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'flex-end',
                                gap: 0.3,
                                mt: 0.3,
                                color: crypto.priceChangePercentage24h >= 0 ? '#c8e6c9' : '#ffcdd2',
                              }}
                            >
                              {crypto.priceChangePercentage24h >= 0 ? (
                                <TrendingUp sx={{ fontSize: 14 }} />
                              ) : (
                                <TrendingDown sx={{ fontSize: 14 }} />
                              )}
                              <Typography variant="caption" sx={{ fontWeight: 600 }}>
                                {crypto.priceChangePercentage24h >= 0 ? '+' : ''}{formatNumber(crypto.priceChangePercentage24h, 2)}%
                              </Typography>
                            </Box>
                          </Box>
                        </Box>
                      ))}
                    </Box>
                  )}
                </Box>
              )}

              {/* KI Insight */}
              {market.insight && (
                <Box
                  sx={{
                    bgcolor: 'rgba(255,255,255,0.15)',
                    borderRadius: 2,
                    p: 1.5,
                    mb: 2,
                    backdropFilter: 'blur(4px)',
                    mt: market.stocks.length > 0 || market.cryptos.length > 0 ? 2 : 0,
                  }}
                >
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                    <PsychologyIcon sx={{ fontSize: 16, color: '#fff' }} />
                    <Typography variant="caption" sx={{ fontWeight: 600, opacity: 0.9 }}>
                      KI-Einblick
                    </Typography>
                  </Box>
                  <Typography variant="body2" sx={{ opacity: 0.95, fontStyle: 'italic' }}>
                    „{market.insight}"
                  </Typography>
                </Box>
              )}
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
