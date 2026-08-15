import re

with open("app/src/main/java/com/example/feature/analytics/presentation/AnalyticsViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("val members = memRes.data ?: emptyList()", "val members = (memRes as? Resource.Success)?.data ?: emptyList()")
content = content.replace("summary = sumRes.data ?: AnalyticsSummary()", "summary = (sumRes as? Resource.Success)?.data ?: AnalyticsSummary()")
content = content.replace("categorySpending = catRes.data ?: emptyList()", "categorySpending = (catRes as? Resource.Success)?.data ?: emptyList()")
content = content.replace("trends = trendRes.data ?: emptyList()", "trends = (trendRes as? Resource.Success)?.data ?: emptyList()")

with open("app/src/main/java/com/example/feature/analytics/presentation/AnalyticsViewModel.kt", "w") as f:
    f.write(content)
