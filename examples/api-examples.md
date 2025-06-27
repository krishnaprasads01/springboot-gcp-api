# API Usage Examples

This file contains examples of how to use the Spring Boot GCP API.

## Base URL
- Local: `http://localhost:8080`
- Production: `https://your-service-url.run.app`

## 1. Health Check

```bash
curl -X GET "http://localhost:8080/health"
```

Response:
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00",
  "service": "SpringBoot GCP API",
  "version": "1.0.0"
}
```

## 2. Get All Tasks

```bash
curl -X GET "http://localhost:8080/api/tasks" \
  -H "Content-Type: application/json"
```

Response:
```json
[
  {
    "id": 1,
    "title": "Complete project setup",
    "description": "Set up the Spring Boot project with all dependencies",
    "status": "COMPLETED",
    "createdAt": "2024-01-15T09:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  },
  {
    "id": 2,
    "title": "Write documentation",
    "description": "Create comprehensive API documentation",
    "status": "IN_PROGRESS",
    "createdAt": "2024-01-15T09:30:00",
    "updatedAt": "2024-01-15T09:30:00"
  }
]
```

## 3. Get Task by ID

```bash
curl -X GET "http://localhost:8080/api/tasks/1" \
  -H "Content-Type: application/json"
```

Response:
```json
{
  "id": 1,
  "title": "Complete project setup",
  "description": "Set up the Spring Boot project with all dependencies",
  "status": "COMPLETED",
  "createdAt": "2024-01-15T09:00:00",
  "updatedAt": "2024-01-15T10:00:00"
}
```

## 4. Create New Task

```bash
curl -X POST "http://localhost:8080/api/tasks" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Deploy to production",
    "description": "Deploy the application to GCP Cloud Run",
    "status": "PENDING"
  }'
```

Response:
```json
{
  "id": 3,
  "title": "Deploy to production",
  "description": "Deploy the application to GCP Cloud Run",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## 5. Update Task

```bash
curl -X PUT "http://localhost:8080/api/tasks/3" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Deploy to production",
    "description": "Deploy the application to GCP Cloud Run with monitoring",
    "status": "IN_PROGRESS"
  }'
```

Response:
```json
{
  "id": 3,
  "title": "Deploy to production",
  "description": "Deploy the application to GCP Cloud Run with monitoring",
  "status": "IN_PROGRESS",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T11:00:00"
}
```

## 6. Delete Task

```bash
curl -X DELETE "http://localhost:8080/api/tasks/3"
```

Response: `204 No Content`

## 7. Get Tasks by Status

```bash
curl -X GET "http://localhost:8080/api/tasks/status/PENDING" \
  -H "Content-Type: application/json"
```

Response:
```json
[
  {
    "id": 4,
    "title": "Review code",
    "description": "Perform code review for the new feature",
    "status": "PENDING",
    "createdAt": "2024-01-15T11:00:00",
    "updatedAt": "2024-01-15T11:00:00"
  }
]
```

## 8. Search Tasks

```bash
curl -X GET "http://localhost:8080/api/tasks/search?keyword=documentation" \
  -H "Content-Type: application/json"
```

Response:
```json
[
  {
    "id": 2,
    "title": "Write documentation",
    "description": "Create comprehensive API documentation",
    "status": "IN_PROGRESS",
    "createdAt": "2024-01-15T09:30:00",
    "updatedAt": "2024-01-15T09:30:00"
  }
]
```

## Status Values

- `PENDING` - Task is created but not started
- `IN_PROGRESS` - Task is currently being worked on
- `COMPLETED` - Task is finished
- `CANCELLED` - Task was cancelled

## Error Responses

### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tasks"
}
```

### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 999",
  "path": "/api/tasks/999"
}
```

## Using with Postman

Import the following JSON to create a Postman collection:

```json
{
  "info": {
    "name": "Spring Boot GCP API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/health",
          "host": ["{{baseUrl}}"],
          "path": ["health"]
        }
      }
    },
    {
      "name": "Get All Tasks",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/api/tasks",
          "host": ["{{baseUrl}}"],
          "path": ["api", "tasks"]
        }
      }
    },
    {
      "name": "Create Task",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"title\": \"Sample Task\",\n  \"description\": \"This is a sample task\",\n  \"status\": \"PENDING\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/api/tasks",
          "host": ["{{baseUrl}}"],
          "path": ["api", "tasks"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    }
  ]
}
```
