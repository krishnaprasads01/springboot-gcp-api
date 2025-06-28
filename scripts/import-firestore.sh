#!/bin/bash

# Firestore Database Import Helper Script
# This script helps import an existing Firestore database into Terraform state

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if project ID is provided
if [ -z "$1" ]; then
    print_error "Usage: $0 <project-id>"
    print_error "Example: $0 my-gcp-project-id"
    exit 1
fi

PROJECT_ID="$1"

print_status "Starting Firestore database import process for project: $PROJECT_ID"

# Check if gcloud is installed and authenticated
if ! command -v gcloud &> /dev/null; then
    print_error "gcloud CLI is not installed. Please install it first."
    exit 1
fi

# Check if user is authenticated
if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" | grep -q .; then
    print_error "You are not authenticated with gcloud. Please run 'gcloud auth login' first."
    exit 1
fi

# Set the project
print_status "Setting project to $PROJECT_ID"
gcloud config set project "$PROJECT_ID"

# Check if Firestore database exists
print_status "Checking if Firestore database exists..."
if gcloud firestore databases list --project="$PROJECT_ID" --format="value(name)" | grep -q "(default)"; then
    print_success "Firestore database already exists"
    DATABASE_EXISTS=true
else
    print_warning "No Firestore database found"
    DATABASE_EXISTS=false
fi

# Navigate to terraform directory
if [ ! -d "terraform" ]; then
    print_error "terraform directory not found. Please run this script from the project root."
    exit 1
fi

cd terraform

# Initialize terraform if needed
if [ ! -d ".terraform" ]; then
    print_status "Initializing Terraform..."
    terraform init
fi

# Check if database is already in state
print_status "Checking Terraform state..."
if terraform state list | grep -q "google_firestore_database.database"; then
    print_success "Firestore database is already in Terraform state"
    DATABASE_IN_STATE=true
else
    print_warning "Firestore database is not in Terraform state"
    DATABASE_IN_STATE=false
fi

# Handle different scenarios
if [ "$DATABASE_EXISTS" = true ] && [ "$DATABASE_IN_STATE" = false ]; then
    print_status "Importing existing Firestore database into Terraform state..."
    terraform import google_firestore_database.database "projects/$PROJECT_ID/databases/(default)"
    print_success "Database imported successfully"
elif [ "$DATABASE_EXISTS" = false ] && [ "$DATABASE_IN_STATE" = false ]; then
    print_status "No database exists. Terraform will create one during apply."
elif [ "$DATABASE_EXISTS" = true ] && [ "$DATABASE_IN_STATE" = true ]; then
    print_success "Database exists and is already managed by Terraform"
else
    print_warning "Unexpected state combination. Please check manually."
fi

# Run terraform plan
print_status "Running terraform plan..."
terraform plan -var="project_id=$PROJECT_ID"

print_success "Import process completed successfully!"
print_status "You can now run: terraform apply -var=\"project_id=$PROJECT_ID\""
