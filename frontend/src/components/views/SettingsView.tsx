import * as React from 'react'
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Alert,
  Divider,
  FormControlLabel,
  Switch,
  Autocomplete,
  TextField,
} from '@mui/material'
import {
  CloudDownload as CloudDownloadIcon,
  Storage as StorageIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
  LocationOn as LocationOnIcon,
  ShowChart as ShowChartIcon,
} from '@mui/icons-material'
import { loadMarketConfig, saveMarketConfig, type MarketConfig } from '../widgets/MarketWidget'

const CITIES = [
  { name: 'Berlin', lat: 52.52, lon: 13.41 },
  { name: 'München', lat: 48.14, lon: 11.58 },
  { name: 'Hamburg', lat: 53.55, lon: 9.99 },
  { name: 'Köln', lat: 50.94, lon: 6.96 },
  { name: 'Frankfurt', lat: 50.11, lon: 8.68 },
  { name: 'Stuttgart', lat: 48.78, lon: 9.18 },
  { name: 'Düsseldorf', lat: 51.23, lon: 6.78 },
  { name: 'Leipzig', lat: 51.34, lon: 12.37 },
  { name: 'Dresden', lat: 51.05, lon: 13.74 },
  { name: 'Nürnberg', lat: 49.45, lon: 11.08 },
  { name: 'Kiel', lat: 54.32, lon: 10.13 },
  { name: 'Bremen', lat: 53.08, lon: 8.80 },
  { name: 'Mannheim', lat: 49.48, lon: 8.46 },
  { name: 'Freiburg', lat: 47.99, lon: 7.85 },
  { name: 'Bonn', lat: 50.73, lon: 7.10 },
]

export interface CityOption {
  label: string
  lat: number
  lon: number
}

function loadSavedCity(): CityOption {
  const raw = localStorage.getItem('weather-location-config')
  if (raw) {
    try {
      const parsed = JSON.parse(raw)
      if (parsed.name && typeof parsed.lat === 'number') {
        return { label: parsed.name, lat: parsed.lat, lon: parsed.lon }
      }
    } catch {}
  }
  const berlin = CITIES[0]
  return { label: berlin.name, lat: berlin.lat, lon: berlin.lon }
}

function saveCity(city: CityOption) {
  localStorage.setItem('weather-location-config', JSON.stringify({
    name: city.label,
    lat: city.lat,
    lon: city.lon,
  }))
  window.dispatchEvent(new CustomEvent('weather-location-changed'))
}

interface SettingsViewProps {
  articlesWithoutContent: number
  onOpenExtractionDialog: () => void
  isDark?: boolean
  onToggleTheme?: () => void
}

export function SettingsView({
  articlesWithoutContent,
  onOpenExtractionDialog,
  isDark,
  onToggleTheme,
}: SettingsViewProps) {
  const [selectedCity, setSelectedCity] = React.useState(loadSavedCity)
  const [marketConfig, setMarketConfig] = React.useState<MarketConfig>(loadMarketConfig)

  const cityOptions: CityOption[] = CITIES.map(c => ({ label: c.name, lat: c.lat, lon: c.lon }))

  const STOCK_OPTIONS = [
    { label: 'DAX', value: 'DAX' },
    { label: 'S&P 500', value: 'S&P500' },
    { label: 'NASDAQ', value: 'NASDAQ' },
  ]

  const CRYPTO_OPTIONS = [
    { label: 'Bitcoin', value: 'bitcoin' },
    { label: 'Ethereum', value: 'ethereum' },
    { label: 'Solana', value: 'solana' },
    { label: 'XRP', value: 'ripple' },
  ]

  return (
    <Box sx={{ maxWidth: 800, mx: 'auto', py: 4 }}>
      <Typography variant="h4" sx={{ mb: 4, fontWeight: 600 }}>
        Einstellungen
      </Typography>

      {/* Appearance/Theme Card */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            {isDark ? <DarkModeIcon color="primary" /> : <LightModeIcon color="primary" />}
            <Typography variant="h6">Erscheinungsbild</Typography>
          </Box>

          <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
            Wähle zwischen hellem und dunklem Design für die App.
          </Typography>

          <FormControlLabel
            control={
              <Switch
                checked={isDark}
                onChange={onToggleTheme}
                color="primary"
              />
            }
            label={isDark ? 'Dunkles Design' : 'Helles Design'}
          />
        </CardContent>
      </Card>

      {/* Weather Location Card */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <LocationOnIcon color="primary" />
            <Typography variant="h6">Wetter-Standort</Typography>
          </Box>

          <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
            Wähle deinen Standort für das Wetter-Widget.
          </Typography>

          <Autocomplete
            options={cityOptions}
            getOptionLabel={(o) => o.label}
            value={selectedCity}
            onChange={(_, newVal) => {
              if (newVal) {
                setSelectedCity(newVal)
                saveCity(newVal)
              }
            }}
            renderInput={(params) => (
              <TextField
                {...params}
                label="Stadt"
                placeholder="Stadt suchen..."
                size="small"
              />
            )}
            sx={{ maxWidth: 400 }}
          />
        </CardContent>
      </Card>

      {/* Market Symbols Card */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <ShowChartIcon color="primary" />
            <Typography variant="h6">Markt-Symbole</Typography>
          </Box>

          <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
            Wähle die Indizes und Kryptowährungen für das Markt-Widget.
          </Typography>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
            {/* Stock symbols */}
            <Box>
              <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
                Börsen-Indizes
              </Typography>
              <Autocomplete
                multiple
                options={STOCK_OPTIONS}
                getOptionLabel={(opt) => opt.label}
                value={STOCK_OPTIONS.filter(o => marketConfig.stockSymbols.includes(o.value))}
                onChange={(_, newVals) => {
                  const cfg: MarketConfig = {
                    ...marketConfig,
                    stockSymbols: newVals.map((v: typeof STOCK_OPTIONS[0]) => v.value),
                  }
                  setMarketConfig(cfg)
                  saveMarketConfig(cfg)
                }}
                renderInput={(params) => <TextField {...params} label="Indizes" size="small" />}
                sx={{ maxWidth: 500 }}
              />
            </Box>

            {/* Crypto IDs */}
            <Box>
              <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
                Krypto-Assets
              </Typography>
              <Autocomplete
                multiple
                options={CRYPTO_OPTIONS}
                getOptionLabel={(opt) => opt.label}
                value={CRYPTO_OPTIONS.filter(o => marketConfig.cryptoIds.includes(o.value))}
                onChange={(_, newVals) => {
                  const cfg: MarketConfig = {
                    ...marketConfig,
                    cryptoIds: newVals.map((v: typeof CRYPTO_OPTIONS[0]) => v.value),
                  }
                  setMarketConfig(cfg)
                  saveMarketConfig(cfg)
                }}
                renderInput={(params) => <TextField {...params} label="Kryptos" size="small" />}
                sx={{ maxWidth: 500 }}
              />
            </Box>
          </Box>
        </CardContent>
      </Card>

      {/* Content Extraction Card */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
            <StorageIcon color="primary" />
            <Typography variant="h6">Content-Extraktion</Typography>
          </Box>

          <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
            Extrahiere den vollständigen Artikelinhalt für bestehende Artikel.
            Dies ermöglicht die Anzeige im Reader-Modus ohne externe Links.
          </Typography>

          <Alert severity="info" sx={{ mb: 3 }}>
            Es gibt aktuell{' '}
            <strong>{articlesWithoutContent} Artikel</strong> ohne extrahierten
            Content.
          </Alert>

          <Button
            variant="contained"
            startIcon={<CloudDownloadIcon />}
            onClick={onOpenExtractionDialog}
            disabled={articlesWithoutContent === 0}
          >
            Content für bestehende Artikel extrahieren
          </Button>

          {articlesWithoutContent === 0 && (
            <Typography
              variant="caption"
              color="success.main"
              sx={{ display: 'block', mt: 1 }}
            >
              ✓ Alle Artikel haben bereits extrahierten Content
            </Typography>
          )}
        </CardContent>
      </Card>

      <Divider sx={{ my: 3 }} />

      <Typography variant="body2" color="text.secondary">
        Weitere Einstellungen folgen in zukünftigen Versionen.
      </Typography>
    </Box>
  )
}
