import { useState, useEffect, useRef } from 'react'
import { Box, InputBase, IconButton, Tooltip, CircularProgress } from '@mui/material'
import { Search as SearchIcon, Clear as ClearIcon } from '@mui/icons-material'
import { useSearch } from '../hooks/useSearch'

interface SearchFilters {
  categoryId?: string
  readFilter?: 'READ' | 'UNREAD'
  favoriteFilter?: 'FAVORITE' | 'NOT_FAVORITE'
}

interface SearchBarProps {
  onResults: (results: any[] | null) => void
  onActive: (active: boolean) => void
  onPageData?: (pageData: { totalElements?: number } | null) => void
  filters?: SearchFilters
}

export function SearchBar({ onResults, onActive, onPageData, filters = {} }: SearchBarProps) {
  const [inputValue, setInputValue] = useState('')
  const [debounced, setDebounced] = useState('')
  const inputRef = useRef<HTMLInputElement>(null)
  const { results, loading, search, reset, pageData } = useSearch()

  // Debounce input (300ms)
  useEffect(() => {
    const timer = setTimeout(() => setDebounced(inputValue), 300)
    return () => clearTimeout(timer)
  }, [inputValue])

  useEffect(() => {
    if (debounced) {
      search(debounced, filters)
    } else {
      reset()
      onResults(null)
      onPageData?.(null)
    }
  }, [debounced])

  useEffect(() => {
    onResults(results)
    onPageData?.(pageData)
    onActive(results !== null || inputValue !== '')
  }, [results, pageData, inputValue])

  const handleClear = () => {
    setInputValue('')
    setDebounced('')
    reset()
    onResults(null)
    inputRef.current?.focus()
  }

  return (
    <Box sx={{ px: 2, py: 1, width: '100%' }}>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          bgcolor: 'background.paper',
          borderRadius: 2,
          px: 1.5,
          py: 0.5,
          border: '1px solid',
          borderColor: 'divider',
        }}
      >
        {loading ? (
          <CircularProgress size={20} sx={{ mr: 1, color: 'text.secondary' }} />
        ) : (
          <SearchIcon sx={{ mr: 1, color: 'text.secondary', fontSize: 20 }} />
        )}
        <InputBase
          inputRef={inputRef}
          placeholder="Volltextsuche..."
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          sx={{ flex: 1, fontSize: '0.95rem' }}
        />
        {inputValue && (
          <Tooltip title="Löschen">
            <IconButton size="small" onClick={handleClear}>
              <ClearIcon fontSize="small" />
            </IconButton>
          </Tooltip>
        )}
      </Box>
    </Box>
  )
}
