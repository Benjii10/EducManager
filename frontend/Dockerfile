# [Benjamin - #08, 2026-10-15] Image du frontend (construction en 2 etapes)
# Etape 1 : build Angular de production
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY . .
RUN npm run build

# Etape 2 : fichiers statiques servis par nginx, qui relaie aussi /api vers le backend
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
HEALTHCHECK --interval=15s --timeout=5s --retries=5 CMD wget -qO- http://localhost/ >/dev/null || exit 1
