import { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Grid,
  Paper,
  CircularProgress,
  Alert,
  useTheme
} from '@mui/material';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Area,
  AreaChart
} from 'recharts';
import {
  Article as ArticleIcon,
  CheckCircle as CheckCircleIcon,
  Favorite as FavoriteIcon,
  TrendingUp as TrendingUpIcon
} from '@mui/icons-material';
import { statisticsApi, ReadingStatistics } from '../../api/client';

const COLORS = ['#4CAF50', '#FF9800', '#F44336', '#2196F3', '#9C27B0'];

export default function StatisticsView() {
  const [stats, setStats] = useState<ReadingStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    try {
      setLoading(true);
      const data = await statisticsApi.getStatistics();
      setStats(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Statistiken konnten nicht geladen werden');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Alert severity="error" sx={{ m: 2 }}>
        {error}
      </Alert>
    );
  }

  if (!stats) {
    return (
      <Alert severity="info" sx={{ m: 2 }}>
        Keine Statistiken verfügbar.
      </Alert>
    );
  }

  // Daten für Pie Chart
  const statusData = [
    { name: 'Gelesen', value: stats.readArticles, color: '#4CAF50' },
    { name: 'Ungelesen', value: stats.unreadArticles, color: '#FF9800' },
  ];

  return (
    <Box sx={{ p: 2 }}>
      <Typography variant="h4" gutterBottom>
        📊 Lesestatistiken
      </Typography>

      {/* Übersichtskarten */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <ArticleIcon sx={{ fontSize: 40, color: 'primary.main', mr: 2 }} />
                <Box>
                  <Typography variant="h4">{stats.totalArticles}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Gesamtartikel
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <CheckCircleIcon sx={{ fontSize: 40, color: 'success.main', mr: 2 }} />
                <Box>
                  <Typography variant="h4">{stats.readArticles}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Gelesen
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <FavoriteIcon sx={{ fontSize: 40, color: 'error.main', mr: 2 }} />
                <Box>
                  <Typography variant="h4">{stats.favoriteArticles}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Favoriten
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <TrendingUpIcon sx={{ fontSize: 40, color: 'info.main', mr: 2 }} />
                <Box>
                  <Typography variant="h4">{stats.readPercentage.toFixed(1)}%</Typography>
                  <Typography variant="body2" color="text.secondary">
                    Lesequote
                  </Typography>
                </Box>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Charts */}
      <Grid container spacing={3}>
        {/* Artikel pro Feed - Bar Chart */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Artikel pro Feed (Top 10)
            </Typography>
            <ResponsiveContainer width="100%" height="85%">
              <BarChart data={stats.articlesPerFeed} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis type="number" />
                <YAxis dataKey="feedName" type="category" width={120} />
                <Tooltip />
                <Legend />
                <Bar dataKey="totalArticles" name="Gesamt" fill="#2196F3" />
                <Bar dataKey="readArticles" name="Gelesen" fill="#4CAF50" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Read/Unread Verteilung - Pie Chart */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Gelesen vs Ungelesen
            </Typography>
            <ResponsiveContainer width="100%" height="85%">
              <PieChart>
                <Pie
                  data={statusData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  paddingAngle={5}
                  dataKey="value"
                  label
                >
                  {statusData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        {/* Artikel pro Tag - Area Chart */}
        <Grid size={{ xs: 12 }}>
          <Paper sx={{ p: 2, height: 400 }}>
            <Typography variant="h6" gutterBottom>
              Artikel pro Tag (letzte 30 Tage)
            </Typography>
            <ResponsiveContainer width="100%" height="85%">
              <AreaChart data={stats.articlesPerDay}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="date" 
                  tickFormatter={(value) => new Date(value).toLocaleDateString('de-DE', { day: 'numeric', month: 'short' })}
                />
                <YAxis />
                <Tooltip 
                  labelFormatter={(value) => new Date(value).toLocaleDateString('de-DE')}
                />
                <Legend />
                <Area 
                  type="monotone" 
                  dataKey="articleCount" 
                  name="Neue Artikel" 
                  stroke="#2196F3" 
                  fill="#2196F3" 
                  fillOpacity={0.6}
                />
                <Area 
                  type="monotone" 
                  dataKey="readCount" 
                  name="Gelesen" 
                  stroke="#4CAF50" 
                  fill="#4CAF50" 
                  fillOpacity={0.6}
                />
              </AreaChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
