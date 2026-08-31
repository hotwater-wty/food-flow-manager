FROM nginx:stable-alpine

COPY apps/customer/dist /usr/share/nginx/html/customer
COPY apps/admin/dist /usr/share/nginx/html/admin
COPY deploy/nginx/compose.conf /etc/nginx/conf.d/default.conf

HEALTHCHECK --interval=5s --timeout=3s --start-period=5s --retries=6 \
  CMD wget -q --spider --header='Host: customer.localhost' http://127.0.0.1/ || exit 1
