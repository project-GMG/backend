# VPC Outputs
output "vpc_id" {
  description = "VPC ID"
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = module.vpc.public_subnet_ids
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = module.vpc.private_subnet_ids
}

# EC2 Outputs
output "app_instance_id" {
  description = "App EC2 instance ID"
  value       = module.ec2.app_instance_id
}

output "app_public_ip" {
  description = "App EC2 public IP"
  value       = module.ec2.app_public_ip
}

output "app_private_ip" {
  description = "App EC2 private IP"
  value       = module.ec2.app_private_ip
}

output "monitoring_public_ip" {
  description = "Monitoring EC2 Elastic IP"
  value       = module.ec2.monitoring_public_ip
}

output "monitoring_private_ip" {
  description = "Monitoring EC2 private IP"
  value       = module.ec2.monitoring_private_ip
}

# RDS Outputs
output "db_endpoint" {
  description = "RDS endpoint"
  value       = module.rds.db_endpoint
}

output "db_address" {
  description = "RDS address (hostname)"
  value       = module.rds.db_address
}


# ALB Outputs
output "alb_dns_name" {
  description = "ALB DNS name"
  value       = module.alb.alb_dns_name
}

output "acm_validation_records" {
  description = "ACM DNS validation records - app.hosting.kr에 추가 필요!"
  value       = module.alb.acm_domain_validation_options
}

# ECR Outputs
output "ecr_repository_url" {
  description = "ECR repository URL"
  value       = module.ecr.repository_url
}

output "ecr_registry_id" {
  description = "ECR registry ID"
  value       = module.ecr.registry_id
}

# DNS Configuration Guide
output "dns_configuration_guide" {
  description = "app.hosting.kr에서 설정해야 할 DNS 레코드"
  value = <<-EOT
    
    ============================================
    app.hosting.kr DNS 설정 가이드
    ============================================
    
    1. ACM 인증서 검증용 레코드 (CNAME):
       위의 acm_validation_records 출력값 참조
    
    2. API 도메인 연결 (CNAME):
       Name:  api
       Type:  CNAME
       Value: ${module.alb.alb_dns_name}
    
    ============================================
  EOT
}
