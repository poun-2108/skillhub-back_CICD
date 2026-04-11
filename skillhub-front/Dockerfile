# ─── Frontend React — Node 22 LTS ─────────────────────────────────────────────
# Node 22 requis par vite@8, react-router@7, @vitejs/plugin-react@6
FROM node:22-alpine

WORKDIR /app

# ── Dépendances ───────────────────────────────────────────────────────────────
COPY package*.json ./
RUN npm install

# ── Code source ───────────────────────────────────────────────────────────────
COPY . .

# ── Build production ──────────────────────────────────────────────────────────
RUN npm run build

EXPOSE 5173

# Dev : npm run dev | Prod : utiliser un serveur statique (nginx)
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]