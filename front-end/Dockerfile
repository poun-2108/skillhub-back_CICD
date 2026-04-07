# Image Node
FROM node:18

# Dossier de travail
WORKDIR /app

# Copier package.json
COPY package*.json ./

# Installer dépendances
RUN npm install

# Copier le reste du projet
COPY . .

# Lancer le projet React
CMD ["npm", "run", "dev"]