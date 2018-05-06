# Incoming

 [ ] 
 [ ] 
 [ ] 
 [ ] 
 [ ] 

# Potential Directions

POST EVENT
==========

App:
 [X] Add "Add event here..." option to long-press menu at the top
    [X] Say what's up
    [X] Set event starting and ending time
    [X] Set event cover price (optional)
    [X] POST EVENT button
 [X] APIs
    [ ] Offline first support
    [X] Center map on newly created event
 [.] Load events on map move
 [X] Show event bubbles (red)
    [X] Add support for red color to MapBubble
    [X] Message, time
 
 [.] Tap on event bubble
 [.] Share event -> Share with...
 [.] Open event chat
    [ ] Show HOST next to the event host in messages

 [ ] Push events briefing every morning (8:30am)
    [ ] JobScheduler check events at specified time
 [ ] Red arrows pointing towards events off-screen (2 mi)
 [ ] If creator is me: Cancel this event option

Backend:
 [.] POST /event?name=&geo=&about=&startsAt=&endsAt=
 [.] GET /event?geo=l,l (20 nearest, incl cancelled)
 [.] Auto-create event group "event:1456"
 [ ] POST /event/1234?cancel=true

 [ ] Release today

# Scratchpad

# More Goodies

### P0 ###

### P1 ###

 [o] P0 App Feedback bubble
 [o] P1 (Polish) Set map margin top no status bar
 [o] P1 (Polish) Use suggested name on number verified
 [o] P1 (Polish) Hide inviting when searching contacts
 [ ] P1 (Polish) Share photos (in chats)
 [ ] P1 (Polish) Don't reactivate bubble when using push notification
 [ ] P1 (Polish) Use "No name" always in place of "Unknown" or "null"
 [o] P1 (Polish) Fully async group ID in Group Activity (gray before loaded, "loading..." title)
 [o] P1 (Polish) HTTPS
 [o] P1 (Polish) Some messages/groups are duplicated
 [o] P1 (Polish) Randomize message hint
 [o] P1 (Polish) Don't allow direct messages if active=false ("X has already left the map")
 [ ] P1 (Polish) Don't show timestamps that are the same as the previous message
 [ ] P1 (Polish) Show no recent activity message
 [ ] P1 (Polish) Only allow app to talk to backend (signature header, support multiple header values) X-CLOSER-SIGNATURE
 [ ] P1 (Polish) "Network unavailable"
 [ ] P1 (Polish) Name null checks backend
 [o] P1 (Polish) Show "Bun is typing..."
 [o] P1 (Polish) WebSocket (exact same as push notifications, ZERO difference!)
 [ ] P1 (Polish) Handle permanently denied contacts permissions (Dialog telling to go to settings)
 [ ] P1 (Polish) Settings bubble at end pink [Set event notification time]
 [ ] P1 (Polish) Settings show privacy policy (/static/privacy)

### P2 ###

 [ ] P2 (Polish) Show inactive contacts (haven't seen any activity in >1 month)
 [b] P2 (Polish) All messages from person go to one notification box
 [o] P2 (Polish) Search for places on the map (green button)
 [ ] P2 (Polish) Get Async push notification queues working (copy from Vlllage)
 [ ] P2 (Feature) Add photo / video circles to map tap to fly into
 [o] P2 (Feature) Pinned public groups
 [o] P2 (Feature) Chats on map (Open chat here...) -> Opens group messages, anyone can drop in from bub
 [o] P2 (Feature) Add your name bubble
 [ ] P2 (Feature) Share photo to group (tap on group from menu)
    [ ] Tap on photos to view all shared to group
        [ ] Share icon on photo

### P3 ###

 [o] P3 (Feature) Single emoji floats many of them over your screen
    [o] Bubble to allow draw over screen
 [o] P3 (Feature) Add public photos to suggestion
    [ ] Get screen wake lock working
 [ ] P3 (Feature) Photos on map (when zoomed in, dots when zoomed out)
 [ ] P3 (Feature) Camera icon appears on map to add photo (when zoomed in)
 [o] P3 (Feature) Set profile photo

#
# Advertise Closer in SF and NY and LA and CO (NOT Austin!! :P)                  <-- !!
#

[.] Localized screenshots (NY, LA, SF)
[ ] "Pulses" of invites so masses of people join at the exact same time
    [ ] Limited join times (can only join on Saturday at 2pm!)
[ ] Get location queries implemented
    [ ] (local) Expanding bounding box query helper for suggestions

# Feature: Mute groups

[ ] P3 (Feature) POST /group?mute=true/false
    [ ] GroupMute model
        [ ] Use GroupMute for push notifications to omit phones
    [ ] Don't send notifications if mute token exists
    [ ] Mute group
        [ ] Bell/slashed-bell icon top right

# Presence

 [ ] P1 (Release) Privacy policy - http://closer.vlllage.com/privacy
 [ ] P2 (Release) Fancy marketing screenshots with written text on Play Store
 [ ] P2 (Release) Closer launch party (once totally complete :) free wine fridays 2pm, cheese, give aways announce through closer
 [ ] P2 (Release) Closer infatuation parties

 # $ Ideas
 
 https://developers.google.com/pay/api/tutorial
 
  [ ] Sell API access for public groups / map
  [ ] Sell temporary map bubbles
  [ ] Sell push notifications
  [ ] ???
  
# MKTG  
 [ ] Do Something Different :) -- square cards in coffee shop bathrooms
 [ ] Closer flip books (super cute stories, app at end)

# Arcchive

 [ ] NFC tags to open chats about a location
    [ ] Chat about this tree, download Closer and tap here with NFC enabled.
 [ ] social points for doing things, you can collect on your own to

 [ ] Revive ghosts / alex_b tried to revive your ghost / chase ghosts
 [ ] Public things to do
    [ ] Take a photo with the dude rock
    [ ] 43 people did this
    [ ] Show photos

 [ ] Create a check-in base
    [ ] Bases appear when you're near them
 [ ] Check-in with photos
 
 [ ] Share photos to 
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

 [ ] Only the best events "most fun"

$10 = 1-2 bubbles nearby
$20 = 3-4 bubble nearby
$30 = 5-6 bubble nearby
$40 = 7-8 bubble nearby
$50 = 9-10 bubble nearby

 [ ] Admins finds all cool events and adds them

Bubbles last for 24h
Buy from start-stop


 [ ] (Profile) Intro app just name face hello text / Overlord

 [ ] (Event) (Learn) Learn Something Together / entrances to journeys
 [ ] (Event) (Learn) Run classes for programming weekend slime quest, village, etc
 [ ] (Event) (Pay) Host themed house parties / get 10 / Free / $10 / $20
 [ ] (Event) (Pay) Become a member purple bubble / get more
 [ ] (Event) (Pay) Get paid to host events
 [ ] (Event) (Fun) Exotic parties
 [ ] (Event) (Fun) Introduction parties
 [ ] (Event) (Fun) 3 events per day
 [ ] (Event) (Fun) there's 3 events today
 [ ] (Event) (Fun) Level 1 2 3 curated events paid hosts
 [ ] (Event) (Fun) Closer automatic events, you can go if you want / # people interested / random cues
 [ ] (Event) (Fun) Passes to private events / areas (unlock new areas)
 [ ] (Event) (Fun) Condor (unlock all events in Austin)

 [ ] (Quest) 4 Quests starting today (purple bubble to show all on map pops down from top)
 [ ] (Quest) Starting Point
 [ ] (Quest) Name & About Text
 [ ] (Quest) Starting Time
 [ ] (Quest) Group Chat
 [ ] (Quest) Hosted by 3 people
 [ ] (Quest) QPs Quest Points
 [ ] (Quest) Get quest medal after successful (Level 3 adventurer)
 [ ] (Quest) 3 days, 2 hours remaining to complete this quest
 [ ] (Quest) Clear beginning, clear ending, clear duration, clear difficulty, clear group size

 [ ] (Feature) Discoveries (bubbles on map)

 [ ] (Plan) plans (public, group) Jacob wants to go to sushi
 [ ] (Plan) Longpress -> Make a plan, share with groups
    [ ] 33 minutes left 8 spots left
 [ ] (Challenge) Post challenge video
 [ ] (Challenge) Challenges and profiles and high score boards
