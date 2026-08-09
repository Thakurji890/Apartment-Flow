cat << 'INNER' > insert.txt
                if (state.recurringBills.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Upcoming Bills", onActionClick = { /* Navigate to bills list */ })
                    }
                    items(state.recurringBills.take(3)) { bill ->
                        RecurringBillItem(bill = bill, onClick = { /* Navigate to bill details */ })
                    }
                }
INNER
sed -i '180r insert.txt' app/src/main/java/com/example/feature/dashboard/presentation/dashboard/HomeDashboardScreen.kt
