# 🚀 Quick Start Deployment Guide

## Step-by-Step Deployment

### 1️⃣ **Deploy Backend to Railway** (Recommended - Easiest)

1. **Push code to GitHub**
   ```bash
   git add .
   git commit -m "Prepare for deployment"
   git push origin main
   ```

2. **Go to Railway** (https://railway.app)
   - Sign up with GitHub
   - Click "New Project" → "Deploy from GitHub repo"
   - Select your backend repository

3. **Add MySQL Database**
   - Click "New" → "Database" → "Add MySQL"
   - Railway creates database automatically

4. **Set Environment Variables**
   - Go to your service → Variables tab
   - Add these variables:
     ```
     SPRING_PROFILES_ACTIVE=production
     SPRING_DATASOURCE_URL=<from MySQL service>
     SPRING_DATASOURCE_USERNAME=<from MySQL service>
     SPRING_DATASOURCE_PASSWORD=<from MySQL service>
     JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
     JWT_EXPIRATION=86400000
     CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
     ```

5. **Deploy**
   - Railway auto-deploys
   - Get your backend URL: `https://your-project.railway.app`

---

### 2️⃣ **Deploy Frontend to Vercel**

1. **Update Frontend API URL**
   - Find API configuration in your frontend code
   - Change from `http://localhost:8080` to your Railway URL
   - Example: `https://your-project.railway.app/api`

2. **Push to GitHub**
   ```bash
   git add .
   git commit -m "Update API URL for production"
   git push origin main
   ```

3. **Deploy to Vercel**
   - Go to https://vercel.com
   - Sign up with GitHub
   - Click "Add New Project"
   - Select your frontend repository
   - Click "Deploy"
   - Get your frontend URL: `https://your-project.vercel.app`

4. **Update Backend CORS**
   - Go back to Railway
   - Update `CORS_ALLOWED_ORIGINS` variable:
     ```
     CORS_ALLOWED_ORIGINS=https://your-project.vercel.app,http://localhost:5173
     ```
   - Redeploy backend

---

### 3️⃣ **Test Everything**

1. **Test Backend**
   ```
   GET https://your-project.railway.app/api/auth/test
   ```
   Should return: `"Backend is running!"`

2. **Test Frontend**
   - Visit: `https://your-project.vercel.app`
   - Try signing up
   - Try logging in

3. **Check Browser Console**
   - Open DevTools (F12)
   - Check for CORS errors
   - Verify API calls are working

---

## 📝 Environment Variables Summary

### Backend (Railway)
```
SPRING_PROFILES_ACTIVE=production
SPRING_DATASOURCE_URL=<from MySQL>
SPRING_DATASOURCE_USERNAME=<from MySQL>
SPRING_DATASOURCE_PASSWORD=<from MySQL>
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION=86400000
CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app
```

### Frontend (Vercel) - Optional
```
VITE_API_URL=https://your-backend.railway.app/api
```

---

## 🆘 Common Issues

### CORS Error
- Update `CORS_ALLOWED_ORIGINS` in Railway with your Vercel URL
- Include both production and local URLs

### Database Connection Failed
- Check MySQL service is running in Railway
- Verify connection string format
- Ensure credentials are correct

### Backend Won't Start
- Check Railway logs
- Verify all environment variables are set
- Ensure JAR file is built correctly

---

## ✅ Deployment Checklist

- [ ] Backend code pushed to GitHub
- [ ] Backend deployed to Railway
- [ ] MySQL database added in Railway
- [ ] Environment variables configured
- [ ] Backend URL obtained
- [ ] Frontend API URL updated
- [ ] Frontend deployed to Vercel
- [ ] CORS updated with frontend URL
- [ ] Test endpoints working
- [ ] Frontend can connect to backend

---

## 🔗 Your URLs

- **Backend**: `https://your-project.railway.app`
- **Frontend**: `https://your-project.vercel.app`
- **Database**: Managed by Railway

---

For detailed instructions, see `DEPLOYMENT_GUIDE.md`
