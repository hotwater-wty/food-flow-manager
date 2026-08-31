FROM nginx:stable-alpine

COPY apps/customer/dist /usr/share/nginx/html/customer
COPY apps/admin/dist /usr/share/nginx/html/admin
COPY deploy/nginx/compose.conf /etc/nginx/conf.d/default.conf
