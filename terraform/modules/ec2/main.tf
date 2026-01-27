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

  user_data = <<-EOF
    #!/bin/bash
    set -e  # 에러 발생 시 바로 종료

    # 기본 업데이트 & 필요 패키지
    apt-get update -y
    apt-get install -y docker.io unzip curl

    # Docker 서비스 시작 & 자동 시작 설정
    systemctl start docker
    systemctl enable docker
    usermod -aG docker ubuntu

    # AWS CLI v2 설치 (ARM64용 bundled installer)
    curl "https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip" -o "awscliv2.zip"
    unzip awscliv2.zip
    ./aws/install
    rm -rf awscliv2.zip aws  # 정리

    # AWS CLI 버전 확인 (로그용, 옵션)
    aws --version || echo "AWS CLI 설치 확인 실패"

    # Docker Compose v2 (plugin 방식) 설치 - 최신 버전 자동
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-aarch64" \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    # Docker Compose 버전 확인 (로그용)
    docker compose version || echo "Docker Compose 설치 확인 실패"

    # CloudWatch Agent 설치 (ARM64)
    wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/arm64/latest/amazon-cloudwatch-agent.deb
    dpkg -i -E ./amazon-cloudwatch-agent.deb
    rm ./amazon-cloudwatch-agent.deb
  EOF

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
    set -e

    apt-get update -y
    apt-get install -y docker.io unzip curl mysql-client

    systemctl start docker
    systemctl enable docker
    usermod -aG docker ubuntu

    # AWS CLI v2 (ARM64)
    curl "https://awscli.amazonaws.com/awscli-exe-linux-aarch64.zip" -o "awscliv2.zip"
    unzip awscliv2.zip
    ./aws/install
    rm -rf awscliv2.zip aws

    aws --version || echo "AWS CLI 설치 확인 실패"

    # Docker Compose v2 plugin
    mkdir -p /usr/local/lib/docker/cli-plugins
    curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-aarch64" \
      -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

    docker compose version || echo "Docker Compose 설치 확인 실패"

    # CloudWatch Agent 설치 (ARM64)
    wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/arm64/latest/amazon-cloudwatch-agent.deb
    dpkg -i -E ./amazon-cloudwatch-agent.deb
    rm ./amazon-cloudwatch-agent.deb
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
