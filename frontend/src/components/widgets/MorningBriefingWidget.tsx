import { useEffect, useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box, Card, CardContent, Typography, Grid, Paper, Skeleton, Button,
  Fade, Chip, Avatar
} from '@mui/material'
import {
  Coffee as CoffeeIcon,
  WbSunny as SunIcon,
  Brightness4 as EveningIcon,
  NightsStay as NightIcon,
  AutoStories as ArticleIcon,
  RssFeed,
  Star,
  LocalFireDepartment,
  ArrowForward,
} from '@mui/icons-material'
import { useDashboardStats } from '../../hooks'

interface GreetingConfig {
  text: string;
  Icon: typeof CoffeeIcon;
  gradient: string;
  iconBg: string;
}

function getGreeting(): GreetingConfig {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 12) {
    return { text: 'Guten Morgen', Icon: CoffeeIcon, gradient: 'linear-gradient(135deg, #E65100 0%, #FF8F00 50%, #FFB300 100%)', iconBg: 'rgba(255,255,255,0.2)' }
  }
  if (hour >= 12 && hour < 17) {
    return { text: 'Guten Tag', Icon: SunIcon, gradient: 'linear-gradient(135deg, #1565C0 0%, #42A5F5 50%, #64B5F6 100%)', iconBg: 'rgba(255,255,255,0.2)' }
  }
  if (hour >= 17 && hour < 22) {
    return { text: 'Guten Abend', Icon: EveningIcon, gradient: 'linear-gradient(135deg, #C62828 0%, #EF5350 50%, #FF7043 100%)', iconBg: 'rgba(255,255,255,0.2)' }
  }
  return { text: 'Gute Nacht', Icon: NightIcon, gradient: 'linear-gradient(135deg, #283593 0%, #5C6BC0 50%, #7986CB 100%)', iconBg: 'rgba(255,255,255,0.2)' }
}

function formatDate(): string {
  return new Date().toLocaleDateString('de-DE', { weekday: 'long', day: 'numeric', month: 'long' })
}

function useCountUp(target: number, duration: number = 1200): number {
  const [count, setCount] = useState(0)
  const frameRef = useRef<number>(0)

  useEffect(() => {
    const startTime = performance.now()
    const startCount = 0

    const animate = (currentTime: number) => {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)
      // ease-out cubic
      const eased = 1 - Math.pow(1 - progress, 3)
      const current = Math.round(startCount + (target - startCount) * eased)
      setCount(current)
      if (progress < 1) {
        frameRef.current = requestAnimationFrame(animate)
      }
    }
    frameRef.current = requestAnimationFrame(animate)
    return () => cancelAnimationFrame(frameRef.current)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [target, duration])

  return count
}

interface StatCardProps {
  label: string;
  value: number;
  icon: typeof ArticleIcon;
  color: string;
  suffix?: string;
}

function StatCard({ label, value, icon: Icon, color, suffix }: StatCardProps) {
  const animatedValue = useCountUp(value)
  const [hovered, setHovered] = useState(false)

  return (
    <Paper
      elevation={0}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      sx={{
        p: 2,
        minHeight: 120,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: 3,
        bgcolor: 'background.paper',
        border: 1,
        borderColor: 'divider',
        transition: 'transform 0.25s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.25s ease',
        transform: hovered ? 'translateY(-4px)' : 'translateY(0)',
        boxShadow: (theme) => hovered ? theme.shadows[6] : theme.shadows[0],
        position: 'relative',
        overflow: 'hidden',
        cursor: 'default',
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          top: 12,
          right: 12,
          width: 36,
          height: 36,
          borderRadius: '50%',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: color + '15',
          color: color,
          transition: 'transform 0.2s ease',
          transform: hovered ? 'scale(1.1)' : 'scale(1)',
        }}
      >
        <Icon sx={{ fontSize: 20 }} />
      </Box>
      <Typography
        variant="h4"
        sx={{
          fontWeight: 700,
          color: 'text.primary',
          mt: 1,
          lineHeight: 1.2,
          fontVariantNumeric: 'tabular-nums',
        }}
      >
        {animatedValue}
        {suffix && (
          <Typography component="span" variant="body2" color="text.secondary" sx={{ ml: 0.5, fontWeight: 500 }}>
            {suffix}
          </Typography>
        )}
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5, textAlign: 'center', fontWeight: 500 }}>
        {label}
      </Typography>
    </Paper>
  )
}

function StatCardSkeleton() {
  return (
    <Paper
      elevation={0}
      sx={{
        p: 2,
        minHeight: 120,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: 3,
        border: 1,
        borderColor: 'divider',
      }}
    >
      <Skeleton variant="circular" width={36} height={36} />
      <Skeleton variant="text" width={50} height={40} sx={{ mt: 1 }} />
      <Skeleton variant="text" width={80} height={16} />
    </Paper>
  )
}

export function MorningBriefingWidget() {
  const navigate = useNavigate()
  const { stats, loading } = useDashboardStats(30_000)
  const greeting = getGreeting()
  const GreetingIcon = greeting.Icon
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    const timer = setTimeout(() => setMounted(true), 50)
    return () => clearTimeout(timer)
  }, [])

  return (
    <Fade in={mounted} timeout={600}>
      <Card
        sx={{
          borderRadius: 3,
          overflow: 'visible',
          background: greeting.gradient,
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
                  bgcolor: greeting.iconBg,
                  color: '#fff',
                  backdropFilter: 'blur(4px)',
                }}
              >
                <GreetingIcon sx={{ fontSize: 28 }} />
              </Avatar>
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 600, lineHeight: 1.3, color: '#fff' }}>
                  {greeting.text}, Oliver
                </Typography>
                <Typography variant="body2" sx={{ opacity: 0.85, mt: 0.3 }}>
                  {formatDate()}
                </Typography>
              </Box>
            </Box>

            {/* Streak Badge */}
            {!loading && stats && stats.readStreakDays > 0 && stats.readStreakDays >= 3 && (
              <Chip
                icon={<LocalFireDepartment sx={{ color: '#FF6F00 !important' }} />}
                label={`${stats.readStreakDays} Tage Streak`}
                sx={{
                  bgcolor: 'rgba(255,255,255,0.2)',
                  color: '#fff',
                  fontWeight: 600,
                  backdropFilter: 'blur(4px)',
                  '& .MuiChip-label': { color: '#fff' },
                }}
              />
            )}
          </Box>

          {/* Tagline */}
          {!loading && stats && (
            <Typography variant="body1" sx={{ mb: 2.5, opacity: 0.92, fontWeight: 400 }}>
              {stats.unreadCount > 0
                ? `Du hast ${stats.unreadCount} ungelesene Artikel${stats.feedsWithNewArticles > 0 ? ` in ${stats.feedsWithNewArticles} Feeds` : ''}.`
                : 'Alles erledigt 🎉'}
              {stats.articlesReadToday > 0 && ` Heute schon ${stats.articlesReadToday} gelesen.`}
            </Typography>
          )}
          {loading && (
            <Box sx={{ mb: 2.5 }}>
              <Skeleton variant="text" width="60%" height={24} sx={{ bgcolor: 'rgba(255,255,255,0.2)' }} />
            </Box>
          )}

          {/* Stats Grid */}
          <Grid container spacing={2}>
            {loading ? (
              <>
                <Grid size={{ xs: 6, sm: 3 }}><StatCardSkeleton /></Grid>
                <Grid size={{ xs: 6, sm: 3 }}><StatCardSkeleton /></Grid>
                <Grid size={{ xs: 6, sm: 3 }}><StatCardSkeleton /></Grid>
                <Grid size={{ xs: 6, sm: 3 }}><StatCardSkeleton /></Grid>
              </>
            ) : stats ? (
              <>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <StatCard label="Ungelesen" value={stats.unreadCount} icon={ArticleIcon} color="#F57C00" />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <StatCard label="offene Feeds" value={stats.feedsWithNewArticles} icon={RssFeed} color="#388E3C" />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <StatCard label="Favoriten" value={stats.favoriteCount} icon={Star} color="#FBC02D" />
                </Grid>
                <Grid size={{ xs: 6, sm: 3 }}>
                  <StatCard label="Lesestreak" value={stats.readStreakDays} icon={LocalFireDepartment} color="#D32F2F" suffix=" Tage" />
                </Grid>
              </>
            ) : null}
          </Grid>

          {/* Quick Actions */}
          {!loading && stats && (
            <Box sx={{ display: 'flex', gap: 1.5, mt: 3, flexWrap: 'wrap' }}>
              <Button
                variant="contained"
                size="small"
                onClick={() => navigate('/articles', { state: { filter: 'unread' } })}
                sx={{
                  bgcolor: 'rgba(255,255,255,0.2)',
                  color: '#fff',
                  textTransform: 'none',
                  fontWeight: 500,
                  borderRadius: 2,
                  px: 2,
                  '&:hover': { bgcolor: 'rgba(255,255,255,0.35)' },
                }}
                endIcon={<ArrowForward />}
              >
                Artikel
              </Button>
              <Button
                variant="outlined"
                size="small"
                onClick={() => navigate('/favorites')}
                sx={{
                  borderColor: 'rgba(255,255,255,0.4)',
                  color: '#fff',
                  textTransform: 'none',
                  fontWeight: 500,
                  borderRadius: 2,
                  px: 2,
                  '&:hover': { borderColor: 'rgba(255,255,255,0.7)', bgcolor: 'rgba(255,255,255,0.08)' },
                }}
                endIcon={<ArrowForward />}
              >
                Favoriten
              </Button>
              <Button
                variant="outlined"
                size="small"
                onClick={() => navigate('/feeds')}
                sx={{
                  borderColor: 'rgba(255,255,255,0.4)',
                  color: '#fff',
                  textTransform: 'none',
                  fontWeight: 500,
                  borderRadius: 2,
                  px: 2,
                  '&:hover': { borderColor: 'rgba(255,255,255,0.7)', bgcolor: 'rgba(255,255,255,0.08)' },
                }}
                endIcon={<ArrowForward />}
              >
                Feeds
              </Button>
            </Box>
          )}
        </CardContent>
      </Card>
    </Fade>
  )
}
