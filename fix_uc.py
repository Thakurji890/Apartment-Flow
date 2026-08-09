with open("app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt", "r") as f:
    lines = f.readlines()

# Remove anything after activities.sortedByDescending { it.timestamp }.take(limit)
for i, line in enumerate(lines):
    if "activities.sortedByDescending { it.timestamp }.take(limit)" in line:
        # Keep this line, then next line should be }, then }, then the getRecurringBills function, then }
        new_lines = lines[:i+1]
        new_lines.extend([
            "        }\n",
            "    }\n\n",
            "    fun getRecurringBills(apartmentId: String): Flow<Resource<List<RecurringBill>>> =\n",
            "        recurringBillRepository.getRecurringBills(apartmentId)\n",
            "}\n"
        ])
        with open("app/src/main/java/com/example/feature/dashboard/domain/usecase/DashboardUseCases.kt", "w") as f_out:
            f_out.writelines(new_lines)
        break
