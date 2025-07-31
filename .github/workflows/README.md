# EuroLeague App - CI/CD Configuration

## 🚀 GitHub Actions Workflows

This directory contains the complete CI/CD pipeline configuration for the EuroLeague Android application.

### 📁 Workflow Files

#### 1. **ci.yml** - Main CI Pipeline
- **Purpose**: Comprehensive build, test, and quality analysis
- **Triggers**: Push to main/develop, PRs
- **Jobs**:
  - 🏗️ Build & Test
  - 🔍 Code Quality Analysis
  - 🔒 Security Scan
  - 📬 Build Summary

#### 2. **test-suite.yml** - Comprehensive Testing
- **Purpose**: Extensive testing across multiple API levels
- **Triggers**: Push, PRs, scheduled runs
- **Jobs**:
  - 🧪 Unit Tests (API 28, 29, 30, 31, 33)
  - 🤖 Instrumentation Tests (Android UI)
  - 📊 Coverage Analysis
  - 🎯 Test Quality Metrics

#### 3. **pr-validation.yml** - Pull Request Validation
- **Purpose**: Fast validation for PRs
- **Triggers**: PR events
- **Jobs**:
  - 🔍 PR Validation
  - ⚡ Quick Build Check
  - 🧪 Essential Tests
  - 🚦 Code Quality Gate

### 🎯 Test Coverage

Our test suite covers **105+ unit tests** across:

- **Data Layer**: Repository pattern, mappers, entities
- **Domain Layer**: Business logic and models  
- **Presentation Layer**: ViewModels and navigation
- **Utils**: Image handling, data processing

### 📊 Quality Gates

#### Build Requirements
- ✅ Clean compilation
- ✅ No critical lint errors
- ✅ Core tests passing
- ✅ Security scan clear

#### PR Requirements
- ✅ Conventional commit title format
- ✅ Quick build success
- ✅ Essential tests passing
- ✅ Code quality checks

### 🛡️ Security & Quality

- **Dependency Check**: Automated vulnerability scanning
- **Static Analysis**: Code quality with Detekt
- **Coverage Reporting**: Jacoco + Codecov integration
- **Lint Analysis**: Android lint for best practices

### 🚀 Artifacts Generated

Each CI run produces:
- 📦 Debug APK builds
- 🧪 Test reports (HTML + XML)
- 📋 Lint analysis results
- 🔍 Code quality reports
- 🔒 Security scan results
- 📊 Coverage reports

### 🔄 Workflow Triggers

| Workflow | Push | PR | Schedule | Manual |
|----------|------|----|---------| -------|
| CI Pipeline | ✅ main/develop | ✅ | ❌ | ✅ |
| Test Suite | ✅ all branches | ✅ | ✅ Daily 6AM | ✅ |
| PR Validation | ❌ | ✅ | ❌ | ❌ |

### 🎨 Status Badges

Add these to your README.md:

```markdown
![CI Pipeline](https://github.com/lasecun/EurobasketMatchs/workflows/🚀%20CI%20Pipeline%20-%20Build%20&%20Test/badge.svg)
![Test Suite](https://github.com/lasecun/EurobasketMatchs/workflows/🧪%20Test%20Suite%20-%20Comprehensive%20Testing/badge.svg)
![PR Validation](https://github.com/lasecun/EurobasketMatchs/workflows/🔄%20Pull%20Request%20Validation/badge.svg)
```

### 💡 Best Practices

1. **Fast Feedback**: PR validation runs in ~10 minutes
2. **Parallel Execution**: Jobs run concurrently when possible
3. **Smart Caching**: Gradle dependencies cached for speed
4. **Comprehensive Coverage**: Multiple Android API levels tested
5. **Quality First**: Code quality gates prevent bad merges

### 🔧 Configuration

#### Required Secrets
- No additional secrets required for basic functionality
- Optional: `CODECOV_TOKEN` for enhanced coverage reporting

#### Environment Setup
- **JDK**: 17 (Temurin distribution)
- **Gradle**: Latest with build action caching
- **Android**: API levels 28-33 tested

---

**Last Updated**: July 31, 2025  
**Coverage**: 105+ unit tests across all layers  
**Performance**: PR validation ~10min, Full CI ~30min
