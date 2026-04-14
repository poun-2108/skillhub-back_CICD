# ─── Backend Laravel — PHP 8.2 + ext-mongodb ──────────────────────────────────
FROM php:8.2-cli

WORKDIR /app

# ── Dépendances système ────────────────────────────────────────────────────────
RUN apt-get update && apt-get install -y \
        git \
        curl \
        unzip \
        libzip-dev \
        libssl-dev \
        pkg-config \
    && rm -rf /var/lib/apt/lists/*

# ── Extensions PHP standard ────────────────────────────────────────────────────
RUN docker-php-ext-install pdo pdo_mysql zip

# ── Extension MongoDB via PECL ─────────────────────────────────────────────────
# Installe la version 1.21+ requise par mongodb/laravel-mongodb 5.7
RUN pecl install mongodb \
    && docker-php-ext-enable mongodb

# ── Xdebug pour coverage tests ──────────────────────────────────────────────────
RUN pecl install xdebug \
    && docker-php-ext-enable xdebug

# ── Composer ──────────────────────────────────────────────────────────────────
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer

# ── Code source ───────────────────────────────────────────────────────────────
COPY . .

# ── Dépendances PHP ───────────────────────────────────────────────────────────
RUN composer install --no-interaction --prefer-dist --optimize-autoloader

# ── Permissions storage/cache ─────────────────────────────────────────────────
RUN chmod -R 775 storage bootstrap/cache \
    && chown -R www-data:www-data storage bootstrap/cache

EXPOSE 8001

CMD ["php", "artisan", "serve", "--host=0.0.0.0", "--port=8001"]
