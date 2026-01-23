output "app_instance_id" {
  description = "App EC2 instance ID"
  value       = aws_instance.app.id
}

output "app_private_ip" {
  description = "App EC2 private IP"
  value       = aws_instance.app.private_ip
}

output "app_public_ip" {
  description = "App EC2 public IP"
  value       = aws_instance.app.public_ip
}

output "monitoring_instance_id" {
  description = "Monitoring EC2 instance ID"
  value       = aws_instance.monitoring.id
}

output "monitoring_private_ip" {
  description = "Monitoring EC2 private IP"
  value       = aws_instance.monitoring.private_ip
}

output "monitoring_public_ip" {
  description = "Monitoring EC2 Elastic IP"
  value       = aws_eip.monitoring.public_ip
}

output "ec2_role_arn" {
  description = "EC2 IAM role ARN"
  value       = aws_iam_role.ec2_role.arn
}
