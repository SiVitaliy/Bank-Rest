# Bank Rest

Backend-приложение для управления банковскими картами, пользователями, заявками и переводами между картами.

Проект реализован на Spring Boot с JWT-аутентификацией, ролевой авторизацией, PostgreSQL, Liquibase и OpenAPI/Swagger.

## Возможности

### Пользователь

- регистрация и вход по JWT;
- просмотр собственных карт;
- фильтрация, сортировка и пагинация карт;
- просмотр отдельной карты;
- пополнение и списание средств;
- перевод между собственными картами;
- просмотр истории транзакций;
- создание заявок на выпуск, блокировку, активацию или удаление карты.

### Администратор

- просмотр пользователей;
- поиск, блокировка и разблокировка пользователей;
- просмотр всех карт;
- создание карт пользователям;
- изменение срока действия карты;
- просмотр и обработка заявок;
- просмотр транзакций пользователей.

## Технологии

- Java 17;
- Spring Boot 4;
- Spring Web MVC;
- Spring Data JPA;
- Spring Security;
- JWT;
- PostgreSQL 17;
- Liquibase;
- MapStruct;
- Lombok;
- OpenAPI / Swagger UI;
- Maven;
- Docker и Docker Compose.

## Требования

Для запуска через Docker нужны:

- Docker Desktop;
- Docker Compose.

Для локального запуска нужны:

- JDK 21;
- Maven 3.9+;
- PostgreSQL 17 или совместимая версия.

## Быстрый запуск через Docker

Из корня проекта выполните:

```bash
docker compose up --build
```


Проверить состояние контейнеров:

```bash
docker compose ps
```


Остановить контейнеры:

```bash
docker compose down
```

Остановить контейнеры и удалить данные PostgreSQL:

```bash
docker compose down -v
```


## Доступные адреса

После запуска приложение доступно по адресу:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Локальный запуск

Создайте базу данных PostgreSQL:

```sql
CREATE DATABASE "BANK_REST_DB";
```

По умолчанию приложение использует следующие параметры:

```text
URL: jdbc:postgresql://localhost:5432/BANK_REST_DB
Username: postgres
Password: postgres
```



Запуск через Maven:

```bash
mvn spring-boot:run
```

Или сборка и запуск JAR:

```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## Конфигурация

Основные настройки находятся в:

```text
src/main/resources/application.yml
```



## Миграции базы данных

Liquibase запускается автоматически при старте приложения.

Главный changelog:

```text
src/main/resources/db/changelog/db.changelog-master.yaml
```

Миграции выполняются в следующем порядке:

```text
001-create-users-table.yaml
002-insert-default-users.yaml
003-create-cards-table.yaml
004-create-card-request-table.yaml
005-create-transaction-table.yaml
```

Hibernate работает в режиме:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

Это означает, что структуру базы создаёт Liquibase, а Hibernate только проверяет соответствие схемы JPA-сущностям.


## Аутентификация

Регистрация пользователя:

```http
POST /api/auth/register
```

Пример тела запроса:

```json
{
  "username": "user123",
  "password": "password123"
}
```

Вход:

```http
POST /api/auth/login
```

Пример тела запроса:

```json
{
  "username": "user123",
  "password": "password123"
}
```

В ответ возвращается JWT-токен. Для защищённых запросов передавайте его в заголовке:

```http
Authorization: Bearer <token>
```

В Swagger UI токен можно указать через кнопку **Authorize**.

## Роли

В системе используются две роли:

- `USER` — пользовательские операции;
- `ADMIN` — административные операции.

Маршруты `/api/**` доступны пользователям с ролью `USER`, маршруты `/admin/**` — пользователям с ролью `ADMIN`.

При запуске миграций создаются 
демонстрационные пользователи 
`admin` и `user`.
Их пароли совпадают с именем. Все новые пользователи при регистрации получают роль
`USER`.  

## Основные API

### Аутентификация

| Метод | URL | Описание |
|---|---|---|
| `POST` | `/api/auth/register` | Регистрация пользователя |
| `POST` | `/api/auth/login` | Получение JWT |

### Карты пользователя

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/api/cards` | Получить карты текущего пользователя |
| `GET` | `/api/cards/{id}` | Получить карту по ID |
| `PATCH` | `/api/cards/{id}/balance` | Пополнить или уменьшить баланс |

### Заявки пользователя

| Метод | URL | Описание |
|---|---|---|
| `POST` | `/api/card-requests` | Создать заявку на действие с картой |

### Транзакции пользователя

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/api/transactions` | Получить транзакции текущего пользователя |
| `GET` | `/api/transactions/{id}` | Получить транзакцию по ID |
| `POST` | `/api/transactions` | Выполнить перевод между картами |

### Администрирование пользователей

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/admin/users` | Получить список пользователей |
| `GET` | `/admin/users/{id}` | Получить пользователя по ID |
| `PATCH` | `/admin/users/{userId}/lock` | Заблокировать пользователя |
| `PATCH` | `/admin/users/{userId}/unlock` | Разблокировать пользователя |

### Администрирование карт

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/admin/cards` | Получить все карты |
| `GET` | `/admin/cards/{id}` | Получить карту по ID |
| `POST` | `/admin/users/{userId}/cards` | Создать карту пользователю |
| `PATCH` | `/admin/cards/{id}/expiration-date` | Изменить срок действия карты |

### Администрирование заявок

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/admin/card-requests` | Получить заявки |
| `PATCH` | `/admin/card-requests/{id}` | Одобрить или отклонить заявку |

### Администрирование транзакций

| Метод | URL | Описание |
|---|---|---|
| `GET` | `/admin/users/{userId}/transactions` | Получить транзакции пользователя |
| `GET` | `/admin/transactions/{id}` | Получить транзакцию по ID |

Полные схемы запросов, фильтры и параметры пагинации доступны в Swagger UI.

## Пагинация и сортировка

Для списочных методов используются стандартные параметры Spring Data:

```text
page=0
size=5
sort=creationTime,asc
```

Максимальный размер страницы — `50`.

## Тестирование

Запуск тестов:

```bash
mvn test
```



## Структура проекта

```text
src/main/java/com/example/bankcards
├── config          # конфигурация Spring Security и OpenAPI
├── controller      # REST-контроллеры пользователя
├── controller/admin# административные REST-контроллеры
├── dto             # DTO запросов, ответов и фильтров
├── entity          # JPA-сущности
├── exception       # исключения и обработчики ошибок
├── mapper          # MapStruct-мапперы
├── repository      # Spring Data JPA-репозитории
├── security        # JWT-фильтр
├── service         # бизнес-логика
├── specification   # динамические фильтры JPA
└── util            # вспомогательные классы
```

