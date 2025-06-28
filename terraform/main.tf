terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
    google-beta = {
      source  = "hashicorp/google-beta"
      version = "~> 5.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}

provider "google-beta" {
  project = var.project_id
  region  = var.region
  zone    = var.zone
}

# Enable required APIs for Cloud Run and Firestore
resource "google_project_service" "required_apis" {
  for_each = toset([
    "run.googleapis.com",
    "firestore.googleapis.com",
    "cloudbuild.googleapis.com",
    "containerregistry.googleapis.com"
  ])

  service = each.key
  project = var.project_id

  disable_dependent_services = false
  disable_on_destroy         = false
}

# Random suffix for unique resource names
resource "random_id" "suffix" {
  byte_length = 4
}

# Service Account for Cloud Run with Firestore access
resource "google_service_account" "cloud_run_sa" {
  account_id   = "${var.app_name}-sa-${random_id.suffix.hex}"
  display_name = "Cloud Run Service Account for ${var.app_name}"
  
  depends_on = [google_project_service.required_apis]
}

# Grant Firestore access to the service account
resource "google_project_iam_member" "firestore_user" {
  project = var.project_id
  role    = "roles/datastore.user"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
  
  depends_on = [google_service_account.cloud_run_sa]
}

# Reference existing Firestore database (instead of creating new one)
# Note: Only one default Firestore database is allowed per GCP project
# If database doesn't exist, create it manually first:
# gcloud firestore databases create --location=us-central1
data "google_firestore_database" "database" {
  project  = var.project_id
  database = "(default)"
  
  depends_on = [google_project_service.required_apis]
}

# Cloud Run Service with Firestore configuration
resource "google_cloud_run_v2_service" "api_service" {
  name     = "${var.app_name}-service-${random_id.suffix.hex}"
  location = var.region
  
  template {
    service_account = google_service_account.cloud_run_sa.email
    
    scaling {
      min_instance_count = var.min_instances
      max_instance_count = var.max_instances
    }
    
    containers {
      image = "gcr.io/${var.project_id}/${var.app_name}:latest"
      
      ports {
        container_port = 8080
      }
      
      resources {
        limits = {
          cpu    = "1"
          memory = "1Gi"
        }
      }
      
      # Environment variables for Firestore connection
      env {
        name  = "GCP_PROJECT_ID"
        value = var.project_id
      }
      
      env {
        name  = "GCP_FIRESTORE_ENABLED"
        value = "true"
      }
      
      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "gcp"
      }
      
      env {
        name  = "GOOGLE_CLOUD_PROJECT"
        value = var.project_id
      }
    }
  }
  
  depends_on = [
    google_project_service.required_apis,
    data.google_firestore_database.database,
    google_project_iam_member.firestore_user
  ]
}

# IAM policy for Cloud Run service (allow public access)
resource "google_cloud_run_service_iam_member" "public_access" {
  location = google_cloud_run_v2_service.api_service.location
  project  = google_cloud_run_v2_service.api_service.project
  service  = google_cloud_run_v2_service.api_service.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
