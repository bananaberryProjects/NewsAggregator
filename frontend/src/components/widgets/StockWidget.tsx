import React, { useEffect, useState } from 'react'
import { Card, CardContent, Typography, Box, Skeleton, Alert, IconButton, Divider, CardMedia } from '@mui/material'
import { TrendingUp, TrendingDown, Refresh, ShowChart, Remove } from '@mui/icons-material'

interface StockIndex {
  symbol: string
  name: string
  value: number
  change: number
  changePercent: number
}

type Sentiment = 'bullish' | 'bearish' | 'neutral'

interface SentimentConfig {
  type: Sentiment
  gradient: string
  icon: React.ReactNode
  label: string
}

const getMarketSentiment = (stocks: StockIndex[]): SentimentConfig => {
  if (stocks.length === 0) {
    return {
      type: 'neutral',
      gradient: 'linear-gradient(135deg, #607d8b 0%, #90a4ae 100%)',
      icon: <Remove sx={{ fontSize: 32 }} />,
      label: 'Neutral'
    }
  }

  const positiveCount = stocks.filter(s => s.change >= 0).length
  const negativeCount = stocks.filter(s => s.change < 0).length
  const total = stocks.length
  const positiveRatio = positiveCount / total
  const negativeRatio = negativeCount / total

  if (positiveRatio > 0.5) {
    return {
      type: 'bullish',
      gradient: 'linear-gradient(135deg, #4caf50 0%, #81c784 100%)',
      icon: <TrendingUp sx={{ fontSize: 32 }} />,
      label: 'Bullisch'
    }
  }

  if (negativeRatio > 0.5) {
    return {
      type: 'bearish',
      gradient: 'linear-gradient(135deg, #f44336 0%, #e57373 100%)',
      icon: <TrendingDown sx={{ fontSize: 32 }} />,
      label: 'Bärisch'
    }
  }

  return {
    type: 'neutral',
    gradient: 'linear-gradient(135deg, #607d8b 0%, #90a4ae 100%)',
    icon: <Remove sx={{ fontSize: 32 }} />,
    label: 'Neutral'
  }
}

interface StockWidgetProps {
  refreshIntervalSeconds?: number // in seconds, default 60
}

interface YahooChartResult {
  meta: {
    regularMarketPrice: number
    previousClose: number
    shortName?: string
    symbol: string
  }
}

interface YahooResponse {
  chart: {
    result?: YahooChartResult[]
    error?: { description: string }
  }
}

const SYMBOLS = [
  { symbol: '^GDAXI', displayName: 'DAX', shortName: 'DAX' },
  { symbol: '^GSPC', displayName: 'S&P 500', shortName: 'S&P500' },
  { symbol: 'BTC-USD', displayName: 'Bitcoin', shortName: 'BTC' }
]

const fetchYahooData = async (): Promise<StockIndex[]> => {
  const results: StockIndex[] = []

  await Promise.all(
    SYMBOLS.map(async ({ symbol, displayName, shortName }) => {
      try {
        const response = await fetch(
          `https://query1.finance.yahoo.com/v8/finance/chart/${encodeURIComponent(symbol)}?interval=1d&range=2d`
        )

        if (!response.ok) {
          throw new Error(`HTTP ${response.status} for ${symbol}`)
        }

        const data: YahooResponse = await response.json()

        if (data.chart.error || !data.chart.result || data.chart.result.length === 0) {
          throw new Error(`Keine Daten für ${symbol}`)
        }

        const result = data.chart.result[0]
        const meta = result.meta
        const currentPrice = meta.regularMarketPrice
        const previousClose = meta.previousClose

        const change = currentPrice - previousClose
        const changePercent = previousClose !== 0 ? (change / previousClose) * 100 : 0

        results.push({
          symbol: shortName,
          name: displayName,
          value: currentPrice,
          change,
          changePercent
        })
      } catch (err) {
        console.error(`Fehler beim Laden von ${symbol}:`, err)
      }
    })
  )

  return results.sort(
    (a, b) => SYMBOLS.findIndex(s => s.shortName === a.symbol) - SYMBOLS.findIndex(s => s.shortName === b.symbol)
  )
}

export function StockWidget({ refreshIntervalSeconds = 60 }: StockWidgetProps) {
  const [stocks, setStocks] = useState<StockIndex[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null)

  const fetchStocks = async () => {
    setLoading(true)
    setError(null)

    try {
      const data = await fetchYahooData()

      if (data.length === 0) {
        throw new Error('Keine Kursdaten verfügbar')
      }

      setStocks(data)
      setLastUpdate(new Date())
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Fehler beim Laden der Kursdaten')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchStocks()
  }, [])

  // Auto-refresh
  useEffect(() => {
    const interval = setInterval(() => {
      fetchStocks()
    }, refreshIntervalSeconds * 1000)

    return () => clearInterval(interval)
  }, [refreshIntervalSeconds])

  const formatNumber = (num: number, decimals: number = 0): string => {
    return new Intl.NumberFormat('de-DE', {
      minimumFractionDigits: decimals,
      maximumFractionDigits: decimals
    }).format(num)
  }

  const formatTime = (date: Date): string => {
    return date.toLocaleTimeString('de-DE', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const sentiment = getMarketSentiment(stocks)

  return (
    <Card sx={{ height: '100%', minHeight: 200 }}>
      <CardMedia
        sx={{
          height: 60,
          background: sentiment.gradient,
          display: 'flex',
          alignItems: 'center',
          px: 2,
          justifyContent: 'space-between'
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, color: 'white' }}>
          {sentiment.icon}
          <Box>
            <Typography variant="h6" component="div" sx={{ fontWeight: 600, lineHeight: 1.2, color: 'white' }}>
              Börse
            </Typography>
            <Typography variant="caption" sx={{ opacity: 0.9, color: 'white' }}>
              {sentiment.label}
            </Typography>
          </Box>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {lastUpdate && (
            <Typography variant="caption" sx={{ opacity: 0.9, color: 'white' }}>
              {formatTime(lastUpdate)}
            </Typography>
          )}
          <IconButton size="small" onClick={fetchStocks} disabled={loading} sx={{ color: 'white' }}>
            <Refresh />
          </IconButton>
        </Box>
      </CardMedia>
      <CardContent>

        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {loading && stocks.length === 0 ? (
          <Box>
            {[1, 2, 3].map((i) => (
              <Box key={i} sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Skeleton width={80} height={24} />
                  <Skeleton width={60} height={24} />
                </Box>
                <Skeleton width={120} height={20} />
                {i < 3 && <Divider sx={{ mt: 1 }} />}
              </Box>
            ))}
          </Box>
        ) : (
          <Box>
            {stocks.map((stock, index) => (
              <Box key={stock.symbol}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1 }}>
                  <Box>
                    <Typography variant="subtitle2" component="div" sx={{ fontWeight: 600 }}>
                      {stock.name}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {stock.symbol}
                    </Typography>
                  </Box>
                  <Box sx={{ textAlign: 'right' }}>
                    <Typography variant="body1" component="div" sx={{ fontWeight: 500 }}>
                      {stock.symbol === 'BTC'
                        ? formatNumber(stock.value, 2)
                        : formatNumber(stock.value, 0)}
                    </Typography>
                    <Box
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 0.5,
                        color: stock.change >= 0 ? 'success.main' : 'error.main'
                      }}
                    >
                      {stock.change >= 0 ? (
                        <TrendingUp sx={{ fontSize: 16 }} />
                      ) : (
                        <TrendingDown sx={{ fontSize: 16 }} />
                      )}
                      <Typography variant="caption" component="span" sx={{ fontWeight: 500 }}>
                        {stock.change >= 0 ? '+' : ''}{formatNumber(stock.changePercent, 2)}%
                      </Typography>
                    </Box>
                  </Box>
                </Box>
                {index < stocks.length - 1 && <Divider />}
              </Box>
            ))}

            <Box sx={{ mt: 2, pt: 1, borderTop: '1px dashed', borderColor: 'divider' }}>
              <Typography variant="caption" color="text.secondary" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <ShowChart sx={{ fontSize: 14 }} />
                Yahoo Finance • Aktualisiert alle {refreshIntervalSeconds}s
              </Typography>
            </Box>
          </Box>
        )}
      </CardContent>
    </Card>
  )
}
