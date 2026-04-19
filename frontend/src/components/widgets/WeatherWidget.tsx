import { useEffect, useState } from 'react'
import { Card, CardContent, Typography, Box, Skeleton, Alert, IconButton, CardMedia, Divider } from '@mui/material'
import { Refresh, WbSunny, Cloud, CloudOff, Opacity, AcUnit, Thunderstorm } from '@mui/icons-material'

interface WeatherData {
  temperature: number
  weatherCode: number
  description: string
  todayMin: number
  todayMax: number
}

interface ForecastDay {
  day: string
  maxTemp: number
  minTemp: number
  weatherCode: number
}

interface WeatherTheme {
  gradient: string
  iconBg: string
}

interface WeatherWidgetProps {
  defaultLatitude?: number
  defaultLongitude?: number
  defaultCity?: string
}

interface City {
  name: string
  lat: number
  lon: number
}

// List of cities kept for potential fallback (not shown in UI)
const CITIES: City[] = [
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
  // Additional cities (unused in UI)
  { name: 'Kiel', lat: 54.32, lon: 10.13 },
  { name: 'Bremen', lat: 53.08, lon: 8.80 },
  { name: 'Mannheim', lat: 49.48, lon: 8.46 },
  { name: 'Freiburg', lat: 47.99, lon: 7.85 },
  { name: 'Bonn', lat: 50.73, lon: 7.10 }
]

// WMO Weather interpretation codes – https://open-meteo.com/en/docs
const getWeatherTheme = (code: number): WeatherTheme => {
  if (code === 0) {
    return { gradient: 'linear-gradient(135deg, #FFD54F 0%, #FFB300 50%, #FF8F00 100%)', iconBg: '#FFF8E1' }
  }
  if (code >= 1 && code <= 3) {
    return { gradient: 'linear-gradient(135deg, #ECEFF1 0%, #B0BEC5 50%, #78909C 100%)', iconBg: '#ECEFF1' }
  }
  if (code >= 45 && code <= 48) {
    return { gradient: 'linear-gradient(135deg, #CFD8DC 0%, #B0BEC5 100%)', iconBg: '#CFD8DC' }
  }
  if (code >= 51 && code <= 57) {
    return { gradient: 'linear-gradient(135deg, #90CAF9 0%, #42A5F5 100%)', iconBg: '#E3F2FD' }
  }
  if (code >= 61 && code <= 67) {
    return { gradient: 'linear-gradient(135deg, #42A5F5 0%, #1976D2 100%)', iconBg: '#BBDEFB' }
  }
  if (code >= 71 && code <= 77) {
    return { gradient: 'linear-gradient(135deg, #B3E5FC 0%, #4FC3F7 50%, #81D4FA 100%)', iconBg: '#E1F5FE' }
  }
  if (code >= 80 && code <= 82) {
    return { gradient: 'linear-gradient(135deg, #1976D2 0%, #0D47A1 100%)', iconBg: '#90CAF9' }
  }
  if (code >= 95) {
    return { gradient: 'linear-gradient(135deg, #5C6BC0 0%, #3949AB 50%, #1A237E 100%)', iconBg: '#C5CAE9' }
  }
  return { gradient: 'linear-gradient(135deg, #FFD54F 0%, #FFB300 100%)', iconBg: '#FFF8E1' }
}

const getMuiWeatherIcon = (code: number) => {
  if (code === 0) return WbSunny
  if (code >= 1 && code <= 3) return Cloud
  if (code >= 45 && code <= 48) return CloudOff
  if (code >= 51 && code <= 67) return Opacity
  if (code >= 71 && code <= 77) return AcUnit
  if (code >= 95) return Thunderstorm
  return Cloud
}

const WeatherIcon = ({ code, size = 64, sx }: { code: number; size?: number; sx?: object }) => {
  const IconComponent = getMuiWeatherIcon(code)
  return <IconComponent sx={{ fontSize: size, ...sx }} />
}

const getWeatherDescription = (code: number): string => {
  if (code === 0) return 'Klarer Himmel'
  if (code === 1) return 'Hauptsächlich klar'
  if (code === 2) return 'Teilweise bewölkt'
  if (code === 3) return 'Bedeckt'
  if (code >= 45 && code <= 48) return 'Nebelig'
  if (code >= 51 && code <= 55) return 'Nieselregen'
  if (code >= 61 && code <= 65) return 'Regen'
  if (code >= 71 && code <= 77) return 'Schneefall'
  if (code >= 80 && code <= 82) return 'Regenschauer'
  if (code >= 95) return 'Gewitter'
  return 'Unbekannt'
}

const getWeekdayAbbreviation = (dateStr: string): string => {
  const days = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa']
  const date = new Date(dateStr)
  return days[date.getDay()]
}

const STORAGE_KEY = 'weather-location'

export function WeatherWidget({
  defaultLatitude = 52.52,
  defaultLongitude = 13.41,
  defaultCity = 'Berlin'
}: WeatherWidgetProps) {
  const [weather, setWeather] = useState<WeatherData | null>(null)
  const [forecast, setForecast] = useState<ForecastDay[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Initialise with defaults; will be overwritten by geolocation or saved location
  const [selectedLocation, setSelectedLocation] = useState<City>({
    name: defaultCity,
    lat: defaultLatitude,
    lon: defaultLongitude
  })

  // Load saved location from localStorage on mount
  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      try {
        const parsed = JSON.parse(saved) as City
        setSelectedLocation(parsed)
      } catch {}
    }
  }, [])

  // Obtain user's location on mount (fallback to defaults if denied)
  useEffect(() => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          const { latitude, longitude } = pos.coords
          setSelectedLocation({ name: 'Mein Standort', lat: latitude, lon: longitude })
        },
        () => {
          // Permission denied – keep current (default or saved) location
        }
      )
    }
  }, [])

  // Persist location changes
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(selectedLocation))
  }, [selectedLocation])

  const fetchWeather = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch(
        `https://api.open-meteo.com/v1/forecast?latitude=${selectedLocation.lat}&longitude=${selectedLocation.lon}&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min,weather_code&forecast_days=3`
      )
      if (!response.ok) throw new Error('Wetterdaten konnten nicht geladen werden')
      const data = await response.json()
      setWeather({
        temperature: data.current.temperature_2m,
        weatherCode: data.current.weather_code,
        description: getWeatherDescription(data.current.weather_code),
        todayMin: Math.round(data.daily.temperature_2m_min[0]),
        todayMax: Math.round(data.daily.temperature_2m_max[0])
      })
      if (data.daily) {
        const forecastData: ForecastDay[] = []
        for (let i = 0; i < data.daily.time.length; i++) {
          forecastData.push({
            day: getWeekdayAbbreviation(data.daily.time[i]),
            maxTemp: Math.round(data.daily.temperature_2m_max[i]),
            minTemp: Math.round(data.daily.temperature_2m_min[i]),
            weatherCode: data.daily.weather_code[i]
          })
        }
        setForecast(forecastData)
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unbekannter Fehler')
    } finally {
      setLoading(false)
    }
  }

  // Refetch when location changes
  useEffect(() => {
    fetchWeather()
  }, [selectedLocation])

  const handleRefresh = () => {
    fetchWeather()
  }

  const theme = weather ? getWeatherTheme(weather.weatherCode) : getWeatherTheme(0)

  return (
    <Card sx={{ height: '100%', minHeight: 200, overflow: 'hidden' }}>
      {/* Header */}
      <CardMedia
        sx={{
          height: 60,
          background: theme.gradient,
          position: 'relative',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          px: 2
        }}
      >
        <Typography variant="h5" component="div" sx={{ fontWeight: 600, color: 'white', textShadow: '0 1px 3px rgba(0,0,0,0.3)' }}>
          Wetter
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <IconButton size="small" onClick={handleRefresh} disabled={loading} sx={{ color: 'white' }}>
            <Refresh />
          </IconButton>
        </Box>
      </CardMedia>

      <CardContent sx={{ pt: 2 }}>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        {loading ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Skeleton variant="rounded" width={64} height={64} sx={{ borderRadius: 1 }} />
            <Box>
              <Skeleton width={80} height={32} />
              <Skeleton width={120} height={20} />
            </Box>
          </Box>
        ) : weather ? (
          <Box>
            {/* Current weather */}
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>}
                  <Box
                    sx={{
                      width: 80,
                      height: 80,
                      borderRadius: '50%',
                      backgroundColor: theme.iconBg,
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                      flexShrink: 0
                    }}
                  >
                    <WeatherIcon code={weather.weatherCode} size={64} />
                  </Box>
                  <Box>
                    <Typography variant="h4" component="div" sx={{ fontWeight: 700, lineHeight: 1.2 }}>
                      {Math.round(weather.temperature)}°C
                    </Typography>
                    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start' }}>
                      <Typography variant="caption" color="text.secondary">
                        H: {weather.todayMax}°
                      </Typography>
                      <Typography variant="caption" color="text.secondary"
                      >
                        T: {weather.todayMin}°
                      </Typography>
                    </Box>
                  </Box>
                </Box>
              </Box>
              <Box sx={{ textAlign: 'right' }}>
                <Typography variant="h5" color="text.secondary" sx={{ fontWeight: 500 }}>
                  {selectedLocation.name}
                </Typography>
                <Typography variant="body2" sx={{ mt: 0.5 }}>
                  {weather.description}
                </Typography>
              </Box>
            </Box>
            {/* Forecast */}
            {forecast.length > 0 && (
              <>
                <Divider sx={{ my: 1.5 }} />
                <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                  3‑Tage Vorhersage
                </Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  {forecast.map((day, index) => (
                    <Box
                      key={index}
                      sx={{
                        flex: 1,
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        py: 1,
                        px: 0.5,
                        borderRadius: 2,
                        bgcolor: 'background.paper',
                        border: '1px solid',
                        borderColor: 'divider',
                        minWidth: 0
                      }}
                    >
                      <Typography variant="caption" sx={{ fontWeight: 600, mb: 0.5 }}>
                        {day.day}
                      </Typography>
                      <WeatherIcon code={day.weatherCode} size={32} />
                      <Box sx={{ display: 'flex', gap: 0.5, mt: 0.5, alignItems: 'center' }}>
                        <Typography variant="caption" sx={{ fontWeight: 600 }}>
                          {day.maxTemp}°
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {day.minTemp}°
                        </Typography>
                      </Box>
                    </Box>
                  ))}
                </Box>
              </>
            )}
          </Box>
        ) : null}
      </CardContent>
    </Card>
  )
}
