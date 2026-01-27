# =====================================================
# EC2 Module
# App EC2 + Monitoring EC2
# =====================================================

# Ubuntu
data "aws_ami" "ubuntu_arm" {
  most_recent = true
  owners      = ["099720109477"]  # Canonical 공식

  filter {
    name   = "name"
    values = ["ubuntu/images/hvm-ssd-gp3/ubuntu-noble-24.04-arm64-server-*"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# EC2 Instance Profile (ECR 접근용)
resource "aws_iam_role" "ec2_role" {
  name = "${var.project_name}-ec2-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name        = "${var.project_name}-ec2-role-${var.environment}"
    Environment = var.environment
  }
}

# ECR 읽기 권한 정책
resource "aws_iam_role_policy" "ecr_policy" {
  name = "${var.project_name}-ecr-policy-${var.environment}"
  role = aws_iam_role.ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:BatchCheckLayerAvailability",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_instance_profile" "ec2_profile" {
  name = "${var.project_name}-ec2-profile-${var.environment}"
  role = aws_iam_role.ec2_role.name
}

# App EC2 Instance
resource "aws_instance" "app" {
  ami                    = data.aws_ami.ubuntu_arm.id
  instance_type          = var.app_instance_type
  key_name               = var.key_name
  subnet_id              = var.public_subnet_ids[0]
  vpc_security_group_ids = [var.app_sg_id]
  iam_instance_profile   = aws_iam_instance_profile.ec2_profile.name

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = base64encode(<<-EOF
    #!/bin/bash
    # Docker 설치
    apt-get update -y
    apt-get install -y docker.io
    systemctl start docker
    systemctl enable docker
    usermod -a -G docker ubuntu
    
    # Docker Compose 설치
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    
    # AWS CLI 설치 (ECR 로그인용)
    apt-get install -y awscli
  EOF
  )

  tags = {
    Name        = "${var.project_name}-app-${var.environment}"
    Environment = var.environment
    Role        = "app"
  }
}

# Monitoring EC2 Instance
resource "aws_instance" "monitoring" {
  ami                    = data.aws_ami.ubuntu_arm.id
  instance_type          = var.monitoring_instance_type
  key_name               = var.key_name
  subnet_id              = var.public_subnet_ids[0]
  vpc_security_group_ids = [var.monitoring_sg_id]

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
    encrypted   = true
  }

  user_data = <<-EOF
    #!/bin/bash
    # Docker 설치
    apt-get update -y
    apt-get install -y docker.io
    systemctl start docker
    systemctl enable docker
    usermod -a -G docker ubuntu

    # Docker Compose 설치
    curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose

    # AWS CLI 설치 (ECR 로그인용)
    apt-get install -y awscli

    # MySQL Client 설치 (RDS 접속용)
    apt-get install -y mysql-client
  EOF

  tags = {
    Name        = "${var.project_name}-monitoring-${var.environment}"
    Environment = var.environment
    Role        = "monitoring"
  }
}

# Monitoring EC2 Elastic IP
resource "aws_eip" "monitoring" {
  instance = aws_instance.monitoring.id
  domain   = "vpc"

  tags = {
    Name        = "${var.project_name}-monitoring-eip-${var.environment}"
    Environment = var.environment
  }
}
