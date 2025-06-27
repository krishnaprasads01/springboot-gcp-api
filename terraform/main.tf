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

# Enable only Cloud Run - No database needed!
resource "google_project_service" "required_apis" {
  for_each = toset([
    "run.googleapis.com"
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

# Service Account for Cloud Run - Minimal
resource "google_service_account" "cloud_run_sa" {
  account_id   = "${var.app_name}-sa-${random_id.suffix.hex}"
  display_name = "Cloud Run SA"
  
  depends_on = [google_project_service.required_apis]
}

# Cloud Run Service - H2 in-memory database (no external DB needed!)
resource "google_cloud_run_v2_service" "api_service" {
  name     = "${var.app_name}-service-${random_id.suffix.hex}"
  location = var.region
  
  template {
    service_account = google_service_account.cloud_run_sa.email
    
    scaling {
      min_instance_count = 0  # Scale to zero
      max_instance_count = 2  # Lower max
    }
    
    containers {
      image = "gcr.io/${var.project_id}/${var.app_name}:latest"
      
      ports {
        container_port = 8080
      }
      
      resources {
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }
      
      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "local"  # Use H2 in-memory database
      }
    }
  }
  
  depends_on = [google_project_service.required_apis]
}

# IAM policy for Cloud Run service (allow public access)
resource "google_cloud_run_service_iam_member" "public_access" {
  location = google_cloud_run_v2_service.api_service.location
  project  = google_cloud_run_v2_service.api_service.project
  service  = google_cloud_run_v2_service.api_service.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
