#!/usr/bin/env bash
set -euo pipefail

image_ref="${1:-${IMAGE_REF:-}}"
push_image="${PUSH_IMAGE:-true}"

if [[ -z "$image_ref" ]]; then
  echo "Usage: IMAGE_REF=<image:tag> [PUSH_IMAGE=false] $0" >&2
  exit 2
fi

if [[ "$push_image" != "true" && "$push_image" != "false" ]]; then
  echo "PUSH_IMAGE must be 'true' or 'false'." >&2
  exit 2
fi

kubectl apply -f k8s/namespace.yaml
if ! kubectl get secret country-info-secret -n country-info >/dev/null 2>&1; then
  echo "Required secret 'country-info-secret' is missing in namespace 'country-info'. See docs/kubernetes-deployment.md." >&2
  exit 1
fi

docker build -t "$image_ref" .
if [[ "$push_image" == "true" ]]; then
  docker push "$image_ref"
fi

manifest_dir="$(mktemp -d)"
trap 'rm -rf "$manifest_dir"' EXIT
cp k8s/configmap.yaml k8s/service.yaml k8s/autoscaling.yaml k8s/pdb.yaml "$manifest_dir/"
sed "s|country-info-st-v1:1.0.0|${image_ref}|g" k8s/deployment.yaml > "$manifest_dir/deployment.yaml"

kubectl apply -n country-info -f "$manifest_dir"
kubectl rollout status deployment/country-info-st-v1 -n country-info --timeout=180s