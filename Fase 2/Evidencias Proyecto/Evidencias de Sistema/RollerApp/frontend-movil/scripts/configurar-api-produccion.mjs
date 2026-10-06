import { writeFileSync } from 'node:fs';
const raw = process.env.ROLLERAPP_API_URL;
if (!raw) {
  throw new Error('Definir ROLLERAPP_API_URL=https://tu-dominio/api antes de compilar la app móvil.');
}
const url = new URL(raw);
if (url.username || url.password || url.search || url.hash) throw new Error('La URL de la API no debe contener credenciales, parámetros ni fragmentos.');
if (url.protocol !== 'https:' && !['localhost', '127.0.0.1'].includes(url.hostname)) {
  throw new Error('La API móvil debe usar HTTPS fuera de localhost.');
}
if (!url.pathname.endsWith('/api')) {
  throw new Error('ROLLERAPP_API_URL debe terminar en /api.');
}
writeFileSync(new URL('../src/environments/environment.prod.ts', import.meta.url),
  `export const environment = { production: true, apiUrl: ${JSON.stringify(url.toString().replace(/\/$/, ''))} };\n`);
