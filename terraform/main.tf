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

# Enable only essential APIs for minimal setup
resource "google_project_service" "required_apis" {
  for_each = toset([
    "run.googleapis.com",
    "sqladmin.googleapis.com"
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

# VPC Network - Commented out for minimal setup
# resource "google_compute_network" "vpc_network" {
#   name                    = "${var.app_name}-vpc-${random_id.suffix.hex}"
#   auto_create_subnetworks = false
#   
#   depends_on = [google_project_service.required_apis]
# }

# Subnet - Commented out for minimal setup
# resource "google_compute_subnetwork" "subnet" {
#   name          = "${var.app_name}-subnet-${random_id.suffix.hex}"
#   ip_cidr_range = "10.0.0.0/24"
#   region        = var.region
#   network       = google_compute_network.vpc_network.id

#   private_ip_google_access = true
# }

# Private IP allocation for Cloud SQL - Commented out for minimal setup
# resource "google_compute_global_address" "private_ip_allocation" {
#   name          = "${var.app_name}-private-ip-${random_id.suffix.hex}"
#   purpose       = "VPC_PEERING"
#   address_type  = "INTERNAL"
#   prefix_length = 16
#   network       = google_compute_network.vpc_network.id
#   
#   depends_on = [google_project_service.required_apis]
# }

# Private connection for Cloud SQL - Commented out for minimal setup
# resource "google_service_networking_connection" "private_vpc_connection" {
#   network                 = google_compute_network.vpc_network.id
#   service                 = "servicenetworking.googleapis.com"
#   reserved_peering_ranges = [google_compute_global_address.private_ip_allocation.name]
#   
#   depends_on = [google_project_service.required_apis]
# }

# VPC Access Connector for Cloud Run - Commented out for minimal setup
# resource "google_vpc_access_connector" "connector" {
#   name           = "${var.app_name}-connector-${random_id.suffix.hex}"
#   region         = var.region
#   network        = google_compute_network.vpc_network.name
#   ip_cidr_range  = "10.8.0.0/28"
#   max_throughput = 300
#   
#   depends_on = [
#     google_project_service.required_apis,
#     google_compute_subnetwork.subnet
#   ]
# }

# Cloud SQL Database Instance - Ultra minimal configuration
resource "google_sql_database_instance" "postgres" {
  name             = "${var.app_name}-db-${random_id.suffix.hex}"
  database_version = "POSTGRES_15"
  region           = var.region
  
  deletion_protection = false

  settings {
    tier                = "db-f1-micro"  # Smallest possible
    disk_size          = 10             # Minimum
    disk_type          = "PD_HDD"       # Cheapest
    disk_autoresize    = false
    availability_type  = "ZONAL"       # Single zone for speed
    
    # Public IP - fastest setup
    ip_configuration {
      ipv4_enabled = true
      authorized_networks {
        value = "0.0.0.0/0"
        name  = "all"
      }
    }

    # Disable everything for speed
    backup_configuration {
      enabled = false
    }
    
    # Remove database flags
  }

  depends_on = [google_project_service.required_apis]

  timeouts {
    create = "15m"  # Reduced timeout
    update = "15m"
    delete = "15m"
  }
}

# Database
resource "google_sql_database" "database" {
  name     = "${var.app_name}_${var.environment}"
  instance = google_sql_database_instance.postgres.name
}

# Database User - Simple password
resource "random_password" "db_password" {
  length  = 12  # Shorter password
  special = false  # No special chars for simplicity
}

resource "google_sql_user" "users" {
  name     = "${var.app_name}_user"
  instance = google_sql_database_instance.postgres.name
  password = random_password.db_password.result
}

# Service Account for Cloud Run - Minimal
resource "google_service_account" "cloud_run_sa" {
  account_id   = "${var.app_name}-sa-${random_id.suffix.hex}"
  display_name = "Cloud Run SA"
  
  depends_on = [google_project_service.required_apis]
}

# Only essential IAM role
resource "google_project_iam_member" "cloud_run_sa_roles" {
  project = var.project_id
  role    = "roles/cloudsql.client"
  member  = "serviceAccount:${google_service_account.cloud_run_sa.email}"
}

# Cloud Run Service - Ultra simplified
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
          memory = "512Mi"  # Lower memory
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
      
      # Direct password - not secure but faster for dev
      env {
        name  = "GCP_DATABASE_PASSWORD"
        value = random_password.db_password.result
      }
      
      env {
        name  = "PORT"
        value = "8080"
      }

      env {
        name  = "DATABASE_URL"
        value = "jdbc:postgresql://${google_sql_database_instance.postgres.public_ip_address}:5432/${google_sql_database.database.name}?sslmode=require"
      }
    }
  }
  
  depends_on = [
    google_project_service.required_apis,
    google_sql_database_instance.postgres
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
