import { useState, useEffect, useCallback } from 'react'
import { dashboardApi, type DashboardStats } from '../api/client'

export function useDashboardStats(pollingIntervalMs: number = 30_000) {
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchStats = useCallback(async () => {
    try {
      setLoading(prev => prev) // keep existing loading state for refresh
      const data = await dashboardApi.getDashboardStats()
      setStats(data)
      setError(null)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Fehler beim Laden der Dashboard-Statistiken')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchStats()
    const interval = setInterval(fetchStats, pollingIntervalMs)
    return () => clearInterval(interval)
  }, [fetchStats, pollingIntervalMs])

  return { stats, loading, error, refresh: fetchStats }
}
