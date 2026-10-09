# Kubernetes Deployment

## Architecture & Design

The service runs as a stateless Spring MVC application in Kubernetes. Persistent data is stored in an external MySQL database, and country details are retrieved synchronously from the upstream SOAP service.

- **Scaling & High Availability:** Horizontal Pod Autoscaler (HPA) scales pods between 1 and 4 replicas based on 80% target CPU or memory utilization. A PodDisruptionBudget ensures at least 1 replica remains available during voluntary disruptions and node drains.
- **Database Connection Management:** Each pod maintains a default Hikari pool of 10 connections (up to 50 connections across 10 pods). Ensure the MySQL server max connection limit accommodates this budget.
- **Probes:**
  - Readiness check includes MySQL connectivity to ensure non-ready pods do not receive traffic.
  - Liveness check monitors internal application health without depending on external databases to prevent cascade restarts during database latency.
- **Resilience:** Circuit breakers and timeouts protect against upstream SOAP delays and failures, returning HTTP 502 when the upstream service is degraded.
- **Metrics & Logging:** Prometheus metrics are exposed at `/country-info-st-v1/actuator/prometheus`. Logs are emitted as structured JSON to stdout for log aggregators.

## Prerequisites

- Kubernetes cluster with `kubectl` configured.
- Docker (or access to an image registry for remote clusters).
- External MySQL instance reachable from the Kubernetes cluster.

## Secrets Configuration

Set up `country-info-secret` and `country-info-configmap` in the `country-info` namespace containing:
- `DB_CONNECTION_STRING`
- `DB_USERNAME`
- `DB_PASSWORD`

You can configure this using `k8s/secret.yaml` abd `k8s/configmap.yaml`.

## Deployment

### Option A: Using the Deployment Script

For local a clusters (e.g. Docker Desktop Kubernetes):
```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml
IMAGE_REF=country-info-st-v1:1.0.0 PUSH_IMAGE=false bash scripts/deploy-k8s.sh
```

For remote clusters:
```bash
IMAGE_REF=registry.example.com/team/country-info-st-v1:1.0.0 bash scripts/deploy-k8s.sh
```

### Option B: Manual Deployment

```bash
# 1. Namespace & Secret
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml

# 2. Build Image
docker build -t country-info-st-v1:1.0.0 .

# 3. Apply manifests
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/autoscaling.yaml
kubectl apply -f k8s/pdb.yaml
```

## Verification & Access

Check resource status and verify connectivity:

```bash
kubectl get pods,svc,hpa,pdb -n country-info

# Port-forward to local port 8080
kubectl port-forward -n country-info svc/country-info-st-v1 8080:80

# Verify API
curl http://localhost:8080/country-info-st-v1/api/v1/countries
```

Swagger UI is available at `http://localhost:8080/country-info-st-v1/swagger-ui.html`.

## Cleanup

To remove the deployed resources:

```bash
kubectl delete -f k8s/
```