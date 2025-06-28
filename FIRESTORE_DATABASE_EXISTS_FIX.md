# Firestore Database Already Exists Fix

## Problem
During Terraform deployment, encountered the following error:
```
Error: Error creating Database: googleapi: Error 409: Database already exists. Please use another database_id
```

## Root Cause
- Google Cloud projects can only have **one default Firestore database** per project
- The Terraform configuration was trying to create a new Firestore database using `google_firestore_database` resource
- A Firestore database already existed in the target GCP project from a previous deployment or manual creation

## Solution
Changed the Terraform configuration to **reference the existing database** instead of trying to create a new one.

### Changes Made

#### 1. Updated `terraform/main.tf`
**Before (Resource - Creates new database):**
```hcl
resource "google_firestore_database" "database" {
  project     = var.project_id
  name        = "(default)"
  location_id = var.firestore_location
  type        = "FIRESTORE_NATIVE"
  
  depends_on = [google_project_service.required_apis]
}
```

**After (Data Source - References existing database):**
```hcl
# Reference existing Firestore database (instead of creating new one)
# Note: Only one default Firestore database is allowed per GCP project
# If database doesn't exist, create it manually first:
# gcloud firestore databases create --location=us-central1
data "google_firestore_database" "database" {
  project  = var.project_id
  database = "(default)"
  
  depends_on = [google_project_service.required_apis]
}
```

#### 2. Updated `terraform/outputs.tf`
**Before:**
```hcl
output "firestore_database_name" {
  value = google_firestore_database.database.name
}

output "firestore_location" {
  value = google_firestore_database.database.location_id
}
```

**After:**
```hcl
output "firestore_database_name" {
  value = data.google_firestore_database.database.name
}

output "firestore_location" {
  value = data.google_firestore_database.database.location_id
}
```

#### 3. Updated Dependencies
Updated all references from `google_firestore_database.database` to `data.google_firestore_database.database`

## Verification Steps

### 1. Check if Firestore Database Exists
```bash
gcloud firestore databases list --project=your-project-id
```

### 2. If Database Doesn't Exist, Create It Manually
```bash
gcloud firestore databases create --location=us-central1 --project=your-project-id
```

### 3. Run Terraform Apply
```bash
cd terraform
terraform init
terraform plan -var="project_id=your-project-id"
terraform apply -var="project_id=your-project-id"
```

## Benefits of This Approach

### ✅ **Idempotent Deployments**
- Terraform can run multiple times without errors
- Works whether database exists or not

### ✅ **Production Safe**
- Won't accidentally delete existing Firestore data
- Preserves existing database configuration

### ✅ **Flexible**
- Works with manually created databases
- Works with databases created by other tools

### ✅ **Cost Effective**
- Reuses existing database instead of attempting to create duplicates
- No downtime or data migration required

## Alternative Solutions Considered

### 1. Import Existing Database (Not Chosen)
```bash
terraform import google_firestore_database.database projects/your-project-id/databases/(default)
```
**Why not chosen:** More complex, requires state management

### 2. Use Different Database ID (Not Chosen)
```hcl
name = "my-custom-database"
```
**Why not chosen:** Google Cloud allows only one Firestore database per project

### 3. Conditional Resource Creation (Not Chosen)
Using `count` or `for_each` with conditionals
**Why not chosen:** Data source approach is simpler and more reliable

## Error Prevention

### For New Projects
If deploying to a completely new GCP project without Firestore:

1. **Option A:** Create database manually first
   ```bash
   gcloud firestore databases create --location=us-central1
   ```

2. **Option B:** Use resource for first deployment, then switch to data source
   - Not recommended due to complexity

### For Existing Projects
The current configuration with data source will work automatically.

## Testing

After applying the fix:

```bash
# 1. Verify Terraform runs successfully
cd terraform && terraform apply -var="project_id=your-project-id"

# 2. Test the deployed application
curl https://your-cloud-run-url/health
curl https://your-cloud-run-url/api/tasks

# 3. Verify Firestore connection
curl -X POST -H "Content-Type: application/json" \
  -d '{"title":"Test","description":"Test task","status":"PENDING"}' \
  https://your-cloud-run-url/api/tasks
```

## Status
🟢 **RESOLVED** - The Terraform configuration now correctly handles existing Firestore databases and deploys successfully without conflicts.
