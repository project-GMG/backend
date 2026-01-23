# Terraform Backend Bootstrap
# S3 버킷과 DynamoDB 테이블을 생성합니다.

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

# Terraform 상태 저장을 위한 s3 버킷
resource "aws_s3_bucket" "terraform_state" {
  bucket = "gmg-terraform-state-prod"

  lifecycle {
    prevent_destroy = true
  }

  tags = {
    Name        = "gmg-terraform-state"
    Environment = "prod"
    ManagedBy   = "terraform"
  }
}

# S3 버킷 버전 관리 활성화
resource "aws_s3_bucket_versioning" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

# S3 버킷 암호화
resource "aws_s3_bucket_server_side_encryption_configuration" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# S3 버킷 퍼블릭 액세스 차단
resource "aws_s3_bucket_public_access_block" "terraform_state" {
  bucket = aws_s3_bucket.terraform_state.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# 상태 락을 위한 DynamoDB 테이블
resource "aws_dynamodb_table" "terraform_lock" {
  name         = "gmg-terraform-lock"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Name        = "gmg-terraform-lock"
    Environment = "prod"
    ManagedBy   = "terraform"
  }
}

output "s3_bucket_name" {
  value       = aws_s3_bucket.terraform_state.id
  description = "Terraform state S3 bucket name"
}

output "dynamodb_table_name" {
  value       = aws_dynamodb_table.terraform_lock.name
  description = "Terraform lock DynamoDB table name"
}
