self.addEventListener('install', (event) => {
  console.log('# Service worker is installed! 🎉')
  event.waitUntil(
    caches.open('static').then((cache) => {
      cache.addAll([
        '/',
        '/css/style.css',
        '/images/icons-512.png',
        '/images/icons-192.png',
        '/images/icons-152.png',
        '/images/icons-144.png',
        '/index.html',
        '/js/main/main.js',
      ])
    }),
  )
})

self.addEventListener('activate', () => {
  console.log('# Service worker is active! 🎉')
})

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((res) => {
      if (res) return res
      return fetch(event.request)
    }),
  )
})
