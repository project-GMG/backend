variable "project_name" {
  description = "Project name for resource naming"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "app_instance_type" {
  description = "Instance type for App EC2"
  type        = string
  default     = "t4g.medium"
}

variable "monitoring_instance_type" {
  description = "Instance type for Monitoring EC2"
  type        = string
  default     = "t4g.small"
}

variable "key_name" {
  description = "SSH key pair name"
  type        = string
}

variable "public_subnet_ids" {
  description = "List of public subnet IDs"
  type        = list(string)
}

variable "app_sg_id" {
  description = "Security group ID for App EC2"
  type        = string
}

variable "monitoring_sg_id" {
  description = "Security group ID for Monitoring EC2"
  type        = string
}
