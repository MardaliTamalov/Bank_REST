# JWT Аутентификация - Руководство по использованию

## Обзор
Проект теперь использует Spring Security с JWT токенами для аутентификации и авторизации пользователей.

## Роли пользователей
- **ADMIN** - Полный доступ ко всем операциям
- **USER** - Доступ к картам и транзакциям

## API Endpoints

### Аутентификация
- `POST /api/auth/login` - Вход в систему
- `POST /api/auth/register` - Регистрация нового пользователя
- `GET /api/auth/me` - Получение информации о текущем пользователе

### Защищенные endpoints
- `GET /api/users/**` - Только ADMIN
- `GET /api/cards/**` - ADMIN, USER
- `GET /api/transactions/**` - ADMIN, USER

## Использование API

### 1. Регистрация нового пользователя
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser@example.com",
    "password": "password123"
  }'
```

### 2. Вход в систему
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin@bank.com",
    "password": "Admin@12345"
  }'
```

Ответ будет содержать JWT токен:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin@bank.com",
  "role": "ADMIN"
}
```

### 3. Использование защищенных endpoints
```bash
curl -X GET http://localhost:8080/api/cards \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

## Тестовые пользователи

### Администратор
- **Username:** admin@bank.com
- **Password:** Admin@12345
- **Role:** ADMIN

### Обычный пользователь
- **Username:** user@bank.com
- **Password:** User@12345
- **Role:** USER

## Конфигурация JWT

JWT настройки находятся в `application.yml`:
```yaml
app:
  jwt:
    secret: your-secret-key-here-make-it-long-and-secure-for-production
    expiration: 86400000 # 24 часа в миллисекундах
```

## Безопасность

### Рекомендации для продакшена:
1. Измените JWT секрет на длинную случайную строку
2. Используйте HTTPS
3. Установите разумное время жизни токена
4. Реализуйте refresh токены для длительных сессий

### Пример генерации секретного ключа:
```bash
openssl rand -base64 64
```

## Swagger UI

Документация API доступна по адресу:
- http://localhost:8080/swagger-ui.html

## Обработка ошибок

### 401 Unauthorized
- Неверный токен
- Истекший токен
- Отсутствует токен

### 403 Forbidden
- Недостаточно прав для доступа к ресурсу

### 400 Bad Request
- Неверные данные аутентификации
- Пользователь уже существует при регистрации 