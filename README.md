# 🛒 E-Commerce Microservices Platform

[![Java](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-green)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue)](https://www.docker.com/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A production-ready microservices architecture built with Spring Boot and Spring Cloud, demonstrating modern distributed systems patterns.

## 📋 Overview

Complete microservices ecosystem featuring:
- **5 Microservices**: Auth, Product, Order, API Gateway, Config Server
- **JWT Authentication** with role-based authorization (ADMIN/CUSTOMER)
- **Real-time Inventory Management** with automatic stock updates
- **Service Discovery** using Netflix Eureka
- **Distributed Tracing** with Zipkin
- **Containerized Deployment** with Docker Compose

## 🏗️ Architecture
```
                    Client
                      ↓
              API Gateway (8080)
                ↙     ↓     ↘
        Auth    Product    Order
       (8083)   (8081)    (8082)
                      ↓
                   Eureka (8761)
                      ↓
                  MySQL (3311)
            ┌────────┼────────┐
        auth_db  product_db  order_db
```

## ✨ Features

- ✅ JWT Authentication & Authorization
- ✅ Role-Based Access Control (RBAC)
- ✅ Product CRUD Operations
- ✅ Real-time Inventory Management
- ✅ Order Lifecycle Management (8 states)
- ✅ Automatic Stock Deduction/Return
- ✅ Inter-Service Communication
- ✅ Service Discovery (Eureka)
- ✅ Distributed Tracing (Zipkin)
- ✅ Database Per Service Pattern
- ✅ API Gateway Pattern
- ✅ Saga Pattern (Compensating Transactions)

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.4.1, Spring Cloud 2024.0.0 |
| Security | Spring Security, JWT |
| Database | MySQL 8.0 |
| Service Discovery | Netflix Eureka |
| API Gateway | Spring Cloud Gateway |
| Tracing | Zipkin |
| Containerization | Docker, Docker Compose |

## 🚀 Quick Start

### Prerequisites
- Docker Desktop
- Java 17+
- 5GB free disk space

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/ecommerce-microservices.git
cd ecommerce-microservices
```

2. **Start all services**
```bash
docker-compose up -d
```

3. **Wait for startup** (~2-3 minutes)
```bash
docker-compose ps
```

4. **Access the services**
- API Gateway: http://localhost:8080
- Eureka Dashboard: http://localhost:8761
- Zipkin UI: http://localhost:9411

### Test the APIs
```bash
# 1. Register user
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123",
    "role": "CUSTOMER"
  }'

# 2. Login and get token
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'

# 3. Use token for authenticated requests
curl -X GET http://localhost:8080/products \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 📚 API Documentation

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token

### Products (🔒 Auth Required)
- `GET /products` - Get all products
- `GET /products/{id}` - Get product by ID
- `POST /products` - Create product (Admin only)
- `PUT /products/{id}` - Update product (Admin only)
- `DELETE /products/{id}` - Delete product (Admin only)

### Orders (🔒 Auth Required)
- `GET /orders` - Get orders (filtered by role)
- `POST /orders` - Create order
- `PUT /orders/{id}/cancel` - Cancel order
- `GET /orders/stats` - Get statistics (Admin only)

## 📁 Project Structure
```
ecommerce-microservices/
├── api-gateway/          # API Gateway (Port 8080)
├── auth-service/         # Authentication Service (Port 8083)
├── product-service/      # Product Management (Port 8081)
├── order-service/        # Order Management (Port 8082)
├── eureka-server/        # Service Discovery (Port 8761)
├── config-server/        # Configuration Server (Port 8888)
├── docker-compose.yml    # Docker orchestration
├── init-db.sql          # Database initialization
└── README.md            # This file
```

## 🎨 Design Patterns

- **API Gateway Pattern** - Single entry point
- **Service Registry Pattern** - Dynamic discovery
- **Database Per Service** - Service independence
- **Saga Pattern** - Distributed transactions
- **Externalized Configuration** - Centralized config

## 🔮 Future Enhancements

- [ ] Circuit Breaker (Resilience4j)
- [ ] Redis Caching
- [ ] Swagger API Documentation
- [ ] Prometheus + Grafana Monitoring
- [ ] RabbitMQ Message Queue
- [ ] Kubernetes Deployment

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is licensed under the MIT License - see [LICENSE](LICENSE) file.

## 👤 Author

**Your Name**
- GitHub: [@yourusername](https://github.com/Basava-Ra)
- LinkedIn: [Your Profile](https://linkedin.com/in/basavaraj-mang-4a1b4a271)

---

⭐️ If you found this helpful, please star the repository!
