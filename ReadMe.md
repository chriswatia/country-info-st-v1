# Country Info Service (`country-info-st-v1`)

Spring Boot REST API service for managing country information. It fetches country details from an external SOAP service and stores them in MySQL.

---

## Prerequisites

- **Java 21**
- **Maven 3.9+**
- **MySQL 8.0+**
- **Docker** (optional)
- **Kubernetes** (optional)

---

## Running Locally

Set your database environment variables and start the service:

```bash
export DB_CONNECTION_STRING="jdbc:mysql://<database_host>:<port>/<database_name>"
export DB_USERNAME="<your_username>"
export DB_PASSWORD="<your_password>"
./mvnw spring-boot:run
```

On Windows (PowerShell):
```powershell
$env:DB_CONNECTION_STRING="jdbc:mysql://<database_host>:<port>/<database_name>"
$env:DB_USERNAME="<your_username>"
$env:DB_PASSWORD="<your_password>"
.\mvnw.cmd spring-boot:run
```

---

## Docker

### Build Image
```bash
docker build -t country-info-st-v1:1.0 .
```

### Run Container
```bash
docker run -d \
  --name country-info \
  -p 8080:8080 \
  -e DB_CONNECTION_STRING="jdbc:mysql://<database_host>:3306/<database_name>" \
  -e DB_USERNAME="<your_username>" \
  -e DB_PASSWORD="<your_password>" \
  country-info-st-v1:1.0
```

### View Logs
```bash
docker logs -f country-info
```

---

## Testing

### Automated Tests

Run the test suite with Maven:
```bash
./mvnw test
```

### Interactive API Docs (Swagger UI)

When the service is running, explore and test the endpoints directly in your browser:
- **Swagger UI:** `http://localhost:8080/country-info-st-v1/swagger-ui.html`
- **OpenAPI JSON Docs:** `http://localhost:8080/country-info-st-v1/api-docs`

### REST Endpoints

Base path: `http://localhost:8080/country-info-st-v1/api/v1`

#### Fetch Country Info from SOAP & Save to DB
```bash
curl -X POST http://localhost:8080/country-info-st-v1/api/v1/getCountryInfo \
  -H "Content-Type: application/json" \
  -d '{"name": "kenya"}'
```

#### Fetch Countries (with Pagination)
```bash
# Default pagination (page 0, size 10)
curl -X GET http://localhost:8080/country-info-st-v1/api/v1/countries

# Custom page and page size
curl -X GET "http://localhost:8080/country-info-st-v1/api/v1/countries?page=0&size=5"
```

#### Fetch Country by ID
```bash
curl -X GET http://localhost:8080/country-info-st-v1/api/v1/countries/1
```

#### Update Country
```bash
curl -X PUT http://localhost:8080/country-info-st-v1/api/v1/countries \
  -H "Content-Type: application/json" \
  -d '{
    "isoCode": "KE",
    "name": "Kenya",
    "capitalCity": "Nairobi",
    "phoneCode": "254",
    "continentCode": "AF",
    "currencyISOCode": "KES",
    "countryFlag": "http://www.oorsprong.org/WebSamples.CountryInfo/Flags/Kenya.jpg",
    "languages": [
      {
        "isoCode": "swa",
        "name": "Swahili"
      },
      {
        "isoCode": "eng",
        "name": "English"
      }
    ]
  }'
```

#### Delete Country
```bash
curl -X DELETE http://localhost:8080/country-info-st-v1/api/v1/countries/1
```

---

## Health & Monitoring

- **Health Probe:** `http://localhost:8080/country-info-st-v1/actuator/health`
- **Prometheus Metrics:** `http://localhost:8080/country-info-st-v1/actuator/prometheus`

---

## Kubernetes

Deployment and operations guides:
- [Kubernetes Deployment](docs/kubernetes-deployment.md) — Setup, manifests, autoscaling, and rollback/cleanup.
- [Kubernetes Troubleshooting](docs/kubernetes-troubleshooting.md) — Debugging pod startup, probes, connectivity, and performance.

### Quick Start
```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/secret.yaml
IMAGE_REF=country-info-st-v1:1.0.0 PUSH_IMAGE=false bash scripts/deploy-k8s.sh

# Access service
kubectl port-forward -n country-info svc/country-info-st-v1 8080:80
```
