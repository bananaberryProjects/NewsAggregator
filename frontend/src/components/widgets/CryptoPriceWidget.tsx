import { useEffect, useState } from 'react'
import { Card, CardContent, Typography, Box, Skeleton, Alert, IconButton, Divider, CardMedia } from '@mui/material'
import {
  DollarSign, TrendingUp, TrendingDown, RotateCw, Bitcoin
} from 'lucide-react'

interface CryptoPrice {
  coinId: string
  symbol: string
  name: string
  priceUsd: number
  priceEur: number
  priceChange24h: number
  priceChangePercentage24h: number
  marketCapUsd: number
  volume24hUsd: number
  lastUpdated: string
}

interface CryptoPriceApiResponse {
  prices: CryptoPrice[]
  updatedAt: string
}

const API_BASE_URL = '/api'

const formatNumber = (num: number | undefined | null, decimals: number = 2): string => {
  if (num === undefined || num === null) return '-'
  return new Intl.NumberFormat('de-DE', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals
  }).format(num)
}

const formatCompactNumber = (num: number | undefined | null): string => {
  if (num === undefined || num === null) return '-'
  return new Intl.NumberFormat('de-DE', {
    notation: 'compact',
    compactDisplay: 'short',
    maximumFractionDigits: 1
  }).format(num)
}

const fetchCryptoData = async (): Promise<CryptoPrice[]> => {
  const response = await fetch(`${API_BASE_URL}/crypto/prices`)
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`)
  }
  const data: CryptoPriceApiResponse = await response.json()
  return data.prices || []
}

export function CryptoPriceWidget({ refreshIntervalSeconds = 300 }: { refreshIntervalSeconds?: number }) {
  const [prices, setPrices] = useState<CryptoPrice[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null)

  const loadPrices = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchCryptoData()
      setPrices(data)
      setLastUpdate(new Date())
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Fehler beim Laden der Krypto-Preise')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPrices()
  }, [])

  useEffect(() => {
    const interval = setInterval(() => {
      loadPrices()
    }, refreshIntervalSeconds * 1000)
    return () => clearInterval(interval)
  }, [refreshIntervalSeconds])

  const formatTime = (date: Date): string => {
    return date.toLocaleTimeString('de-DE', { hour: '2-digit', minute: '2-digit' })
  }

  const totalMarketCap = prices.reduce((sum, p) => sum + (p.marketCapUsd || 0), 0)
  const totalVolume = prices.reduce((sum, p) => sum + (p.volume24hUsd || 0), 0)

  return (
    <Card sx={{ height: '100%', minHeight: 200 }}>
      <CardMedia
        sx={{
          height: 50,
          background: 'linear-gradient(135deg, #f7931a 0%, #ffab40 100%)',
          display: 'flex',
          alignItems: 'center',
          px: 2,
          justifyContent: 'space-between'
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, color: 'white' }}>
          <Bitcoin size={32} />
          <Box>
            <Typography variant="h6" component="div" sx={{ fontWeight: 500, lineHeight: 1.2, color: 'white' }}>
              Krypto
            </Typography>
          </Box>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {lastUpdate && (
            <Typography variant="caption" sx={{ opacity: 0.9, color: 'white' }}>
              {formatTime(lastUpdate)}
            </Typography>
          )}
          <IconButton size="small" onClick={loadPrices} disabled={loading} sx={{ color: 'white' }}>
            <RotateCw size={20} />
          </IconButton>
        </Box>
      </CardMedia>

      <CardContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

        {loading && prices.length === 0 ? (
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
        ) : prices.length === 0 ? (
          <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
            Keine Krypto-Daten verfügbar
          </Typography>
        ) : (
          <Box>
            {prices.map((crypto, index) => (
              <Box key={crypto.coinId}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                    <Box
                      sx={{
                        width: 32,
                        height: 32,
                        borderRadius: '50%',
                        background: 'linear-gradient(135deg, #f7931a 0%, #ffab40 100%)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: 12,
                        fontWeight: 'bold',
                        color: 'white'
                      }}
                    >
                      {crypto.symbol.charAt(0)}
                    </Box>
                    <Box>
                      <Typography variant="subtitle2" component="div" sx={{ fontWeight: 600 }}>
                        {crypto.name}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {crypto.symbol}
                      </Typography>
                    </Box>
                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                    <Typography variant="body1" component="div" sx={{ fontWeight: 500 }}>
                      {crypto.symbol === 'BTC' ? formatNumber(crypto.priceUsd, 0) : formatNumber(crypto.priceUsd)} $
                    </Typography>
                    <Box
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: 0.5,
                        mt: 0.5,
                        color: crypto.priceChangePercentage24h >= 0 ? 'success.main' : 'error.main'
                      }}
                    >
                      {crypto.priceChangePercentage24h >= 0 ? (
                        <TrendingUp size={16} />
                      ) : (
                        <TrendingDown size={16} />
                      )}
                      <Typography variant="caption" component="span" sx={{ fontWeight: 500 }}>
                        {crypto.priceChangePercentage24h >= 0 ? '+' : ''}{formatNumber(crypto.priceChangePercentage24h, 2)}%
                      </Typography>
                    </Box>
                  </Box>
                </Box>
                {index < prices.length - 1 && <Divider />}
              </Box>
            ))}

            <Box sx={{ mt: 2, pt: 1, borderTop: '1px dashed', borderColor: 'divider' }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                <Typography variant="caption" color="text.secondary">
                  <DollarSign size={12} style={{ verticalAlign: 'middle', marginRight: 4 }} />
                  Marktkap.
                </Typography>
                <Typography variant="caption" sx={{ fontWeight: 500 }}>
                  {formatCompactNumber(totalMarketCap)}
                </Typography>
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <Typography variant="caption" color="text.secondary">
                  Vol. 24h
                </Typography>
                <Typography variant="caption" sx={{ fontWeight: 500 }}>
                  {formatCompactNumber(totalVolume)}
                </Typography>
              </Box>
              <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.5 }}>
                CoinGecko &bull; Aktualisiert alle 5 Minuten
              </Typography>
            </Box>
          </Box>
        )}
      </CardContent>
    </Card>
  )
}
