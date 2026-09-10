with open("app/src/main/java/com/example/ApartmentFlowApp.kt", "r") as f:
    content = f.read()

imports = """
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
"""

init_code = """
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )
    }
"""

if "FirebaseAppCheck" not in content:
    content = content.replace("import javax.inject.Inject", "import javax.inject.Inject" + imports)
    content = content.replace("class ApartmentFlowApp : Application(), Configuration.Provider {", "class ApartmentFlowApp : Application(), Configuration.Provider {" + init_code)

with open("app/src/main/java/com/example/ApartmentFlowApp.kt", "w") as f:
    f.write(content)
