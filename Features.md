
#
# Advertise Closer in SF and NY and LA and CO (NOT Austin!! :P)                  <-- !!
#

[ ] Localized screenshots (NY, LA, SF)
[ ] "Pulses" of invites so masses of people join at the exact same time
    [ ] Limited join times (can only join on Sturday at 2pm!)
[ ] Get location queries implemented
    [ ] (local) Expanding bounding box query helper for suggestions
[ ] Get Async push notification queues working (copy from Vlllage)
[ ] HTTPS

# Public Groups

[ ] Fully async group ID in Group Activity

[ ] Tests
    [ ] Create public group
    [ ] Get public groups
    [ ] Get messages for public groups
    [ ] Suggestions are local
    [ ] Active phones are local

# Feature: Mute groups

[ ] POST /group?mute=true/false
    [ ] GroupMute model
        [ ] Use GroupMute for push notifications to omit phones
    [ ] Don't send notifications if mute token exists

[ ] App
    [ ] Mute group
        [ ] Bell/slashed-bell icon top right


# Presence

 [ ] Privacy policy - http://closer.vlllage.com/privacy
 [ ] Fancy marketing screenshots with written text on Play Store
 [ ] Closer launch party (once totally complete :) free wine fridays 2pm, cheese, give aways announce through closer

# Features

 [o] Search for places on the map (green button)

 [o] Single emoji floats many of them over your screen
    [o] Bubble to allow draw over screen
    [ ] Get screen wake lock working
 [o] Add public photos to suggestion
 [ ] Long-press add photo to map
 [b] All messages from person go to one notification box
 [ ] Profile -> Settings, Suggestions, More
 
 
# Enhancements

 [ ] HTTPS
 [ ] Some messages are duplicated
 [ ] Profile icon in status bar
 [.] Randomize message hint
 [ ] Don't show timestamps that are the same as the previous message
 [ ] Show no recent activity message
 [.] Don't allow direct messages if active=false
    [ ] Show dialog "X has already left the map"

 [o] Show "Bun is typing..."
 [o] WebSocket (exact same as push notifications, zero difference!)
 [o] Move pushes to background thread (server worker)
 [ ] Handle permanently denied contacts permissions
    [ ] Dialog telling to go to settings
 [ ] Only allow app to talk to backend (signature header)
 [ ] Bubble arrow is cut off when in landscape mode

# More

 [ ] "Network unavailable"
 [ ] Name null checks backend

# Group Ideas

 [ ] Your Sexy Photos
 [ ] (Idea) All photographers photograph you at the same time, improve your portfolio -> validate for 5 markets
 
 # $ Ideas
 
  [ ] Sell API access for public groups / map
  [ ] Sell temporary map bubbles
  [ ] Sell push notifications
  [ ] ???
  
# Arcchive

 [ ] Mute notifications for 12 hours
 [ ] Show call button if phone number is available
 [ ] Expiring suggestions, 5/6 messages, yellow dot
 [ ] Closer beta tester parties (as a thank you for testing)
 [ ] Show white dot when unread messages in group
 [ ] Fun events exclusive to Closer users
    [ ] Offer normally-not-free things to Closer users
    [ ] Subscribe to event channels (dancing, artsy, act)
        [ ] Channels
        [ ] Red bubbles
        [ ] Auto-added to all, unsubscribe from any/all