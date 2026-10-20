# Ancient Content Reader

A simple Android reader for ancientcontent.com. It loads the public RSS feed, shows the posts as cards, and keeps a copy on the phone for offline reading.

## What it does

- Latest tab with cover image, title, summary and date
- Reader screen with a clean layout and automatic dark mode
- Saved tab for posts the reader hearts
- Offline reading for post text. Images show offline once they have been viewed
- Pull down to refresh
- Share button and an "Open on Substack" button
- Paid posts show only the preview that Substack puts in the feed, with a button that opens the full post on Substack

## Build the APK

1. Install Android Studio (Ladybug or newer).
2. Choose File > Open and select this folder. Let Gradle sync finish.
3. Connect a phone with USB debugging on, or start an emulator, then press Run.
4. For a file to share, choose Build > Build Bundle(s) / APK(s) > Build APK(s).
   The debug APK is created at app/build/outputs/apk/debug/app-debug.apk

### Build without installing anything

1. Create a free repository on github.com and upload the contents of this folder, including the hidden .github folder.
2. Open the Actions tab and wait for the Build APK run to finish (a few minutes). Use "Run workflow" to start it by hand.
3. Open the finished run and download ancient-content-apk from the Artifacts section. The zip holds app-debug.apk.

For the Play Store, use Build > Generate Signed App Bundle and keep the keystore file safe.

## Project layout

- data/FeedParser.kt reads the RSS feed
- data/FeedRepository.kt downloads the feed and stores posts in a JSON file on the phone
- data/BookmarkStore.kt remembers saved posts
- ui/ListScreen.kt and ui/ReaderScreen.kt hold the two screens
- ui/FeedViewModel.kt connects the data to the screens

## Next steps

- Turkish and English interface toggle
- Push notification for new posts
- Timeline map of ancient civilizations
- Live TV tab with an HLS player
- App icon and splash screen artwork
