# VeloCommerce

A RESTful e-commerce backend built with **Java 21 and Spring Boot**. The project focuses on real backend concepts such as JWT authentication, role-based security, JPA entity relationships, cart and order workflows, stock management, transactions, custom exception handling, and unit testing.

## Features

- User registration and login
- JWT-based authentication
- Role-based users (`CUSTOMER`, `ADMIN`)
- Product CRUD operations
- Product validation
- Shopping cart management
- Add/update/remove cart items
- Order placement from cart
- Automatic stock reduction after successful orders
- Stock validation before placing an order
- Order history for the authenticated user
- Order cancellation
- Automatic stock restoration when an order is cancelled
- Transaction management with `@Transactional`
- Custom business exceptions
- Centralized exception handling
- DTO-based API responses
- Unit testing with JUnit 5 and Mockito
- PostgreSQL database persistence
- Environment-based configuration for database/JWT secrets

## Tech Stack

- **Java 21**
- **Spring Boot**
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Hibernate / JPA
- Maven
- Lombok
- JUnit 5
- Mockito
- Postman

## Project Structure

```text
src/main/java/com/vipul/ecommerce
├── config
│   └── SecurityConfig.java
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
│   └── OrderService.java
└── EcommerceBackendApplication.java
```

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

- A user has one cart.
- A cart contains multiple cart items.
- Each cart item references one product.
- A user can have multiple orders.
- An order contains multiple order items.
- Each order item references a product.
- `OrderItem.price` stores the product price at the time of purchase.

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

## API Endpoints

### User

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users/register` | Register a new user |
| POST | `/api/users/login` | Login and receive JWT |

### Products

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/products` | Create a product |
| GET | `/api/products` | Get all products |
| GET | `/api/products/{id}` | Get a product |
| PUT | `/api/products/{id}` | Update a product |
| DELETE | `/api/products/{id}` | Delete a product |

### Cart

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/cart` | Get current user's cart |
| POST | `/api/cart/items` | Add a product to cart |
| PUT | `/api/cart/items/{productId}` | Update cart item quantity |
| DELETE | `/api/cart/items/{productId}` | Remove a product from cart |

### Orders

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Place an order from the cart |
| GET | `/api/orders` | Get current user's orders |
| DELETE | `/api/orders/{orderId}` | Cancel an order |

The cancellation endpoint performs a **logical cancellation**. The order remains in the database with status `CANCELLED`; it is not physically deleted.

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

## Exception Handling

The application uses custom exceptions instead of relying on generic `RuntimeException` for business cases.

Examples:

- `UserNotFoundException`
- `ProductNotFoundException`
- `CartNotFoundException`
- `CartItemNotFoundException`
- `OrderNotFoundException`
- `UnauthorizedOrderException`
- `OrderCancellationException`
- `CartEmptyException`
- `InsufficientStockException`

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

- JUnit 5
- Mockito
- Spring Boot Test

Service-layer tests cover scenarios including:

- Successful product operations
- Product not found
- Successful order placement
- User/cart not found
- Empty cart
- Insufficient stock
- Retrieving order history
- Multiple orders
- Cart clearing after successful order
- Order response containing order items
- Successful order cancellation
- Order not found during cancellation
- Unauthorized cancellation
- Cancellation of an order in an invalid status

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

## Running the Project

### 1. Create the database

Create a PostgreSQL database named:

```text
ecommerce_db
```

### 2. Configure environment variables

Create a `.env` file in the project root:

```env
DB_USERNAME=postgres
DB_PASSWORD=your_password
JWT_SECRET=your_secret
JWT_EXPIRATION=604800000
```

### 3. Run the application

Using Maven:

```bash
mvn spring-boot:run
```

Or run `EcommerceBackendApplication` from IntelliJ IDEA.

The application runs on:

```text
http://localhost:8081
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

- REST API design
- Layered architecture
- Dependency injection
- DTOs
- JPA relationships
- Hibernate
- PostgreSQL
- Spring Security
- JWT authentication
- Role-based authorization
- Transactions
- Database consistency
- Business-rule validation
- Custom exception handling
- Unit testing with Mockito
- Git/GitHub workflow

## Future Improvements

Potential future additions include:

- Pagination and filtering
- Admin-specific product/order management
- More comprehensive controller/API tests
- Asynchronous order confirmation
- Email notifications
- Redis caching
- Improved concurrency handling for stock
- Docker support
- API documentation with Swagger/OpenAPI
- Payment integration
