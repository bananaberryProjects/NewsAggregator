import { useEffect, useState } from 'react'
import { Card, CardContent, Typography, Box, Skeleton, Alert, IconButton, Divider } from '@mui/material'
import { TrendingUp, TrendingDown, Refresh, ShowChart } from '@mui/icons-material'

interface StockIndex {
  symbol: string
  name: string
  value: number
  change: number
  changePercent: number
}

interface StockWidgetProps {
  refreshInterval?: number // in minutes
}

// Mock data generator (since free stock APIs with CORS are limited)
// In a real app, you would use a proper API like Alpha Vantage, Finnhub, or Yahoo Finance
const generateMockData = (): StockIndex[] => {
  const baseValues: Record<string, number> = {
    'DAX': 18500,
    'S&P500': 5500,
    'BTC': 65000
  }

  return [
    {
      symbol: 'DAX',
      name: 'DAX',
      value: baseValues['DAX'] + (Math.random() - 0.5) * 200,
      change: (Math.random() - 0.5) * 150,
      changePercent: (Math.random() - 0.5) * 1.5
    },
    {
      symbol: 'S&P500',
      name: 'S&P 500',
      value: baseValues['S&P500'] + (Math.random() - 0.5) * 100,
      change: (Math.random() - 0.5) * 50,
      changePercent: (Math.random() - 0.5) * 1.2
    },
    {
      symbol: 'BTC',
      name: 'Bitcoin',
      value: baseValues['BTC'] + (Math.random() - 0.5) * 3000,
      change: (Math.random() - 0.5) * 2000,
      changePercent: (Math.random() - 0.5) * 5
    }
  ]
}

export function StockWidget({ refreshInterval = 5 }: StockWidgetProps) {
  const [stocks, setStocks] = useState<StockIndex[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null)

  const fetchStocks = async () => {
    setLoading(true)
    setError(null)

    try {
      // Simulating API delay
      await new Promise(resolve => setTimeout(resolve, 500))

      // In a real implementation, you would fetch from an actual API:
      // const response = await fetch('https://api.example.com/stocks')
      // const data = await response.json()

      const data = generateMockData()
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
    }, refreshInterval * 60 * 1000)

    return () => clearInterval(interval)
  }, [refreshInterval])

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

  return (
    <Card sx={{ height: '100%', minHeight: 200 }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6" component="div" sx={{ fontWeight: 600 }}>
            Börse
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            {lastUpdate && (
              <Typography variant="caption" color="text.secondary">
                {formatTime(lastUpdate)}
              </Typography>
            )}
            <IconButton size="small" onClick={fetchStocks} disabled={loading}>
              <Refresh />
            </IconButton>
          </Box>
        </Box>

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
                      {formatNumber(stock.value, stock.symbol === 'BTC' ? 0 : 0)}
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
                Demo-Daten (aktualisiert alle {refreshInterval} Min.)
              </Typography>
            </Box>
          </Box>
        )}
      </CardContent>
    </Card>
  )
}
