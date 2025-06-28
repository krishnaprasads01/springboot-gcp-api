# Quick Fix for "Database already exists" Error

## What happened?
Terraform tried to create a Firestore database, but one already exists in your GCP project. Google Cloud only allows one default Firestore database per project.

## Quick Solution

### Step 1: Find your Project ID
```bash
# If you're already authenticated with gcloud:
gcloud config get-value project

# Or check from your terraform command/error message
```

### Step 2: Import the existing database
```bash
# Use the automated import script (recommended):
./scripts/import-firestore.sh YOUR_PROJECT_ID

# Or manually import:
cd terraform
terraform import google_firestore_database.database projects/YOUR_PROJECT_ID/databases/\(default\)
```

### Step 3: Run terraform apply again
```bash
cd terraform
terraform apply -var="project_id=YOUR_PROJECT_ID"
```

## Example Commands (replace YOUR_PROJECT_ID)

If your project ID is `my-gcp-project-123`, run:

```bash
# Option 1: Automated script
./scripts/import-firestore.sh my-gcp-project-123

# Option 2: Manual import
cd terraform
terraform import google_firestore_database.database projects/my-gcp-project-123/databases/\(default\)
terraform apply -var="project_id=my-gcp-project-123"
```

## What the import does
- Adds the existing Firestore database to Terraform's state
- Terraform will now manage the existing database instead of trying to create a new one
- No data is lost or modified - it's just a state management operation

## Next steps
After successful import, continue with your terraform apply and the deployment should complete successfully.
