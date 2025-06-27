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

# Enable required APIs
resource "google_project_service" "required_apis" {
  for_each = toset([
    "cloudresourcemanager.googleapis.com",
    "cloudbuild.googleapis.com",
    "run.googleapis.com",
    "sql-component.googleapis.com",
    "sqladmin.googleapis.com",
    "vpcaccess.googleapis.com",
    "servicenetworking.googleapis.com",
    "iam.googleapis.com",
    "logging.googleapis.com",
    "monitoring.googleapis.com"
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

# VPC Network
resource "google_compute_network" "vpc_network" {
  name                    = "${var.app_name}-vpc-${random_id.suffix.hex}"
  auto_create_subnetworks = false
  
  depends_on = [google_project_service.required_apis]
}

# Subnet
resource "google_compute_subnetwork" "subnet" {
  name          = "${var.app_name}-subnet-${random_id.suffix.hex}"
  ip_cidr_range = "10.0.0.0/24"
  region        = var.region
  network       = google_compute_network.vpc_network.id

  private_ip_google_access = true
}

# Private IP allocation for Cloud SQL
resource "google_compute_global_address" "private_ip_allocation" {
  name          = "${var.app_name}-private-ip-${random_id.suffix.hex}"
  purpose       = "VPC_PEERING"
  address_type  = "INTERNAL"
  prefix_length = 16
  network       = google_compute_network.vpc_network.id
  
  depends_on = [google_project_service.required_apis]
}

# Private connection for Cloud SQL
resource "google_service_networking_connection" "private_vpc_connection" {
  network                 = google_compute_network.vpc_network.id
  service                 = "servicenetworking.googleapis.com"
  reserved_peering_ranges = [google_compute_global_address.private_ip_allocation.name]
  
  depends_on = [google_project_service.required_apis]
}

# VPC Access Connector for Cloud Run
resource "google_vpc_access_connector" "connector" {
  name           = "${var.app_name}-connector-${random_id.suffix.hex}"
  region         = var.region
  network        = google_compute_network.vpc_network.name
  ip_cidr_range  = "10.8.0.0/28"
  max_throughput = 300
  
  depends_on = [
    google_project_service.required_apis,
    google_compute_subnetwork.subnet
  ]
}

# Cloud SQL Database Instance
resource "google_sql_database_instance" "postgres" {
  name             = "${var.app_name}-db-${random_id.suffix.hex}"
  database_version = var.database_version
  region           = var.region
  
  deletion_protection = false

  settings {
    tier = var.database_tier
    
    ip_configuration {
      ipv4_enabled                                  = false
      private_network                              = google_compute_network.vpc_network.id
      enable_private_path_for_google_cloud_services = true
    }

    backup_configuration {
      enabled    = true
      start_time = "23:00"
    }

    database_flags {
      name  = "log_statement"
      value = "all"
    }
  }

  depends_on = [
    google_service_networking_connection.private_vpc_connection,
    google_project_service.required_apis
  ]
}

# Database
resource "google_sql_database" "database" {
  name     = "${var.app_name}_${var.environment}"
  instance = google_sql_database_instance.postgres.name
}

# Database User
resource "random_password" "db_password" {
  length  = 16
  special = true
}

resource "google_sql_user" "users" {
  name     = "${var.app_name}_user"
  instance = google_sql_database_instance.postgres.name
  password = random_password.db_password.result
}

# Service Account for Cloud Run
resource "google_service_account" "cloud_run_sa" {
  account_id   = "${var.app_name}-run-sa-${random_id.suffix.hex}"
  display_name = "Cloud Run Service Account for ${var.app_name}"
  
  depends_on = [google_project_service.required_apis]
}

# IAM roles for the service account
resource "google_project_iam_member" "cloud_run_sa_roles" {
  for_each = toset([
    "roles/cloudsql.client",
    "roles/logging.logWriter",
    "roles/monitoring.metricWriter",
    "roles/cloudtrace.agent"
  ])
  
  project = var.project_id
  role    = each.key
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

# Secret for database password
resource "google_secret_manager_secret" "db_password" {
  secret_id = "${var.app_name}-db-password-${random_id.suffix.hex}"
  
  replication {
    auto {}
  }
  
  depends_on = [google_project_service.required_apis]
}

resource "google_secret_manager_secret_version" "db_password" {
  secret      = google_secret_manager_secret.db_password.id
  secret_data = random_password.db_password.result
}

# Secret accessor role for service account
resource "google_secret_manager_secret_iam_member" "db_password_accessor" {
  secret_id = google_secret_manager_secret.db_password.secret_id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

# Cloud Run Service
resource "google_cloud_run_v2_service" "api_service" {
  name     = "${var.app_name}-service-${random_id.suffix.hex}"
  location = var.region
  
  template {
    service_account = google_service_account.cloud_run_sa.email
    
    vpc_access {
      connector = google_vpc_access_connector.connector.id
      egress    = "PRIVATE_RANGES_ONLY"
    }
    
    scaling {
      min_instance_count = var.min_instances
      max_instance_count = var.max_instances
    }
    
    containers {
      image = "gcr.io/${var.project_id}/${var.app_name}:latest"
      
      ports {
        container_port = var.container_port
      }
      
      resources {
        limits = {
          cpu    = var.cpu_limit
          memory = var.memory_limit
        }
      }
      
      env {
        name  = "SPRING_PROFILES_ACTIVE"
        value = "gcp"
      }
      
      env {
        name  = "GCP_PROJECT_ID"
        value = var.project_id
      }
      
      env {
        name  = "GCP_DATABASE_NAME"
        value = google_sql_database.database.name
      }
      
      env {
        name  = "GCP_INSTANCE_CONNECTION_NAME"
        value = google_sql_database_instance.postgres.connection_name
      }
      
      env {
        name  = "GCP_DATABASE_USERNAME"
        value = google_sql_user.users.name
      }
      
      env {
        name = "GCP_DATABASE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
      
      env {
        name  = "PORT"
        value = tostring(var.container_port)
      }
    }
  }
  
  depends_on = [
    google_project_service.required_apis,
    google_vpc_access_connector.connector,
    google_sql_database_instance.postgres,
    google_secret_manager_secret_version.db_password
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
