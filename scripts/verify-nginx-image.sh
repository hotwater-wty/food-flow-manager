#!/bin/sh

set -eu

image_name="${1:-food-flow-manager-nginx:ci}"
container_name="food-flow-manager-nginx-ci"
host_port="${NGINX_VERIFY_PORT:-18080}"

cleanup() {
  docker rm -f "$container_name" >/dev/null 2>&1 || true
}

trap cleanup EXIT INT TERM
cleanup

docker run --rm "$image_name" nginx -t
docker run -d --name "$container_name" -p "127.0.0.1:${host_port}:80" "$image_name" >/dev/null

attempt=1
while [ "$attempt" -le 20 ]; do
  health_status="$(docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}missing{{end}}' "$container_name")"
  if [ "$health_status" = "healthy" ]; then
    break
  fi
  if [ "$health_status" = "unhealthy" ] || [ "$health_status" = "missing" ]; then
    docker logs "$container_name"
    echo "Nginx container health check failed: $health_status" >&2
    exit 1
  fi
  attempt=$((attempt + 1))
  sleep 1
done

if [ "$(docker inspect --format '{{.State.Health.Status}}' "$container_name")" != "healthy" ]; then
  docker logs "$container_name"
  echo "Nginx container did not become healthy in time" >&2
  exit 1
fi

customer_index="$(curl --fail --silent --show-error -H 'Host: customer.localhost' "http://127.0.0.1:${host_port}/")"
admin_index="$(curl --fail --silent --show-error -H 'Host: admin.localhost' "http://127.0.0.1:${host_port}/")"
customer_route="$(curl --fail --silent --show-error -H 'Host: customer.localhost' "http://127.0.0.1:${host_port}/menu")"
admin_route="$(curl --fail --silent --show-error -H 'Host: admin.localhost' "http://127.0.0.1:${host_port}/orders")"

printf '%s' "$customer_index" | grep -q '<title>膳畅管家</title>'
printf '%s' "$admin_index" | grep -q '<title>膳畅管家 · 商户端</title>'
[ "$customer_route" = "$customer_index" ]
[ "$admin_route" = "$admin_index" ]

echo "Nginx image is healthy; customer/admin hosts and SPA fallbacks passed."
