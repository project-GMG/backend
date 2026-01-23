output "alb_sg_id" {
  description = "ALB Security Group ID"
  value       = aws_security_group.alb.id
}

output "app_sg_id" {
  description = "App EC2 Security Group ID"
  value       = aws_security_group.app.id
}

output "monitoring_sg_id" {
  description = "Monitoring EC2 Security Group ID"
  value       = aws_security_group.monitoring.id
}

output "rds_sg_id" {
  description = "RDS Security Group ID"
  value       = aws_security_group.rds.id
}
