import re

with open("app/src/main/java/com/example/navigation/Screen.kt", "r") as f:
    content = f.read()

# I see a double "PurchaseItem" and an extra "}". Let's fix it by rewriting it based on the original.
content = content.replace("""    }
    object PurchaseItem : Screen("purchase_item/{apartmentId}/{listId}/{itemId}") {
        fun createRoute(apartmentId: String, listId: String, itemId: String) = 
            "purchase_item/$apartmentId/$listId/$itemId"
    }
}    object ShoppingLists""", """    object ShoppingLists""")

content = content.replace("    }\n}\n", "    }\n")

with open("app/src/main/java/com/example/navigation/Screen.kt", "w") as f:
    f.write(content + "\n}\n")
