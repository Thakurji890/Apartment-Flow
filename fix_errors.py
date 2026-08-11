import os

def replace_in_file(filepath, old_text, new_text):
    with open(filepath, "r") as f:
        content = f.read()
    content = content.replace(old_text, new_text)
    with open(filepath, "w") as f:
        f.write(content)

replace_in_file("app/src/main/java/com/example/feature/shopping/presentation/add_edit_item/AddEditShoppingItemScreen.kt", "isLoading = state.isLoading", "enabled = !state.isLoading")
replace_in_file("app/src/main/java/com/example/feature/shopping/presentation/add_edit_list/AddEditShoppingListScreen.kt", "isLoading = state.isLoading", "enabled = !state.isLoading")
replace_in_file("app/src/main/java/com/example/feature/shopping/presentation/purchase_item/PurchaseItemScreen.kt", "isLoading = state.isLoading", "enabled = !state.isLoading")

replace_in_file("app/src/main/java/com/example/feature/shopping/presentation/add_edit_item/AddEditShoppingItemViewModel.kt", "user.id", "user.uid")
replace_in_file("app/src/main/java/com/example/feature/shopping/presentation/add_edit_list/AddEditShoppingListViewModel.kt", "user.id", "user.uid")
replace_in_file("app/src/main/java/com/example/feature/shopping/presentation/purchase_item/PurchaseItemViewModel.kt", "user?.id", "user?.uid")
replace_in_file("app/src/main/java/com/example/feature/shopping/domain/usecase/ConvertPurchaseToExpenseUseCase.kt", "currentUser.id", "currentUser.uid")
