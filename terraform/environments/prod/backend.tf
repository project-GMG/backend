terraform {
  backend "s3" {
    bucket         = "gmg-terraform-state-prod"
    key            = "prod/terraform.tfstate"
    region         = "ap-northeast-2"
    dynamodb_table = "gmg-terraform-lock"
    encrypt        = true
  }
}
