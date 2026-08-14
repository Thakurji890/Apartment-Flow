import re

with open("app/src/main/AndroidManifest.xml", "r") as f:
    content = f.read()

# Add permissions
permissions = """    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />"""
content = content.replace('<uses-permission android:name="android.permission.INTERNET" />', permissions)

# Add FirebaseMessagingService
fcm_service = """
        <service
            android:name=".feature.notification.data.service.ApartmentFlowMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
"""
# Insert before </application>
content = content.replace("    </application>", fcm_service + "\n    </application>")

with open("app/src/main/AndroidManifest.xml", "w") as f:
    f.write(content)
