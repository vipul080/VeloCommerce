# VeloCommerce

A RESTful e-commerce backend built with **Java 21 and Spring Boot**. The project focuses on real backend concepts such as JWT authentication, role-based security, JPA entity relationships, cart and order workflows, transactional stock management, optimistic locking, Redis caching, asynchronous processing, custom exception handling, Swagger/OpenAPI documentation, and unit testing.

## Features

- User registration and login
- JWT-based authentication
- Role-based users (`CUSTOMER`, `ADMIN`)
- Product CRUD operations
- Product validation
- Product pagination, filtering, and sorting
- Shopping cart management
- Add/update/remove cart items
- Order placement from cart
- Automatic stock reduction after successful orders
- Stock validation before placing an order
- Order history for the authenticated user
- Order cancellation
- Automatic stock restoration when an order is cancelled
- Transaction management with `@Transactional`
- Optimistic locking with JPA `@Version`
- Redis caching for product lookups
- Cache invalidation when products are updated or deleted
- Asynchronous order confirmation notifications using `@Async`
- Custom business exceptions
- Centralized exception handling
- DTO-based API responses
- Unit testing with JUnit 5 and Mockito
- PostgreSQL database persistence
- Environment-based configuration for database/JWT secrets
- Interactive API documentation with Swagger/OpenAPI
- JWT Bearer authentication support in Swagger UI

## Tech Stack

- **Java 21**
- **Spring Boot**
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Redis
- Hibernate / JPA
- Maven
- Lombok
- JUnit 5
- Mockito
- Swagger / OpenAPI
- Docker
- Postman

## Project Structure

```text
src/main/java/com/vipul/ecommerce
├── config
│   ├── SecurityConfig.java
│   ├── RedisConfig.java
│   └── OpenApiConfig.java
├── controller
│   ├── UserController.java
│   ├── ProductController.java
│   ├── CartController.java
│   └── OrderController.java
├── dto
│   ├── RegisterRequest.java
│   ├── UserResponse.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   ├── CartItemRequest.java
│   ├── CartItemResponse.java
│   ├── CartResponse.java
│   ├── OrderItemResponse.java
│   └── OrderResponse.java
├── entity
│   ├── User.java
│   ├── Role.java
│   ├── Product.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── OrderStatus.java
├── exception
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── ProductNotFoundException.java
│   ├── UserNotFoundException.java
│   ├── CartNotFoundException.java
│   ├── CartItemNotFoundException.java
│   ├── OrderNotFoundException.java
│   ├── UnauthorizedOrderException.java
│   ├── OrderCancellationException.java
│   ├── CartEmptyException.java
│   └── InsufficientStockException.java
├── repository
│   ├── UserRepository.java
│   ├── ProductRepository.java
│   ├── CartRepository.java
│   ├── CartItemRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
├── security
│   └── JwtAuthenticationFilter.java
├── service
│   ├── UserService.java
│   ├── JwtService.java
│   ├── ProductService.java
│   ├── CartService.java
│   ├── OrderService.java
│   └── OrderNotificationService.java
└── EcommerceBackendApplication.java
````

## Database Design

The application uses PostgreSQL with the following main tables:

```text
users
  │
  └── carts
       │
       └── cart_items ─── products

users
  │
  └── orders
       │
       └── order_items ─── products
```

### Main Relationships

* A user has one cart.
* A cart contains multiple cart items.
* Each cart item references one product.
* A user can have multiple orders.
* An order contains multiple order items.
* Each order item references a product.
* `OrderItem.price` stores the product price at the time of purchase.

Using a purchase-time price snapshot means later changes to a product's price do not alter the historical price of an existing order.

## Authentication & Security

The API uses Spring Security with JWT authentication.

The authentication flow is:

```text
Register
   ↓
Password encoded with BCrypt
   ↓
Login
   ↓
JWT generated
   ↓
Client sends JWT with requests
   ↓
JwtAuthenticationFilter validates token
   ↓
Authenticated User becomes the security principal
```

Protected endpoints require:

```text
Authorization: Bearer <JWT>
```

The application is configured as stateless, and CSRF is disabled because the API uses JWT-based authentication rather than server-side sessions.

Role-based authorization is used to restrict protected operations according to the authenticated user's role.

Swagger UI and OpenAPI documentation endpoints are publicly accessible so that the API documentation can be loaded without authentication. Protected API operations still require the appropriate authentication and authorization.

## API Endpoints

### User

| Method | Endpoint              | Description           |
| ------ | --------------------- | --------------------- |
| POST   | `/api/users/register` | Register a new user   |
| POST   | `/api/users/login`    | Login and receive JWT |

### Products

| Method | Endpoint             | Description                                          |
| ------ | -------------------- | ---------------------------------------------------- |
| POST   | `/api/products`      | Create a product                                     |
| GET    | `/api/products`      | Get products with pagination, filtering, and sorting |
| GET    | `/api/products/{id}` | Get a product                                        |
| PUT    | `/api/products/{id}` | Update a product                                     |
| DELETE | `/api/products/{id}` | Delete a product                                     |

### Cart

| Method | Endpoint                      | Description                |
| ------ | ----------------------------- | -------------------------- |
| GET    | `/api/cart`                   | Get current user's cart    |
| POST   | `/api/cart/items`             | Add a product to cart      |
| PUT    | `/api/cart/items/{productId}` | Update cart item quantity  |
| DELETE | `/api/cart/items/{productId}` | Remove a product from cart |

### Orders

| Method | Endpoint                | Description                  |
| ------ | ----------------------- | ---------------------------- |
| POST   | `/api/orders`           | Place an order from the cart |
| GET    | `/api/orders`           | Get current user's orders    |
| DELETE | `/api/orders/{orderId}` | Cancel an order              |

The cancellation endpoint performs a **logical cancellation**. The order remains in the database with status `CANCELLED`; it is not physically deleted.

## API Documentation

The API is documented using **Swagger / OpenAPI**.

After starting the application, Swagger UI is available at:

```text
http://localhost:8081/swagger-ui/index.html
```

Swagger UI provides interactive documentation for the available REST endpoints and allows API requests to be tested directly from the browser.

JWT Bearer authentication is configured in Swagger UI through the **Authorize** button.

The authentication flow is:

```text
Login through the API
        ↓
Receive JWT
        ↓
Click "Authorize" in Swagger UI
        ↓
Enter Bearer JWT
        ↓
Swagger sends the token with protected requests
        ↓
Spring Security validates the JWT
```

The OpenAPI configuration defines the API information and the JWT Bearer security scheme.

## Product Pagination, Filtering & Sorting

The product listing endpoint supports pagination and query-based filtering/sorting.

This allows the API to return a limited portion of the product catalog instead of loading every product into memory at once.

Typical concepts supported by the endpoint include:

* Page number
* Page size
* Category filtering
* Sorting

This provides a more realistic product catalog API than a simple `findAll()` operation.

## Order Processing

Placing an order follows this workflow:

```text
Get authenticated user
        ↓
Find user's cart
        ↓
Check cart is not empty
        ↓
Check stock for every item
        ↓
Reduce product stock
        ↓
Calculate total amount
        ↓
Create Order
        ↓
Create OrderItems
        ↓
Clear cart
        ↓
Return OrderResponse
```

The operation is wrapped in `@Transactional`.

This is important because placing an order consists of several database changes. If an exception occurs during the operation, the transaction can roll back the changes rather than leaving the database in a partially updated state.

### Stock Handling

Before placing an order:

```text
requested quantity <= available stock
```

must be true for every product.

After a successful order:

```text
stock = stock - ordered quantity
```

When an order is cancelled:

```text
stock = stock + ordered quantity
```

The order placement transaction was manually tested by forcing a failure during the workflow and verifying through PostgreSQL that the related database changes were rolled back.

## Optimistic Locking

Products use JPA's `@Version` field to provide optimistic locking.

When a product is updated, Hibernate includes the current version in the SQL `UPDATE` condition:

```text
UPDATE product
SET ...
WHERE id = ? AND version = ?
```

After a successful update, the version is automatically incremented.

This helps prevent stale updates when multiple operations attempt to modify the same product concurrently.

## Order Status

Orders use the following enum:

```text
PENDING
CONFIRMED
SHIPPED
DELIVERED
CANCELLED
```

Currently, a `CONFIRMED` order can be cancelled. Orders in other states are rejected by the cancellation business rules.

## Redis Caching

Redis is used as a cache for individual product lookups.

* `@Cacheable` caches product responses by product ID.
* Cached entries have a 10-minute TTL.
* `@CacheEvict` removes cached product data when a product is updated or deleted.
* PostgreSQL remains the primary source of truth.

The cache reduces repeated PostgreSQL queries for frequently requested product details.

The application uses Docker to run Redis locally.

Example:

```bash
docker run --name ecommerce-redis -p 6379:6379 -d redis
```

## Asynchronous Order Notifications

Order confirmation notifications are processed using Spring's `@Async`.

After an order is successfully created, the notification task is executed asynchronously so the order workflow does not need to wait for the notification processing to finish.

The asynchronous notification logic is handled by a dedicated `OrderNotificationService`.

The current implementation demonstrates asynchronous processing for order confirmation notifications.

## Exception Handling

The application uses custom exceptions instead of relying on generic `RuntimeException` for business cases.

Examples:

* `UserNotFoundException`
* `ProductNotFoundException`
* `CartNotFoundException`
* `CartItemNotFoundException`
* `OrderNotFoundException`
* `UnauthorizedOrderException`
* `OrderCancellationException`
* `CartEmptyException`
* `InsufficientStockException`

`GlobalExceptionHandler` converts these exceptions into consistent HTTP responses.

Examples:

```text
Resource not found
→ 404 Not Found

Unauthorized order cancellation
→ 403 Forbidden

Invalid order operation / empty cart / insufficient stock
→ 400 Bad Request
```

## Testing

The project includes unit tests using:

* JUnit 5
* Mockito
* Spring Boot Test

Service-layer tests cover scenarios including:

* Successful product operations
* Product not found
* Successful order placement
* User/cart not found
* Empty cart
* Insufficient stock
* Retrieving order history
* Multiple orders
* Cart clearing after successful order
* Order response containing order items
* Successful order cancellation
* Order not found during cancellation
* Unauthorized cancellation
* Cancellation of an order in an invalid status
* Asynchronous order notification invocation

The order placement transaction was also manually verified by forcing a failure and confirming through PostgreSQL that the related database changes were rolled back.

## Configuration

Sensitive configuration is kept outside the repository using a `.env` file.

Example:

```env
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_secret
JWT_EXPIRATION=604800000
```

The `.env` file should remain in `.gitignore` and should never be committed to GitHub.

The application imports the environment configuration through:

```yaml
spring:
  config:
    import: optional:file:.env[.properties]
```

Redis is configured through:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

The application uses a 10-minute TTL for cached product entries.

## Running the Project

### 1. Create the database

Create a PostgreSQL database named:

```text
ecommerce_db
```

### 2. Start Redis

Redis can be started locally using Docker:

```bash
docker run --name ecommerce-redis -p 6379:6379 -d redis
```

If the container already exists, it can be started with:

```bash
docker start ecommerce-redis
```

### 3. Configure environment variables

Create a `.env` file in the project root:

```env
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_secret
JWT_EXPIRATION=604800000
```

### 4. Run the application

Using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
.\mvnw.cmd spring-boot:run
```

Or run `EcommerceBackendApplication` from IntelliJ IDEA.

The application runs on:

```text
http://localhost:8081
```

Swagger UI is available at:

```text
http://localhost:8081/swagger-ui/index.html
```

Hibernate is configured with:

```yaml
ddl-auto: update
```

so the database schema is updated automatically during development.

## Example Order Flow

A typical customer workflow is:

```text
Register
   ↓
Login
   ↓
Receive JWT
   ↓
Browse products
   ↓
Add product to cart
   ↓
View cart
   ↓
Place order
   ↓
Stock decreases
   ↓
Order becomes CONFIRMED
   ↓
Order confirmation notification is processed asynchronously
   ↓
View order history
   ↓
Cancel order if allowed
   ↓
Order becomes CANCELLED
   ↓
Stock is restored
```

## Learning Goals

This project was built to practice backend development concepts beyond basic CRUD:

* REST API design
* Layered architecture
* Dependency injection
* DTOs
* JPA relationships
* Hibernate
* PostgreSQL
* Spring Security
* JWT authentication
* Role-based authorization
* Transactions
* Database consistency
* Optimistic locking
* Redis caching
* Cache invalidation
* Asynchronous processing
* Business-rule validation
* Custom exception handling
* Unit testing with Mockito
* Swagger / OpenAPI
* Git/GitHub workflow
* Docker-based local infrastructure

## Future Improvements

Potential future additions include:

* Admin-specific product and order management
* More comprehensive controller/API integration tests
* Docker support for the complete application stack
* Payment integration
* Product search improvements
* Production-grade monitoring and logging