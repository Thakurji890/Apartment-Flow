with open("app/src/main/java/com/example/data/ApartmentRepository.kt", "r") as f:
    text = f.read()

target = """                db.collection("apartments").document(apartmentId)
                    .update("memberIds", updatedMembers)
                    .addOnSuccessListener {"""

replacement = """                db.collection("apartments").document(apartmentId)
                    .update("memberIds", updatedMembers)
                    .addOnSuccessListener {
                        // Notify existing members
                        val newMemberName = auth.currentUser?.displayName ?: "A new member"
                        memberIds.forEach { existingMemberId ->
                            val docRef = db.collection("apartments").document(apartmentId)
                                .collection("notifications").document()
                            val newNotification = Notification(
                                id = docRef.id,
                                recipientId = existingMemberId,
                                title = "New Roommate",
                                message = "$newMemberName joined the apartment!",
                                timestamp = System.currentTimeMillis(),
                                read = false
                            )
                            docRef.set(newNotification)
                        }
"""

text = text.replace(target, replacement)

with open("app/src/main/java/com/example/data/ApartmentRepository.kt", "w") as f:
    f.write(text)
