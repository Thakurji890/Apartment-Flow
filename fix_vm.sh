awk '
/val flow3 = useCases.getRecentActivity/ {
    print "                val flow3 = combine("
    print "                    useCases.getRecentActivity(apartmentId, 10),"
    print "                    useCases.getRecurringBills(apartmentId)"
    print "                ) { acts, bills -> Pair(acts, bills) }"
    next
}
/val flow4 = useCases.getRecurringBills/ { next }
/combine\(flow1, flow2, flow3, flow4\)/ {
    print "                combine(flow1, flow2, flow3) { t1, t2, t3 ->"
    next
}
/val \(expRes, setRes, balRes\) = t1/ {
    print $0
    next
}
/val \(debtRes, memRes, recExpRes\) = t2/ {
    print $0
    print "                    val (activities, recBillsRes) = t3"
    next
}
{ print }
' app/src/main/java/com/example/feature/dashboard/presentation/dashboard/DashboardViewModel.kt > temp.kt && mv temp.kt app/src/main/java/com/example/feature/dashboard/presentation/dashboard/DashboardViewModel.kt
