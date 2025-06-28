# NoSQL Database Integration Guide

This guide explains how to use NoSQL databases with your Spring Boot Task API. The application is designed with a generic interface that supports multiple NoSQL backends.

## Supported NoSQL Databases

- ✅ **Firestore** (Google Cloud) - Fully implemented
- 🚧 **MongoDB** - Implementation template provided
- 🚧 **DynamoDB** (AWS) - Implementation template provided
- 🔄 **Others** - Easy to add new implementations

## Architecture

The application uses a **Strategy Pattern** with Spring profiles to switch between different NoSQL implementations:

```
TaskRepositoryNoSQL (Interface)
├── TaskRepositoryFirestoreImpl (@Profile("firestore"))
├── TaskRepositoryMongoImpl (@Profile("mongodb"))
└── TaskRepositoryDynamoDBImpl (@Profile("dynamodb"))
```

This design allows you to:
- Switch databases without changing business logic
- Add new database implementations easily
- Test with different databases
- Deploy different configurations for different environments

## Prerequisites

1. **Google Cloud Project**: You need a GCP project with Firestore enabled
2. **Authentication**: Service account key or Application Default Credentials (ADC)
3. **Dependencies**: Already included in `pom.xml`

## Configuration

### 1. Environment Variables

Set the following environment variables:

```bash
# Required for production
export GCP_PROJECT_ID=your-project-id

# Optional for local development
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
```

### 2. Application Profiles

The application supports multiple profiles:

#### Firestore Profile (Google Cloud)
```bash
java -jar app.jar --spring.profiles.active=firestore
```

#### MongoDB Profile
```bash
java -jar app.jar --spring.profiles.active=mongodb
```

#### DynamoDB Profile (AWS)
```bash
java -jar app.jar --spring.profiles.active=dynamodb
```

#### Local Development with H2 (Default)
```bash
java -jar app.jar --spring.profiles.active=local
```

## API Endpoints

### NoSQL endpoints (works with any NoSQL implementation):
- Base URL: `/api/nosql/tasks`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/nosql/tasks` | Get all tasks from NoSQL database |
| GET | `/api/nosql/tasks/{id}` | Get task by ID (String UUID) |
| POST | `/api/nosql/tasks` | Create a new task |
| PUT | `/api/nosql/tasks/{id}` | Update an existing task |
| DELETE | `/api/nosql/tasks/{id}` | Delete a task |
| GET | `/api/nosql/tasks/status/{status}` | Get tasks by status |
| GET | `/api/nosql/tasks/search?keyword={keyword}` | Search tasks |

The endpoints remain the same regardless of which NoSQL database you're using (Firestore, MongoDB, DynamoDB, etc.).

## Task Model Differences

### Firestore Task Model Features:
- **ID**: String (UUID) instead of Long
- **Additional Fields**: 
  - `dueDate`: LocalDateTime for task deadlines
  - `assignee`: String for task assignment
- **Automatic Timestamps**: createdAt and updatedAt managed automatically

### Sample Firestore Task JSON:
```json
{
  "title": "Complete project documentation",
  "description": "Write comprehensive API documentation",
  "status": "IN_PROGRESS",
  "dueDate": "2025-07-15T17:00:00",
  "assignee": "john.doe@example.com"
}
```

## Local Development

### Option 1: Use Firestore Emulator (Recommended)
```bash
# Install Firebase CLI
npm install -g firebase-tools

# Start Firestore emulator
firebase emulators:start --only=firestore

# Set environment variable
export FIRESTORE_EMULATOR_HOST=localhost:8080

# Run application
java -jar app.jar --spring.profiles.active=firestore
```

### Option 2: Use Real Firestore with Service Account
```bash
# Download service account key from GCP Console
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/service-account-key.json
export GCP_PROJECT_ID=your-project-id

# Run application
java -jar app.jar --spring.profiles.active=firestore
```

## Production Deployment

### Google Cloud Run
When deploying to Cloud Run, set these environment variables:
```bash
GCP_PROJECT_ID=your-project-id
SPRING_PROFILES_ACTIVE=firestore
```

Authentication is handled automatically via the service account attached to Cloud Run.

### Other Cloud Providers
Ensure you set up ADC or provide a service account key file.

## Adding New Database Implementations

To add support for a new NoSQL database (e.g., Cassandra, Redis, etc.):

### 1. Create Repository Implementation
```java
@Repository
@Profile("your-database")
public class TaskRepositoryYourDBImpl implements TaskRepositoryNoSQL {
    
    @Autowired
    private YourDatabaseTemplate template;
    
    @Override
    public List<TaskNoSQL> findAll() throws ExecutionException, InterruptedException {
        // Your implementation here
    }
    
    // Implement other methods...
}
```

### 2. Add Dependencies
Add the required database dependencies to `pom.xml`:
```xml
<dependency>
    <groupId>your.database</groupId>
    <artifactId>spring-boot-starter-your-db</artifactId>
</dependency>
```

### 3. Configure Profile
Add configuration to `application.yml`:
```yaml
---
spring:
  config:
    activate:
      on-profile: your-database
  your-database:
    connection-string: ${YOUR_DB_CONNECTION}
```

### 4. Test
```bash
java -jar app.jar --spring.profiles.active=your-database
```

That's it! No changes needed to controllers, services, or business logic.

## Migration from PostgreSQL to Firestore

1. **Export existing data** from PostgreSQL
2. **Transform data** to match Firestore model (change Long IDs to String UUIDs)
3. **Import data** into Firestore using the Firestore console or a migration script
4. **Switch profiles** to use `firestore` instead of default
5. **Update your frontend** to use String IDs instead of numeric IDs

## Performance Considerations

- **Queries**: Firestore has different querying capabilities than SQL
- **Indexing**: Complex queries may require composite indexes
- **Search**: Full-text search is limited; consider using Algolia for advanced search
- **Costs**: Pay per read/write operation rather than compute time

## Security

- Use **Firestore Security Rules** for production
- Implement **IAM roles** appropriately
- Consider **VPC Service Controls** for sensitive data

## Troubleshooting

### Common Issues:

1. **Project ID not set**: Ensure `GCP_PROJECT_ID` environment variable is set
2. **Authentication errors**: Check ADC setup or service account key
3. **Firestore not enabled**: Enable Firestore API in GCP Console
4. **Emulator connection**: Ensure `FIRESTORE_EMULATOR_HOST` is set correctly

### Debug Logging:
Add this to application.yml:
```yaml
logging:
  level:
    com.google.cloud.firestore: DEBUG
    com.example.api: DEBUG
```
