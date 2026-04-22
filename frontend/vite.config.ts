import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['newsweave.png', 'apple-touch-icon.png', 'masked-icon.svg'],
      manifest: {
        name: 'NewsWeave',
        short_name: 'NewsWeave',
        description: 'Deine personalisierte News-Plattform - RSS-Feeds übersichtlich organisiert',
        theme_color: '#1976d2',
        background_color: '#ffffff',
        display: 'standalone',
        scope: '/',
        start_url: '/',
        orientation: 'portrait',
        icons: [
          {
            src: '/newsweave-192x192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: '/newsweave-512x512.png',
            sizes: '512x512',
            type: 'image/png'
          },
          {
            src: '/newsweave-512x512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any maskable'
          }
        ],
        categories: ['news', 'productivity'],
        lang: 'de',
        dir: 'ltr',
        shortcuts: [
          {
            name: 'Feeds',
            short_name: 'Feeds',
            description: 'Alle Feeds anzeigen',
            url: '/feeds',
            icons: [{ src: '/newsweave-96x96.png', sizes: '96x96' }]
          },
          {
            name: 'Favoriten',
            short_name: 'Favoriten',
            description: 'Gemerkte Artikel',
            url: '/favorites',
            icons: [{ src: '/newsweave-96x96.png', sizes: '96x96' }]
          }
        ],
        related_applications: [],
        prefer_related_applications: false
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,json,woff2}'],
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/fonts\.googleapis\.com\/.*/i,
            handler: 'CacheFirst',
            options: {
              cacheName: 'google-fonts-cache',
              expiration: {
                maxEntries: 10,
                maxAgeSeconds: 60 * 60 * 24 * 365 // 1 Jahr
              },
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          },
          {
            urlPattern: /\/api\/(articles|feeds|categories)/,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 60 * 24 // 24 Stunden
              },
              networkTimeoutSeconds: 10,
              cacheableResponse: {
                statuses: [0, 200]
              }
            }
          },
          {
            urlPattern: ({ request }) => request.mode === 'navigate',
            handler: 'NetworkFirst',
            options: {
              cacheName: 'pages-cache',
              expiration: {
                maxEntries: 50,
                maxAgeSeconds: 60 * 60 * 24 // 24 Stunden
              },
              networkTimeoutSeconds: 5
            }
          }
        ],
        navigateFallback: '/index.html',
        navigateFallbackDenylist: [/^\/api/, /favicon\.ico$/]
      },
      devOptions: {
        enabled: true,
        type: 'module'
      }
    })
  ],
  server: {
    host: '0.0.0.0',
    port: 5173,
    strictPort: true,
    allowedHosts: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
})
