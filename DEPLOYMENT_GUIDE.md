# F137-K-SendGrid Deployment Guide

## Overview
The f137-k-sendgrid service is a Kafka consumer that processes email notification messages from the `form137-email-notifications` topic and sends emails via SendGrid.

## Configuration Summary

### Kafka Configuration
- **Bootstrap Servers**: `kafka-external.kafka.svc.cluster.local:9092`
- **Consumer Group**: `form137-sendgrid-consumer`
- **Topic**: `form137-email-notifications`
- **Auto Offset Reset**: `earliest`

### SendGrid Configuration
- **API Key**: Provided via Kubernetes secret
- **From Email**: Configured via values.yaml or secret

## Prerequisites

1. **Kafka Cluster**: Ensure the Kafka cluster is deployed in the `kafka` namespace using the kube-kafka configuration
2. **Topic Creation**: The `form137-email-notifications` topic should be created automatically by the kube-kafka deployment
3. **SendGrid Account**: You need a valid SendGrid API key

## Deployment Steps

### 1. Create SendGrid Secret
```bash
kubectl create secret generic sendgrid \
  --from-literal=sendgrid-api-key='your-sendgrid-api-key' \
  --from-literal=sendgrid-from-email='no-reply@yourdomain.com'
```

### 2. Update Helm Values
Copy `values-example.yaml` to `values.yaml` and update as needed:
```bash
cp values-example.yaml values.yaml
# Edit values.yaml with your specific configuration
```

### 3. Deploy Using Helm
```bash
helm install f137-sendgrid ./helm/kafka-sendgrid \
  --values ./helm/kafka-sendgrid/values.yaml
```

### 4. Verify Deployment
```bash
# Check pod status
kubectl get pods -l app.kubernetes.io/name=kafka-sendgrid

# Check logs
kubectl logs -l app.kubernetes.io/name=kafka-sendgrid -f

# Run Helm tests
helm test f137-sendgrid
```

## Integration with Form137 API

The f137-k-sendgrid service is designed to consume messages published by the form137-api service. The message format is:

```json
{
  "to": "user@example.com",
  "from": "no-reply@yourdomain.com",
  "subject": "Form 137 Request Status Update",
  "body": "Your Form 137 request has been updated..."
}
```

## Monitoring and Troubleshooting

### Health Checks
- **Liveness Probe**: `/actuator/health` (30s delay, 10s interval)
- **Readiness Probe**: `/actuator/health` (5s delay, 5s interval)

### Common Issues

1. **Kafka Connection Failed**
   - Verify Kafka cluster is running: `kubectl get pods -n kafka`
   - Check network policies allow communication
   - Verify bootstrap server address

2. **Topic Not Found**
   - Ensure `form137-email-notifications` topic exists
   - Check topic creation job: `kubectl get jobs -n kafka`

3. **SendGrid Authentication Failed**
   - Verify SendGrid API key is correct
   - Check secret is properly created: `kubectl get secret sendgrid -o yaml`

4. **Message Processing Errors**
   - Check application logs for JSON parsing errors
   - Verify message format matches EmailMessage model

### Scaling

The service supports horizontal pod autoscaling based on CPU and memory usage:
- **Min Replicas**: 2
- **Max Replicas**: 10
- **CPU Target**: 80%
- **Memory Target**: 80%

## Security

- Runs as non-root user (UID: 1001)
- Read-only root filesystem
- All capabilities dropped
- Pod disruption budget ensures high availability