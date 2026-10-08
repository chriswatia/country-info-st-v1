# Kubernetes Troubleshooting

Common troubleshooting steps and diagnosis for running `country-info-st-v1` in Kubernetes.

## 1. Pods Are Not Starting / CrashLoopBackOff

```bash
kubectl get pods -n country-info -o wide
kubectl describe pod -n country-info <pod-name>
kubectl logs -n country-info deployment/country-info-st-v1 --all-pods=true --tail=200
kubectl logs -n country-info <pod-name> --previous
```

**Common Causes:**
- **ImagePullBackOff:** Image tag does not exist or image registry credentials are missing.
- **Missing Secrets:** Check that `country-info-secret` exists in namespace `country-info` with keys `DB_CONNECTION_STRING`, `DB_USERNAME`, and `DB_PASSWORD`.
- **Resource Constraints:** Node has insufficient CPU or memory to schedule pods.

## 2. Pods Running but Not Ready (Readiness Probe Failing)

The readiness probe checks both the application and MySQL connectivity:

```bash
kubectl exec -n country-info deploy/country-info-st-v1 -- \
  wget -qO- http://localhost:8080/country-info-st-v1/actuator/health/readiness
```

**Common Causes:**
- Database host is not reachable from the cluster network or firewall blocks port 3306.
- Database credentials in `country-info-secret` are incorrect.
- Database schema does not exist.

Once database connectivity is restored, the pod will automatically mark ready and begin receiving traffic.

## 3. High Memory Usage or OOMKilled

```bash
kubectl describe pod -n country-info <pod-name>
kubectl top pods -n country-info
kubectl get events -n country-info --sort-by=.lastTimestamp
```

**Common Causes:**
- Container exceeded memory limit (1 GiB limit by default). Review JVM heap settings in `k8s/deployment.yaml` or increase container resource limits if needed.

## 4. HPA (Autoscaling) Issues

```bash
kubectl describe hpa country-info-st-v1 -n country-info
kubectl top pods -n country-info
```

**Common Causes:**
- `metrics-server` is not installed or healthy in the cluster.
- Pod CPU requests are not defined (HPA target percentage requires CPU requests to be specified).

## 5. API Errors or Slow Responses

```bash
# Check application logs
kubectl logs -n country-info deployment/country-info-st-v1 --all-pods=true --since=15m

# Check metrics & health
kubectl port-forward -n country-info svc/country-info-st-v1 8080:80
curl -i http://localhost:8080/country-info-st-v1/actuator/health
curl -s http://localhost:8080/country-info-st-v1/actuator/prometheus
```

**Common Causes:**
- **HTTP 502 / Upstream Timeout:** The external SOAP country service is slow or unreachable. The application circuit breaker opens after repeated failures to protect resources.
- **Database Connection Saturation:** High concurrent traffic exhausted the database connection pool. Monitor connection usage and tune Hikari pool size / DB connection limits.

## 6. Service Routing & Connectivity

```bash
kubectl get svc,endpoints -n country-info
```

**Common Causes:**
- If endpoints list is `<none>`, check whether pods are running and passing readiness probes.
- Verify pod labels match the Service selector (`app: country-info-st-v1`).