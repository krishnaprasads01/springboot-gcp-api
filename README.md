# Spring Boot GCP API

A sample Spring Boot REST API with PostgreSQL database, designed for deployment on Google Cloud Platform using Cloud Run and Cloud SQL.

## Features

- ✅ RESTful API with CRUD operations for Task management
- ✅ PostgreSQL database with Cloud SQL
- ✅ Containerized application with Docker
- ✅ Infrastructure as Code with Terraform
- ✅ CI/CD with GitHub Actions
- ✅ Health checks and monitoring
- ✅ Security scanning with Trivy
- ✅ Environment-specific configurations

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Root endpoint with API information |
| GET | `/health` | Health check endpoint |
| GET | `/api/tasks` | Get all tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| POST | `/api/tasks` | Create a new task |
| PUT | `/api/tasks/{id}` | Update an existing task |
| DELETE | `/api/tasks/{id}` | Delete a task |
| GET | `/api/tasks/status/{status}` | Get tasks by status |
| GET | `/api/tasks/search?keyword={keyword}` | Search tasks |

## Task Status Values

- `PENDING`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`

## Local Development

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker (optional)

### Running Locally

1. Clone the repository:
```bash
git clone <repository-url>
cd springboot-gcp-api
```

2. Run the application:
```bash
./mvnw spring-boot:run
```

3. Access the application:
- API: http://localhost:8080
- Health Check: http://localhost:8080/health
- H2 Console: http://localhost:8080/h2-console

### Running with Docker

1. Build the Docker image:
```bash
docker build -t springboot-gcp-api .
```

2. Run the container:
```bash
docker run -p 8080:8080 springboot-gcp-api
```

## GCP Deployment

### Prerequisites

- GCP Account with billing enabled
- Google Cloud SDK installed
- Terraform installed
- Service Account with appropriate permissions

### Required GCP APIs

The following APIs will be automatically enabled by Terraform:
- Cloud Resource Manager API
- Cloud Build API
- Cloud Run API
- Cloud SQL Admin API
- VPC Access API
- Service Networking API
- IAM API
- Logging API
- Monitoring API

### Service Account Permissions

Create a service account with the following roles:
- Cloud Run Admin
- Cloud SQL Admin
- Compute Network Admin
- Service Account Admin
- Storage Admin
- Security Admin (for Secret Manager)

### Deployment Steps

1. **Set up GCP credentials:**
```bash
# Authenticate with gcloud
gcloud auth login
gcloud config set project YOUR_PROJECT_ID

# Create and download service account key
gcloud iam service-accounts create github-actions-sa
gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
    --member="serviceAccount:github-actions-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
    --role="roles/editor"
gcloud iam service-accounts keys create key.json \
    --iam-account=github-actions-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com
```

2. **Configure Terraform variables:**
```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with your project details
```

3. **Deploy infrastructure:**
```bash
terraform init
terraform plan
terraform apply
```

4. **Build and push Docker image:**
```bash
# Configure Docker for GCR
gcloud auth configure-docker gcr.io

# Build and push
docker build -t gcr.io/YOUR_PROJECT_ID/springboot-gcp-api:latest .
docker push gcr.io/YOUR_PROJECT_ID/springboot-gcp-api:latest
```

## GitHub Actions CI/CD

### Required Secrets

Add the following secrets to your GitHub repository:

| Secret Name | Description |
|-------------|-------------|
| `GCP_PROJECT_ID` | Your GCP Project ID |
| `GCP_SA_KEY` | Service Account JSON key (base64 encoded) |

### Workflow Features

- ✅ Automated testing on every push/PR
- ✅ Security scanning with Trivy
- ✅ Docker image building and pushing to GCR
- ✅ Infrastructure deployment with Terraform
- ✅ Deployment testing
- ✅ PR comments with deployment status

## Infrastructure Components

The Terraform configuration creates:

### Networking
- VPC Network with private subnets
- VPC Access Connector for Cloud Run
- Private IP allocation for Cloud SQL

### Database
- Cloud SQL PostgreSQL instance
- Database and user creation
- Automatic backups enabled

### Compute
- Cloud Run service with auto-scaling
- Service account with minimal permissions
- Secret Manager for database credentials

### Security
- Private network connectivity
- IAM roles and policies
- Secret management

## Monitoring and Logging

- **Health Endpoint**: `/health` - Application health status
- **Actuator Endpoints**: Metrics, info, and monitoring data
- **Cloud Logging**: Centralized log management
- **Cloud Monitoring**: Performance metrics and alerts

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `local` |
| `GCP_PROJECT_ID` | GCP Project ID | - |
| `GCP_DATABASE_NAME` | Database name | - |
| `GCP_INSTANCE_CONNECTION_NAME` | Cloud SQL connection name | - |
| `GCP_DATABASE_USERNAME` | Database username | - |
| `GCP_DATABASE_PASSWORD` | Database password | - |
| `PORT` | Application port | `8080` |

### Profiles

- **local**: Uses H2 in-memory database
- **gcp**: Uses Cloud SQL PostgreSQL

## API Usage Examples

### Create a Task
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive README and API documentation",
    "status": "PENDING"
  }'
```

### Get All Tasks
```bash
curl http://localhost:8080/api/tasks
```

### Update Task Status
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Complete project documentation",
    "description": "Write comprehensive README and API documentation",
    "status": "COMPLETED"
  }'
```

### Search Tasks
```bash
curl "http://localhost:8080/api/tasks/search?keyword=documentation"
```

## Cost Optimization

- Cloud Run scales to zero when not in use
- Cloud SQL uses micro instance for development
- VPC and networking components use minimal resources
- Automatic scaling based on traffic

## Security Features

- Private network connectivity
- Service account with minimal permissions
- Secret Manager for sensitive data
- Container vulnerability scanning
- HTTPS enforced by Cloud Run

## Troubleshooting

### Common Issues

1. **Database Connection Issues:**
   - Verify Cloud SQL instance is running
   - Check VPC connector configuration
   - Ensure service account has Cloud SQL Client role

2. **Build Failures:**
   - Check Java version compatibility
   - Verify Maven dependencies
   - Review GitHub Actions logs

3. **Deployment Issues:**
   - Verify Terraform state
   - Check GCP API enablement
   - Review IAM permissions

### Useful Commands

```bash
# Check Cloud Run logs
gcloud logging read "resource.type=cloud_run_revision" --limit=50

# Connect to Cloud SQL instance
gcloud sql connect INSTANCE_NAME --user=USERNAME

# Debug Terraform
terraform plan -detailed-exitcode
terraform show

# Test local connectivity
curl -f http://localhost:8080/health
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
