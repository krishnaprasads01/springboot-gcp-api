# Firestore Deployment Configuration

## Overview

This document describes the updated Terraform configuration and Spring Boot setup for deploying the unified Task API to Google Cloud Platform with Firestore as the backend database.

## Key Changes Made

### 1. Terraform Configuration (`terraform/main.tf`)

- **Added Firestore API**: Enabled `firestore.googleapis.com` in required APIs
- **Provisioned Firestore Database**: Created native Firestore database with location `us-central1`
- **Service Account Permissions**: Added `roles/datastore.user` role for Cloud Run service account
- **Environment Variables**: Configured Cloud Run with proper Firestore environment variables:
  - `GCP_PROJECT_ID`: Project ID for Firestore connection
  - `GCP_FIRESTORE_ENABLED`: Enables Firestore in the application
  - `SPRING_PROFILES_ACTIVE`: Set to `gcp` for cloud deployment
  - `GOOGLE_CLOUD_PROJECT`: Additional project ID variable for Google Cloud SDK

### 2. Spring Boot Configuration (`application.yml`)

- **Profile-based Configuration**: 
  - `local`: Uses H2 in-memory database for development
  - `gcp`: Uses Firestore, disables JPA/DataSource auto-configuration
- **Firestore Integration**: Proper configuration for Google Cloud Firestore
- **Environment Variables**: Uses environment variables for flexible configuration

### 3. Infrastructure Resources

#### Firestore Database
- **Type**: `FIRESTORE_NATIVE`
- **Location**: `us-central1` (configurable via `firestore_location` variable)
- **Name**: `(default)` (the default Firestore database)

#### Service Account Permissions
- **Role**: `roles/datastore.user`
- **Purpose**: Allows Cloud Run service to read/write Firestore documents

#### Cloud Run Configuration
- **Memory**: Increased to 1Gi for better performance
- **Environment**: Configured for Firestore connection
- **Dependencies**: Waits for Firestore database and IAM permissions

## Deployment Instructions

### Prerequisites

1. **Google Cloud Project**: Ensure you have a GCP project with billing enabled
2. **APIs Enabled**: The Terraform will enable required APIs automatically
3. **Authentication**: Ensure `gcloud` is authenticated with appropriate permissions

### Step 1: Configure Variables

1. Copy `terraform.tfvars.example` to `terraform.tfvars`:
   ```bash
   cp terraform/terraform.tfvars.example terraform/terraform.tfvars
   ```

2. Update the variables:
   ```hcl
   project_id        = "your-actual-project-id"
   region            = "us-central1"
   firestore_location = "us-central1"  # Must be compatible with your region
   app_name          = "springboot-gcp-api"
   environment       = "prod"
   ```

### Step 2: Deploy Using Script

Run the deployment script:
```bash
./scripts/deploy.sh your-project-id prod
```

This script will:
1. Build the Spring Boot application
2. Create and push Docker image to GCR
3. Deploy infrastructure with Terraform
4. Test the deployment

### Step 3: Manual Terraform Deployment (Alternative)

If you prefer manual deployment:

```bash
# Build the application
./mvnw clean package -DskipTests

# Build and push Docker image
docker build -t gcr.io/your-project-id/springboot-gcp-api:latest .
gcloud auth configure-docker gcr.io
docker push gcr.io/your-project-id/springboot-gcp-api:latest

# Deploy infrastructure
cd terraform
terraform init
terraform plan -var="project_id=your-project-id"
terraform apply -var="project_id=your-project-id"
```

## Environment Variables

### Cloud Run Environment Variables (Set by Terraform)

- `GCP_PROJECT_ID`: Your Google Cloud project ID
- `GCP_FIRESTORE_ENABLED`: `true` (enables Firestore)
- `SPRING_PROFILES_ACTIVE`: `gcp` (uses GCP profile)
- `GOOGLE_CLOUD_PROJECT`: Your Google Cloud project ID

### Local Development Environment Variables

For local development with Firestore emulator:
```bash
export FIRESTORE_EMULATOR_HOST=localhost:8080
export GCP_PROJECT_ID=demo-project
export SPRING_PROFILES_ACTIVE=local
```

For local development with H2:
```bash
export SPRING_PROFILES_ACTIVE=local
```

## API Endpoints

After deployment, your API will be available at:
- **Base URL**: `https://[service-name]-[hash]-[region].a.run.app`
- **Health Check**: `GET /health`
- **List Tasks**: `GET /api/tasks`  
- **Create Task**: `POST /api/tasks`
- **Get Task**: `GET /api/tasks/{id}`
- **Update Task**: `PUT /api/tasks/{id}`
- **Delete Task**: `DELETE /api/tasks/{id}`

## Monitoring and Troubleshooting

### Check Logs
```bash
gcloud run services logs read [service-name] --region=us-central1
```

### Verify Firestore
```bash
gcloud firestore databases list
gcloud firestore databases describe --database="(default)"
```

### Common Issues

1. **Firestore Location Conflict**: Ensure `firestore_location` is compatible with your region
2. **Permissions**: Verify service account has `roles/datastore.user` permission
3. **Project ID**: Ensure environment variables match your actual project ID
4. **Database Already Exists**: If you get "Database already exists" error, this is normal - the Terraform configuration uses an existing database instead of creating a new one

### Firestore Database Management

**Important:** Google Cloud allows only one default Firestore database per project. The Terraform configuration is designed to:
- Reference an existing Firestore database if one exists
- If no database exists, create one manually first:
  ```bash
  gcloud firestore databases create --location=us-central1 --project=your-project-id
  ```
  
## Testing

### Local Testing
```bash
# Run with H2 database
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run

# Test endpoints
curl http://localhost:8080/health
curl http://localhost:8080/api/tasks
```

### Cloud Testing
```bash
# Get service URL from Terraform output
SERVICE_URL=$(cd terraform && terraform output -raw cloud_run_service_url)

# Test endpoints
curl $SERVICE_URL/health
curl $SERVICE_URL/api/tasks
```

## Cost Considerations

- **Firestore**: Pay-per-operation pricing
- **Cloud Run**: Pay-per-request with generous free tier
- **Container Registry**: Storage costs for Docker images

Monitor usage in Google Cloud Console billing section.

## Security

- Service account follows principle of least privilege
- No public database access (Firestore accessed via service account)
- HTTPS-only endpoints via Cloud Run
- Environment variables for sensitive configuration
