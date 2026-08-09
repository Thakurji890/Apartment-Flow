awk '
/item \{/ {
    if (match($0, /SectionHeader\(title = "Recent Activity"\)/)) {
        print "                if (state.recurringBills.isNotEmpty()) {"
        print "                    item {"
        print "                        SectionHeader(title = \"Upcoming Bills\", onActionClick = { /* Navigate to bills list */ })"
        print "                    }"
        print "                    items(state.recurringBills.take(3)) { bill ->"
        print "                        RecurringBillItem(bill = bill, onClick = { /* Navigate to bill details */ })"
        print "                    }"
        print "                }"
        print ""
    }
}
{ print }
' app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt > temp.kt && mv temp.kt app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt
