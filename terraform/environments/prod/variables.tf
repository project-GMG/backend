# General Variables
variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "gmg"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "prod"
}

# VPC Variables
variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "availability_zones" {
  description = "Availability zones"
  type        = list(string)
  default     = ["ap-northeast-2a", "ap-northeast-2c"]
}

variable "public_subnet_cidrs" {
  description = "Public subnet CIDR blocks"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "Private subnet CIDR blocks"
  type        = list(string)
  default     = ["10.0.101.0/24", "10.0.102.0/24"]
}

# EC2 Variables
variable "app_instance_type" {
  description = "App EC2 instance type"
  type        = string
  default     = "t4g.medium"
}

variable "monitoring_instance_type" {
  description = "Monitoring EC2 instance type"
  type        = string
  default     = "t4g.small"
}

variable "key_name" {
  description = "SSH key pair name"
  type        = string
}

# RDS Variables
variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "gmg"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

# Domain Variables
variable "domain_name" {
  description = "Domain name for ACM certificate"
  type        = string
  default     = "api.xn--o39aa312i.com"  # api.가면가.com의 punycode
}
