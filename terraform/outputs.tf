output "cloud_run_service_url" {
  description = "URL of the deployed Cloud Run service"
  value       = google_cloud_run_v2_service.api_service.uri
}

# No database outputs needed - using H2 in-memory database

output "firestore_database_name" {
  description = "Firestore database name"
  value       = data.google_firestore_database.database.name
}

output "firestore_location" {
  description = "Firestore database location"
  value       = data.google_firestore_database.database.location_id
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

output "container_image_url" {
  description = "Container image URL for Cloud Run"
  value       = "gcr.io/${var.project_id}/${var.app_name}:latest"
}
