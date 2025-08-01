# 📊 Firebase Analytics & Crashlytics Implementation Guide

## 🎯 Overview

Esta implementación proporciona un sistema completo de analytics y crash reporting optimizado para aplicaciones móviles deportivas, siguiendo las mejores prácticas de SEO móvil y UX analytics.

## 🏗️ Architecture

```
📱 UI Layer
    ↓
🎯 EventDispatcher (Async processing)
    ↓
📊 AnalyticsManager (Business logic)
    ↓
🔥 Firebase Services (Analytics + Crashlytics)
```

## 🚀 Quick Start

### 1. 📱 Screen Tracking

```kotlin
@Composable
fun TeamDetailScreen(teamCode: String) {
    val screenTracker = hiltViewModel<ScreenTracker>()
    
    screenTracker.TrackScreen(
        screenName = AnalyticsManager.SCREEN_TEAM_DETAIL,
        screenClass = "TeamDetailScreen"
    ) {
        // Your screen content
        TeamDetailContent(teamCode = teamCode)
    }
}
```

### 2. 🎯 Event Tracking

```kotlin
@Composable
fun TeamCard(team: Team) {
    val eventDispatcher = hiltViewModel<EventDispatcher>()
    
    Card(
        onClick = {
            // Track team view event
            eventDispatcher.dispatch(
                AnalyticsEvent.TeamContentEvent(
                    action = TeamAction.VIEWED,
                    teamCode = team.code,
                    teamName = team.name,
                    source = "team_list"
                )
            )
            // Navigate to team detail
        }
    ) {
        // Card content
    }
}
```

### 3. 🏀 Match Events

```kotlin
// Track match viewed
eventDispatcher.dispatch(
    AnalyticsEvent.MatchContentEvent(
        action = MatchAction.VIEWED,
        matchId = match.id,
        homeTeam = match.homeTeam.name,
        awayTeam = match.awayTeam.name,
        matchStatus = match.status,
        isLive = match.isLive,
        source = "calendar"
    )
)

// Track live score check
eventDispatcher.dispatch(
    AnalyticsEvent.MatchContentEvent(
        action = MatchAction.LIVE_SCORE_CHECKED,
        matchId = match.id,
        homeTeam = match.homeTeam.name,
        awayTeam = match.awayTeam.name,
        matchStatus = match.status,
        isLive = true
    )
)
```

### 4. ⚡ Performance Tracking

```kotlin
// Track image load performance
fun trackImageLoad(imageType: String, startTime: Long, success: Boolean) {
    val loadTime = System.currentTimeMillis() - startTime
    eventDispatcher.dispatch(
        AnalyticsEvent.PerformanceEvent(
            type = PerformanceType.IMAGE_LOAD,
            durationMs = loadTime,
            success = success,
            details = mapOf("imageType" to imageType)
        )
    )
}

// Track API call performance
suspend fun <T> trackApiCall(endpoint: String, call: suspend () -> T): T {
    val startTime = System.currentTimeMillis()
    return try {
        val result = call()
        val duration = System.currentTimeMillis() - startTime
        eventDispatcher.dispatch(
            AnalyticsEvent.PerformanceEvent(
                type = PerformanceType.API_CALL,
                durationMs = duration,
                success = true,
                details = mapOf("endpoint" to endpoint)
            )
        )
        result
    } catch (e: Exception) {
        val duration = System.currentTimeMillis() - startTime
        eventDispatcher.dispatch(
            AnalyticsEvent.PerformanceEvent(
                type = PerformanceType.API_CALL,
                durationMs = duration,
                success = false,
                details = mapOf(
                    "endpoint" to endpoint,
                    "error" to e.message
                )
            )
        )
        throw e
    }
}
```

### 5. 🔍 Search & Discovery

```kotlin
// Track search
fun onSearchPerformed(query: String, results: List<Any>) {
    eventDispatcher.dispatch(
        AnalyticsEvent.SearchEvent(
            query = query,
            category = "teams", // or "players", "matches"
            resultCount = results.size
        )
    )
}

// Track filter usage
fun onFilterApplied(filterType: String, filterValue: String, resultCount: Int) {
    eventDispatcher.dispatch(
        AnalyticsEvent.FilterEvent(
            filterType = filterType,
            filterValue = filterValue,
            resultCount = resultCount,
            screen = "team_list"
        )
    )
}
```

### 6. 💝 User Engagement

```kotlin
// Track favorites
fun onAddToFavorites(contentType: String, contentId: String, contentName: String) {
    eventDispatcher.dispatch(
        AnalyticsEvent.FavoriteEvent(
            action = FavoriteAction.ADDED,
            contentType = contentType,
            contentId = contentId,
            contentName = contentName
        )
    )
}

// Track sharing
fun onShareContent(contentType: String, contentId: String, method: String) {
    eventDispatcher.dispatch(
        AnalyticsEvent.ShareEvent(
            contentType = contentType,
            contentId = contentId,
            shareMethod = method
        )
    )
}
```

### 7. 🚨 Error Tracking

```kotlin
// Track non-critical errors
fun trackError(error: Exception, screen: String, action: String? = null) {
    eventDispatcher.dispatch(
        AnalyticsEvent.ErrorEvent(
            errorType = error::class.simpleName ?: "UnknownError",
            errorMessage = error.message ?: "No message",
            screen = screen,
            action = action,
            severity = ErrorSeverity.MEDIUM
        )
    )
}

// Track critical errors (will also go to Crashlytics)
fun trackCriticalError(error: Exception, screen: String) {
    eventDispatcher.dispatch(
        AnalyticsEvent.ErrorEvent(
            errorType = error::class.simpleName ?: "CriticalError",
            errorMessage = error.message ?: "Critical error occurred",
            screen = screen,
            severity = ErrorSeverity.CRITICAL
        )
    )
}
```

## 📊 Key Metrics Tracked

### 🎯 User Engagement
- **Screen Views**: Automatic tracking of all screen navigation
- **Session Duration**: Time spent on each screen
- **User Journey**: Navigation patterns and funnels
- **Content Interaction**: Taps, scrolls, favorites, shares

### 🏀 Basketball-Specific
- **Match Engagement**: Views, live score checks, favorites
- **Team Interest**: Team views, roster access, stats viewing
- **Player Discovery**: Player profile views, stats access
- **Content Consumption**: Match details, standings, calendar usage

### ⚡ Performance
- **App Startup Time**: Cold start performance
- **Screen Load Times**: Individual screen performance
- **Image Load Performance**: Player/team image loading
- **API Response Times**: Backend performance monitoring

### 🔍 Discovery & Search
- **Search Usage**: Query patterns and result interactions
- **Filter Usage**: How users narrow down content
- **Content Discovery**: How users find teams, players, matches

### 💾 Data Usage
- **Sync Performance**: Data refresh times and success rates
- **Offline Usage**: App usage without internet
- **Cache Performance**: Local data access patterns

## 🎯 SEO & Marketing Benefits

### 📱 Mobile App SEO
- **User Behavior Insights**: Understand how users navigate your app
- **Content Performance**: Identify most engaging content types
- **Feature Adoption**: Track which features users love most
- **Retention Patterns**: Understand what keeps users coming back

### 📊 Growth Optimization
- **Funnel Analysis**: Identify drop-off points in user journeys
- **A/B Testing Data**: Performance data for feature experiments
- **User Segmentation**: Understand different user types and preferences
- **Content Strategy**: Data-driven content and feature decisions

### 🎭 UX Optimization
- **Performance Bottlenecks**: Identify slow screens and operations
- **Error Patterns**: Understand and fix common user issues
- **Navigation Optimization**: Improve user flow based on actual usage
- **Feature Prioritization**: Focus development on high-impact features

## 🔧 Configuration

### Firebase Console Setup
1. Create Firebase project at https://console.firebase.google.com
2. Add Android app with package name `es.itram.basketmatch`
3. Download `google-services.json` and replace the placeholder
4. Enable Analytics and Crashlytics in Firebase console

### Custom Dimensions (Analytics)
Set up these custom dimensions in Firebase Analytics:
- `favorite_team_code`: User's favorite team
- `user_type`: New vs returning user patterns
- `content_preference`: Team, player, or match focus

### Crashlytics Configuration
- Automatic crash reporting enabled
- Custom log messages for debugging
- User ID tracking for support
- Custom keys for enhanced error context

## 🧪 Testing

### Debug Mode
```kotlin
// In debug builds, enable analytics debug view
if (BuildConfig.DEBUG) {
    FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true)
}
```

### Event Validation
Use Firebase DebugView to validate events:
1. Enable debug mode: `adb shell setprop debug.firebase.analytics.app es.itram.basketmatch`
2. View events in Firebase Console > Analytics > DebugView
3. Validate event parameters and user properties

## 📋 Best Practices

### 🎯 Event Naming
- Use consistent naming: `snake_case` for events and parameters
- Include context: `team_viewed` vs generic `content_viewed`
- Be specific: `live_score_checked` vs `score_viewed`

### 📊 Parameter Consistency
- Always include relevant IDs: `team_code`, `player_code`, `match_id`
- Add source context: where did the action originate
- Include success/failure status for operations

### ⚡ Performance
- Events are dispatched asynchronously to avoid UI blocking
- Failed analytics calls won't crash the app
- Minimal impact on app performance

### 🔒 Privacy
- No personally identifiable information (PII) is collected
- User IDs are anonymized
- Compliant with GDPR and privacy regulations

## 📈 Analytics Dashboard Setup

### Key KPIs to Monitor
1. **DAU/MAU**: Daily and monthly active users
2. **Session Duration**: Average time in app
3. **Screen Views per Session**: User engagement depth
4. **Retention Rates**: 1-day, 7-day, 30-day retention
5. **Feature Adoption**: % of users using key features
6. **Crash-free Sessions**: App stability metric

### Custom Reports
Create custom reports for:
- Team popularity ranking
- Most viewed players
- Peak usage times for live matches
- Geographic distribution of users
- Device and OS version adoption

This comprehensive analytics implementation provides deep insights into user behavior while maintaining excellent performance and privacy standards.
