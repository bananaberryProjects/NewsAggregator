import { Skeleton, Card, CardContent, Box } from '@mui/material'

export function ArticleSkeleton() {
  return (
    <Card
      sx={{
        width: '100%',
        height: 430,
        display: 'flex',
        flexDirection: 'column',
      }}
    >
      <Skeleton variant="rectangular" height={200} width="100%" />
      <CardContent sx={{ flexGrow: 1 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
          <Skeleton variant="rounded" width={80} height={24} />
          <Skeleton variant="text" width={90} />
        </Box>
        <Skeleton variant="text" width="100%" height={28} sx={{ mb: 1 }} />
        <Skeleton variant="text" width="85%" height={28} sx={{ mb: 1 }} />
        <Skeleton variant="text" width="60%" height={28} sx={{ mb: 2 }} />
        <Skeleton variant="text" width="100%" />
        <Skeleton variant="text" width="95%" />
        <Skeleton variant="text" width="70%" />
      </CardContent>
    </Card>
  )
}

export function ArticleSkeletonGrid({ count = 6 }: { count?: number }) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <ArticleSkeleton key={i} />
      ))}
    </>
  )
}
