#Use a Node.js image as the base
FROM node:18 as build
WORKDIR /app

#Copy package.json and install dependencies
COPY package*.json ./
RUN npm install

#Copy the rest of the app and build it
COPY . .
RUN npm run build

#Use Nginx to serve the React app
FROM nginx:stable-alpine
COPY --from=build /app/build /usr/share/nginx/html

#Expose port 80 for the React app
EXPOSE 80
