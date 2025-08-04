# 🔐 SECURITY NOTICE - Firebase Configuration

## ⚠️ CRITICAL SECURITY REQUIREMENT

**NEVER COMMIT `google-services.json` TO VERSION CONTROL**

### 🚨 Why This Matters
The `google-services.json` file contains:
- Firebase API keys
- Project credentials  
- Service configuration
- Potentially sensitive project information

### ✅ Proper Setup Process

1. **Download your `google-services.json`** from Firebase Console
2. **Place it in `/app/` directory** (it's already in .gitignore)
3. **Never share this file** in public repositories
4. **Use template file** for team reference

### 📋 Template Available
- Use `app/google-services.json.template` as reference
- Replace `YOUR_*` placeholders with actual values
- Keep template updated, but never expose real keys

### 🔒 For Production Deployment
- Store `google-services.json` in secure CI/CD environment variables
- Use different Firebase projects for dev/staging/prod
- Rotate API keys periodically

### 🚨 If Compromised
If Firebase keys are accidentally exposed:
1. **Immediately regenerate API keys** in Firebase Console
2. **Update all environments** with new keys
3. **Review access logs** for unauthorized usage
4. **Notify team** of security incident

---
**Remember**: Security is everyone's responsibility! 🛡️
