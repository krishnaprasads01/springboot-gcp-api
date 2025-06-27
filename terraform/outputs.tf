output "cloud_run_service_url" {
  description = "URL of the deployed Cloud Run service"
  value       = google_cloud_run_v2_service.api_service.uri
}

output "database_connection_name" {
  description = "Cloud SQL instance connection name"
  value       = google_sql_database_instance.postgres.connection_name
}

output "database_public_ip" {
  description = "Database public IP address"
  value       = google_sql_database_instance.postgres.public_ip_address
}

output "database_name" {
  description = "Database name"
  value       = google_sql_database.database.name
}

output "database_username" {
  description = "Database username"
  value       = google_sql_user.users.name
  sensitive   = true
}

output "project_id" {
  description = "GCP Project ID"
  value       = var.project_id
}

output "region" {
  description = "GCP Region"
  value       = var.region
}

output "service_account_email" {
  description = "Service account email for Cloud Run"
  value       = google_service_account.cloud_run_sa.email
}

# VPC Network output - Commented out since VPC is not used in minimal setup
# output "vpc_network_name" {
#   description = "VPC Network name"
#   value       = google_compute_network.vpc_network.name
# }

output "container_image_url" {
  description = "Container image URL for Cloud Run"
  value       = "gcr.io/${var.project_id}/${var.app_name}:latest"
}
