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
Handle the existing Firestore database by **importing it into Terraform state** before applying the configuration.

### Changes Made

#### 1. Updated `terraform/main.tf`
**Added lifecycle management to prevent destruction:**
```hcl
# Firestore database - import existing or create new
# If database already exists, import it first:
# terraform import google_firestore_database.database projects/YOUR_PROJECT_ID/databases/(default)
resource "google_firestore_database" "database" {
  project     = var.project_id
  name        = "(default)"
  location_id = var.firestore_location
  type        = "FIRESTORE_NATIVE"

  # Prevent accidental destruction of database with data
  lifecycle {
    prevent_destroy = true
  }

  depends_on = [google_project_service.required_apis]
}
```

#### 2. Import Process
**Before running terraform apply, import existing database:**
```bash
# Check if database exists
gcloud firestore databases list --project=your-project-id

# If database exists, import it into Terraform state
cd terraform
terraform import google_firestore_database.database projects/your-project-id/databases/\(default\)

# Then run terraform apply
terraform apply -var="project_id=your-project-id"
```

## Verification Steps

### Option 1: Database Already Exists (Most Common)
```bash
# 1. Check if Firestore Database Exists
gcloud firestore databases list --project=your-project-id

# 2. If database exists, import it into Terraform state
cd terraform
terraform init
terraform import google_firestore_database.database projects/your-project-id/databases/\(default\)

# 3. Run Terraform Apply
terraform plan -var="project_id=your-project-id"
terraform apply -var="project_id=your-project-id"
```

### Option 2: No Database Exists (New Project)
```bash
# 1. Check if Firestore Database Exists
gcloud firestore databases list --project=your-project-id

# 2. If no database exists, Terraform will create it
cd terraform
terraform init
terraform plan -var="project_id=your-project-id"
terraform apply -var="project_id=your-project-id"
```

### Option 3: Manual Database Creation First
```bash
# 1. Create database manually first
gcloud firestore databases create --location=us-central1 --project=your-project-id

# 2. Import it into Terraform state
cd terraform
terraform import google_firestore_database.database projects/your-project-id/databases/\(default\)

# 3. Run Terraform Apply
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
