#!/bin/bash

# Deploy Spring Boot API to GCP
# Usage: ./scripts/deploy.sh [project-id] [environment]

set -e

PROJECT_ID=${1:-"your-gcp-project-id"}
ENVIRONMENT=${2:-"dev"}
REGION="us-central1"
SERVICE_NAME="springboot-gcp-api"

echo "🚀 Deploying Spring Boot API to GCP..."
echo "Project ID: $PROJECT_ID"
echo "Environment: $ENVIRONMENT"
echo "Region: $REGION"

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ gcloud CLI is not installed. Please install it first."
    exit 1
fi

# Check if terraform is installed
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform is not installed. Please install it first."
    exit 1
fi

# Set the project
gcloud config set project $PROJECT_ID

echo "📦 Building application..."
./mvnw clean package -DskipTests

echo "🐳 Building Docker image..."
docker build -t gcr.io/$PROJECT_ID/$SERVICE_NAME:latest .

echo "📤 Pushing Docker image to GCR..."
gcloud auth configure-docker gcr.io --quiet
docker push gcr.io/$PROJECT_ID/$SERVICE_NAME:latest

echo "🏗️  Deploying infrastructure with Terraform..."
cd terraform

# Initialize Terraform if not already done
if [ ! -d ".terraform" ]; then
    terraform init
fi

# Plan the deployment
terraform plan \
    -var="project_id=$PROJECT_ID" \
    -var="region=$REGION" \
    -var="environment=$ENVIRONMENT" \
    -out=tfplan

# Apply the deployment
terraform apply -auto-approve tfplan

# Get the service URL
SERVICE_URL=$(terraform output -raw cloud_run_service_url)

cd ..

echo "✅ Deployment completed successfully!"
echo "🌐 Service URL: $SERVICE_URL"
echo "🏥 Health Check: $SERVICE_URL/health"
echo "📋 API Documentation: $SERVICE_URL/api/tasks"

# Test the deployment
echo "🧪 Testing deployment..."
sleep 30

if curl -f "$SERVICE_URL/health" > /dev/null 2>&1; then
    echo "✅ Health check passed!"
else
    echo "❌ Health check failed!"
    exit 1
fi

echo "🎉 Deployment successful!"
