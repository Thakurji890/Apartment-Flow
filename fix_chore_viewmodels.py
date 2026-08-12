import re

def fix_file(path):
    with open(path, "r") as f:
        content = f.read()

    # Imports
    content = content.replace(
        "import com.example.feature.apartment.domain.model.Member",
        "import com.example.feature.apartment.domain.model.ApartmentMember"
    )
    
    # State member type
    content = content.replace("val availableMembers: List<Member>", "val availableMembers: List<ApartmentMember>")
    content = content.replace("val members: List<Member>", "val members: List<ApartmentMember>")
    content = content.replace("val assignee: Member?", "val assignee: ApartmentMember?")
    content = content.replace("val creator: Member?", "val creator: ApartmentMember?")
    content = content.replace("val completedBy: Member?", "val completedBy: ApartmentMember?")

    # Repository logic
    if "AddEdit" in path:
        content = re.sub(
            r"val apartment = apartmentRepository\.getApartment\(apartmentId\)\s*if \(apartment != null\) \{\s*_state\.value = _state\.value\.copy\(availableMembers = apartment\.members\)\s*\}",
            r"apartmentRepository.getApartmentMembers(apartmentId).collect { result -> if (result is com.example.core.util.Resource.Success) { _state.value = _state.value.copy(availableMembers = result.data ?: emptyList()) } }",
            content
        )
    elif "Details" in path:
        content = re.sub(
            r"val apartment = apartmentRepository\.getApartment\(apartmentId\)\s*val assignee = apartment\?\.members\?\.find \{ it\.userId == chore\.assignedTo \}\s*val creator = apartment\?\.members\?\.find \{ it\.userId == chore\.createdBy \}\s*val completedBy = chore\.completedBy\?\.let \{ id -> apartment\?\.members\?\.find \{ it\.userId == id \} \}",
            r"apartmentRepository.getApartmentMembers(apartmentId).collect { result -> if (result is com.example.core.util.Resource.Success) { val members = result.data ?: emptyList(); val assignee = members.find { it.userId == chore.assignedTo }; val creator = members.find { it.userId == chore.createdBy }; val completedBy = chore.completedBy?.let { id -> members.find { it.userId == id } }; _state.value = _state.value.copy(assignee = assignee, creator = creator, completedBy = completedBy) } }",
            content
        )
    elif "FairnessSummary" in path:
        content = re.sub(
            r"val apartment = apartmentRepository\.getApartment\(apartmentId\)\s*if \(apartment != null\) \{\s*_state\.value = _state\.value\.copy\(members = apartment\.members\)\s*\}",
            r"apartmentRepository.getApartmentMembers(apartmentId).collect { result -> if (result is com.example.core.util.Resource.Success) { _state.value = _state.value.copy(members = result.data ?: emptyList()) } }",
            content
        )

    with open(path, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/example/feature/chore/presentation/add_edit/AddEditChoreViewModel.kt")
fix_file("app/src/main/java/com/example/feature/chore/presentation/details/ChoreDetailsViewModel.kt")
fix_file("app/src/main/java/com/example/feature/chore/presentation/summary/FairnessSummaryViewModel.kt")
