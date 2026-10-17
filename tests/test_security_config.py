import unittest
import xml.etree.ElementTree as ET
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
ANDROID = "http://schemas.android.com/apk/res/android"

class FluidTrackerSecurityTests(unittest.TestCase):
    def test_backup_disabled_and_widget_intake_receiver_private(self):
        tree = ET.parse(ROOT / "app/src/main/AndroidManifest.xml")
        app = tree.getroot().find("application")
        self.assertEqual(app.attrib.get(f"{{{ANDROID}}}allowBackup"), "false")
        receivers = app.findall("receiver")
        private = [r for r in receivers if r.attrib.get(f"{{{ANDROID}}}name") == ".AddWaterWidgetReceiver"]
        self.assertEqual(len(private), 1)
        self.assertEqual(private[0].attrib.get(f"{{{ANDROID}}}exported"), "false")
        for receiver in receivers:
            if receiver is not private[0]:
                for action in receiver.findall(".//action"):
                    self.assertNotIn(action.attrib.get(f"{{{ANDROID}}}name", ""), {
                        "com.smmiqbal.fluidtracker.ACTION_ADD_WATER",
                        "com.smmiqbal.fluidtracker.ACTION_ADD_WATER_ORB",
                        "com.smmiqbal.fluidtracker.ACTION_ADD_WATER_PILL",
                    })

    def test_release_signing_has_no_literal_password_fallbacks(self):
        gradle = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn("FLUID_RELEASE_STORE_PASSWORD", gradle)
        self.assertIn("FLUID_RELEASE_KEY_PASSWORD", gradle)
        self.assertNotRegex(gradle, r'(?:storePassword|keyPassword)\s*=\s*"[^"\n]+"')
        self.assertIn("missingReleaseSigningInputs", gradle)

if __name__ == "__main__":
    unittest.main()
