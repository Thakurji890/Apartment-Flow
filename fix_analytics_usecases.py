import re

with open("app/src/main/java/com/example/feature/analytics/domain/usecase/AnalyticsUseCases.kt", "r") as f:
    content = f.read()

content = content.replace("val expenses = expRes.data ?: emptyList()", "val expenses = (expRes as? Resource.Success)?.data ?: emptyList()")
content = content.replace("val settlements = setRes.data ?: emptyList()", "val settlements = (setRes as? Resource.Success)?.data ?: emptyList()")
content = content.replace("val bills = billRes.data ?: emptyList()", "val bills = (billRes as? Resource.Success)?.data ?: emptyList()")

content = content.replace("val expenses = res.data ?: emptyList()", "val expenses = (res as? Resource.Success)?.data ?: emptyList()")

content = content.replace("val members = memRes.data ?: emptyList()", "val members = (memRes as? Resource.Success)?.data ?: emptyList()")

with open("app/src/main/java/com/example/feature/analytics/domain/usecase/AnalyticsUseCases.kt", "w") as f:
    f.write(content)
