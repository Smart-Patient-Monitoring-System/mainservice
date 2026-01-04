# Complete Deployment Guide - Smart Patient Monitoring System

## 📋 Table of Contents
1. [Local Development Setup](#local-development-setup)
2. [Frontend Deployment (Vercel)](#frontend-deployment-vercel)
3. [Backend Deployment Options](#backend-deployment-options)
4. [MySQL Database Hosting](#mysql-database-hosting)
5. [Configuration Updates](#configuration-updates)
6. [Testing the Deployment](#testing-the-deployment)

---

## 🏠 Local Development Setup

### Step 1: Run Backend Locally

1. **Start MySQL Database**
   ```bash
   # Make sure MySQL is running on your machine
   # Default: localhost:3306
   ```

2. **Update Database Credentials** (if needed)
   - Edit `src/main/resources/application.properties`
   - Update MySQL password if different from `venith123`

3. **Run in IntelliJ IDEA**
   - Open `MainserviceApplication.java`
   - Click the green ▶️ button
   - Wait for: `Started MainserviceApplication`
   - Backend runs on: `http://localhost:8080`

4. **Test Backend**
   - Open browser: `http://localhost:8080/api/auth/test`
   - Should see: `"Backend is running!"`

### Step 2: Run Frontend Locally

1. **Clone/Navigate to Frontend Repo**
   ```bash
   cd "D:\Lec\Group Project\GP\Final\Frontend"
   ```

2. **Install Dependencies**
   ```bash
   npm install
   ```

3. **Update API Endpoints** (if needed)
   - Check frontend code for API base URL
   - Should point to: `http://localhost:8080/api`

4. **Run Frontend**
   ```bash
   npm run dev
   ```
   - Frontend runs on: `http://localhost:5173` (or 3000)

---

## 🚀 Frontend Deployment (Vercel)

### Step 1: Prepare Frontend for Production

1. **Update API Base URL**
   - Find where API calls are made (usually in a config file or axios setup)
   - Change from `http://localhost:8080` to your backend URL
   - Example: `https://your-backend.railway.app/api` or `https://your-backend.onrender.com/api`

2. **Create Environment Variables File** (if needed)
   ```bash
   # Create .env.production in frontend root
   VITE_API_URL=https://your-backend-url.com/api
   ```

3. **Commit and Push to GitHub**
   ```bash
   git add .
   git commit -m "Prepare for production deployment"
   git push origin main
   ```

### Step 2: Deploy to Vercel

1. **Go to Vercel**
   - Visit: https://vercel.com
   - Sign up/Login with GitHub

2. **Import Project**
   - Click "Add New Project"
   - Select your frontend GitHub repository
   - Click "Import"

3. **Configure Project**
   - **Framework Preset**: Vite (or React/Vue based on your setup)
   - **Root Directory**: `./` (or your frontend folder if monorepo)
   - **Build Command**: `npm run build` (usually auto-detected)
   - **Output Directory**: `dist` (for Vite) or `build` (for Create React App)

4. **Add Environment Variables** (if using .env)
   - Go to Settings → Environment Variables
   - Add: `VITE_API_URL` = `https://your-backend-url.com/api`

5. **Deploy**
   - Click "Deploy"
   - Wait for build to complete
   - Your frontend will be live at: `https://your-project.vercel.app`

---

## 🔧 Backend Deployment Options

### Option 1: Railway (Recommended - Easiest)

#### Step 1: Prepare Backend

1. **Create `railway.json`** (optional)
   ```json
   {
     "$schema": "https://railway.app/railway.schema.json",
     "build": {
       "builder": "NIXPACKS"
     },
     "deploy": {
       "startCommand": "java -jar target/mainservice-0.0.1-SNAPSHOT.jar",
       "restartPolicyType": "ON_FAILURE",
       "restartPolicyMaxRetries": 10
     }
   }
   ```

2. **Create `Procfile`** (alternative)
   ```
   web: java -jar target/mainservice-0.0.1-SNAPSHOT.jar
   ```

3. **Update `pom.xml`** - Add packaging
   ```xml
   <packaging>jar</packaging>
   ```

#### Step 2: Deploy to Railway

1. **Go to Railway**
   - Visit: https://railway.app
   - Sign up with GitHub

2. **Create New Project**
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Choose your backend repository

3. **Add MySQL Database**
   - Click "New" → "Database" → "Add MySQL"
   - Railway will create a MySQL database
   - Note the connection details

4. **Configure Environment Variables**
   - Go to your service → Variables
   - Add these variables:
     ```
     SPRING_DATASOURCE_URL=jdbc:mysql://containers-us-west-XXX.railway.app:XXXX/railway
     SPRING_DATASOURCE_USERNAME=root
     SPRING_DATASOURCE_PASSWORD=your-password
     SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
     JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
     JWT_EXPIRATION=86400000
     PORT=8080
     ```

5. **Update `application.properties`** for Railway
   - Create `application-railway.properties`:
   ```properties
   spring.datasource.url=${SPRING_DATASOURCE_URL}
   spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
   spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
   spring.datasource.driver-class-name=${SPRING_DATASOURCE_DRIVER_CLASS_NAME}
   jwt.secret=${JWT_SECRET}
   jwt.expiration=${JWT_EXPIRATION}
   server.port=${PORT:8080}
   ```

6. **Deploy**
   - Railway will automatically build and deploy
   - Your backend URL: `https://your-project.railway.app`

---

### Option 2: Render

#### Step 1: Prepare Backend

1. **Create `render.yaml`**
   ```yaml
   services:
     - type: web
       name: mainservice
       env: java
       buildCommand: ./mvnw clean package -DskipTests
       startCommand: java -jar target/mainservice-0.0.1-SNAPSHOT.jar
       envVars:
         - key: SPRING_PROFILES_ACTIVE
           value: production
   ```

#### Step 2: Deploy to Render

1. **Go to Render**
   - Visit: https://render.com
   - Sign up with GitHub

2. **Create New Web Service**
   - Click "New" → "Web Service"
   - Connect your GitHub repository
   - Select your backend repo

3. **Configure Service**
   - **Name**: mainservice
   - **Environment**: Java
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -jar target/mainservice-0.0.1-SNAPSHOT.jar`

4. **Add Environment Variables**
   - Go to Environment section
   - Add all required variables (same as Railway)

5. **Add MySQL Database**
   - Click "New" → "PostgreSQL" (or use external MySQL)
   - Or use Render's MySQL addon if available

6. **Deploy**
   - Click "Create Web Service"
   - Your backend URL: `https://your-project.onrender.com`

---

### Option 3: Heroku (Free tier discontinued, but still works)

1. **Install Heroku CLI**
   ```bash
   # Download from: https://devcenter.heroku.com/articles/heroku-cli
   ```

2. **Create `Procfile`**
   ```
   web: java -jar target/mainservice-0.0.1-SNAPSHOT.jar
   ```

3. **Login and Deploy**
   ```bash
   heroku login
   heroku create your-app-name
   heroku addons:create cleardb:ignite  # MySQL database
   git push heroku main
   ```

---

## 🗄️ MySQL Database Hosting

### Option 1: Railway MySQL (Easiest with Railway Backend)

- Already included when you add MySQL database in Railway
- Connection string provided automatically
- Free tier: 512MB storage

### Option 2: PlanetScale (Recommended for Production)

1. **Sign Up**
   - Visit: https://planetscale.com
   - Sign up with GitHub

2. **Create Database**
   - Click "Create database"
   - Choose region
   - Note the connection string

3. **Get Connection Details**
   - Go to your database → "Connect"
   - Copy the connection string
   - Format: `mysql://username:password@host:port/database`

4. **Update Backend Configuration**
   - Use the connection string in your backend environment variables

### Option 3: Aiven MySQL (Free Tier Available)

1. **Sign Up**
   - Visit: https://aiven.io
   - Create account

2. **Create MySQL Service**
   - Click "Create service"
   - Select MySQL
   - Choose free tier
   - Get connection details

### Option 4: Free MySQL Hosting Services

- **db4free.net**: Free MySQL hosting
- **Freesqldatabase.com**: Free MySQL database
- **Remotemysql.com**: Free MySQL hosting

---

## ⚙️ Configuration Updates

### Step 1: Update Backend CORS

Update `SecurityConfig.java` to allow your Vercel frontend:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:5173",
        "http://localhost:3000",
        "https://your-frontend.vercel.app"  // Add your Vercel URL
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(Arrays.asList("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Step 2: Update application.properties for Production

Create `application-production.properties`:

```properties
# Server
server.port=${PORT:8080}

# Database (use environment variables)
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}

# CORS (handled in SecurityConfig)
```

### Step 3: Update Frontend API Configuration

In your frontend, update the API base URL:

**For Vite (if using environment variables):**
```javascript
// .env.production
VITE_API_URL=https://your-backend.railway.app/api

// In your API service file
const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';
```

**For React (Create React App):**
```javascript
// .env.production
REACT_APP_API_URL=https://your-backend.railway.app/api

// In your API service file
const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';
```

---

## 🧪 Testing the Deployment

### Step 1: Test Backend

1. **Test Health Endpoint**
   ```
   GET https://your-backend.railway.app/api/auth/test
   ```
   Should return: `"Backend is running!"`

2. **Test Signup**
   ```bash
   POST https://your-backend.railway.app/api/auth/signup
   Content-Type: application/json
   
   {
     "username": "testuser",
     "password": "password123",
     "email": "test@example.com",
     "name": "Test User",
     "role": "PATIENT"
   }
   ```

3. **Test Login**
   ```bash
   POST https://your-backend.railway.app/api/auth/login
   Content-Type: application/json
   
   {
     "username": "testuser",
     "password": "password123"
   }
   ```

### Step 2: Test Frontend

1. **Visit Your Vercel URL**
   - Open: `https://your-frontend.vercel.app`

2. **Test Login/Signup**
   - Try creating an account
   - Try logging in
   - Check browser console for errors

3. **Check Network Tab**
   - Open DevTools → Network
   - Verify API calls are going to your backend URL

---

## 📝 Complete Deployment Checklist

### Backend
- [ ] Code pushed to GitHub
- [ ] `application-production.properties` created
- [ ] CORS updated with frontend URL
- [ ] Environment variables configured in hosting platform
- [ ] Database connection string configured
- [ ] Backend deployed and accessible
- [ ] Test endpoints working

### Frontend
- [ ] Code pushed to GitHub
- [ ] API base URL updated to production backend
- [ ] Environment variables set in Vercel
- [ ] Frontend deployed to Vercel
- [ ] Frontend can communicate with backend
- [ ] CORS errors resolved

### Database
- [ ] MySQL database created
- [ ] Connection string obtained
- [ ] Database accessible from backend
- [ ] Tables created automatically (JPA ddl-auto=update)

---

## 🔗 Quick Links

- **Vercel**: https://vercel.com
- **Railway**: https://railway.app
- **Render**: https://render.com
- **PlanetScale**: https://planetscale.com
- **Aiven**: https://aiven.io

---

## 🆘 Troubleshooting

### Backend won't start
- Check environment variables are set correctly
- Verify database connection string
- Check logs in hosting platform

### CORS errors
- Update CORS configuration in `SecurityConfig.java`
- Ensure frontend URL is in allowed origins
- Check `Access-Control-Allow-Origin` header

### Database connection fails
- Verify connection string format
- Check database is accessible from hosting platform
- Ensure database credentials are correct

### Frontend can't connect to backend
- Verify backend URL is correct
- Check CORS configuration
- Ensure backend is running and accessible

---

## 💡 Pro Tips

1. **Use Environment Variables**: Never hardcode sensitive data
2. **Test Locally First**: Always test changes locally before deploying
3. **Monitor Logs**: Check hosting platform logs for errors
4. **Database Backups**: Set up regular backups for production
5. **SSL/HTTPS**: All platforms provide HTTPS by default
6. **Domain Names**: You can add custom domains to Vercel and Railway

Good luck with your deployment! 🚀
