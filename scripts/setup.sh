#!/bin/bash

# Setup script for Spring Boot GCP API project
# Usage: ./scripts/setup.sh [project-id]

set -e

PROJECT_ID=${1:-}
REGION="us-central1"

echo "🛠️  Setting up Spring Boot GCP API project..."

if [ -z "$PROJECT_ID" ]; then
    echo "❌ Please provide a GCP Project ID"
    echo "Usage: ./scripts/setup.sh [project-id]"
    exit 1
fi

echo "Project ID: $PROJECT_ID"

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ gcloud CLI is not installed. Please install it first."
    echo "Visit: https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Check if terraform is installed
if ! command -v terraform &> /dev/null; then
    echo "❌ Terraform is not installed. Please install it first."
    echo "Visit: https://developer.hashicorp.com/terraform/downloads"
    exit 1
fi

# Authenticate with gcloud
echo "🔐 Authenticating with Google Cloud..."
gcloud auth login

# Set the project
gcloud config set project $PROJECT_ID

# Enable billing (user needs to do this manually)
echo "💳 Please ensure billing is enabled for project: $PROJECT_ID"
echo "Visit: https://console.cloud.google.com/billing/linkedaccount?project=$PROJECT_ID"
read -p "Press Enter after enabling billing..."

# Create service account for GitHub Actions
echo "🤖 Creating service account for GitHub Actions..."
SA_NAME="github-actions-sa"
SA_EMAIL="$SA_NAME@$PROJECT_ID.iam.gserviceaccount.com"

# Check if service account already exists
if ! gcloud iam service-accounts describe $SA_EMAIL &> /dev/null; then
    gcloud iam service-accounts create $SA_NAME \
        --display-name="GitHub Actions Service Account" \
        --description="Service account for GitHub Actions CI/CD"
fi

# Assign necessary roles
echo "🔑 Assigning roles to service account..."
ROLES=(
    "roles/editor"
    "roles/cloudsql.admin"
    "roles/run.admin"
    "roles/storage.admin"
    "roles/iam.serviceAccountUser"
    "roles/compute.networkAdmin"
    "roles/secretmanager.admin"
)

for role in "${ROLES[@]}"; do
    gcloud projects add-iam-policy-binding $PROJECT_ID \
        --member="serviceAccount:$SA_EMAIL" \
        --role="$role"
done

# Create and download service account key
echo "🔐 Creating service account key..."
gcloud iam service-accounts keys create key.json \
    --iam-account=$SA_EMAIL

echo "✅ Service account key created: key.json"

# Create Terraform variables file
echo "📝 Creating Terraform variables file..."
cat > terraform/terraform.tfvars << EOF
project_id     = "$PROJECT_ID"
region         = "$REGION"
zone           = "$REGION-a"
app_name       = "springboot-gcp-api"
environment    = "dev"
database_tier  = "db-f1-micro"
min_instances  = 0
max_instances  = 5
cpu_limit      = "1000m"
memory_limit   = "512Mi"
EOF

echo "✅ Terraform variables file created: terraform/terraform.tfvars"

# Initialize Terraform
echo "🏗️  Initializing Terraform..."
cd terraform
terraform init
cd ..

echo "📋 Setup completed successfully!"
echo ""
echo "Next steps:"
echo "1. Add the following secrets to your GitHub repository:"
echo "   - GCP_PROJECT_ID: $PROJECT_ID"
echo "   - GCP_SA_KEY: $(base64 -i key.json)"
echo ""
echo "2. Review and modify terraform/terraform.tfvars if needed"
echo ""
echo "3. Test locally:"
echo "   ./mvnw spring-boot:run"
echo ""
echo "4. Deploy to GCP:"
echo "   ./scripts/deploy.sh $PROJECT_ID"
echo ""
echo "⚠️  Important: Store the key.json file securely and don't commit it to Git!"
echo "⚠️  The key.json file has been added to .gitignore"
