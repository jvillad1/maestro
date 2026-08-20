// Service worker de Maestro: hace la app instalable y le da una pantalla
// utilizable sin conexión. Estrategia red-primero para no dejar servida una
// versión vieja después de un deploy; la caché es solo el respaldo.
const CACHE = 'maestro-shell-v2';
// composeApp.js va en el precache porque su nombre es estable y en la primera
// visita el navegador ya lo pidió antes de que este worker tomara control; los
// .wasm tienen nombre con hash y se cachean al vuelo en la siguiente carga.
const SHELL = ['/', '/index.html', '/styles.css', '/composeApp.js', '/icon-192.png', '/icon-512.png'];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE).then((cache) => cache.addAll(SHELL)).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== CACHE).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const request = event.request;
  if (request.method !== 'GET') return;

  const url = new URL(request.url);
  if (url.origin !== self.location.origin) return;
  // Los datos siempre desde la red: la versión web no tiene base local
  if (url.pathname.startsWith('/api/')) return;

  event.respondWith(
    fetch(request)
      .then((response) => {
        const copy = response.clone();
        caches.open(CACHE).then((cache) => cache.put(request, copy)).catch(() => {});
        return response;
      })
      .catch(() => caches.match(request).then((hit) => {
        if (hit) return hit;
        // Solo las navegaciones caen al shell; devolver index.html en lugar de
        // un .js o .wasm faltante esconde el error real detrás de un HTML
        if (request.mode === 'navigate') return caches.match('/index.html');
        return Response.error();
      }))
  );
});
