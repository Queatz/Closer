
# Feedback From Users

 [ ] Has has has to feel private
 [ ] Private party (Private mode?)

# Top

 [ ] Post photos and videos
    [x] closer-files.vlllage.com (https)
        [ ] HTTPS
 [ ] Purple dots
 [ ] Mini-games (challenges, one-off games) [NSFW]

 [ ] New group params
    [ ] /group/?physical=true
    [ ] /group/?hub=true
 [ ] New UI
    [ ] New bubble types
        [ ] Purple dots (12dp @ 13z)
        [ ] Image dots (24dp @ 13z)
    [ ] Purple backgrounds (physical=true)
    [ ] Geo chat background
    [ ] Geo chat settings (3rd icon)
        [ ] Pin group -> Set name
        [ ] Set photo
    [ ] Upload photos "photo" attr
    [ ] Photo group message attachment
    [ ] Zoom photo view, slide left/right

# What I'm Seeing

 [ ] Chat here...
 [ ] Purple dots - chats on the map - most recent 20
    [ ] Load on map move and zoom >= 13
    [ ] Animate in / out (boop!)
    [ ] Push notifications to people < .5 miles
    [ ] Expires after 12 hours of inactivity
 [ ] chat has photo ? Photos on map - hangouts on map
    [ ] Pin group on map -> Add name -> Does not expire
    [ ] Show on map, share
    [ ] Settings ... icon - change background (enabled if number is verified)
    [ ] Post photos / videos in chat (a funny and a must :P)

# Named Areas / Locations on map (admins can edit) (detail size)

 [ ] Names on map

 : The Naked Woods
 : The Burping Tree
 : The Sleeping Forest
 : Trail of the haunted man
 : The Tree of Aurora Mae
 : The Spirit Garden

# Incoming

 [ ] Automatic Events (suggestions from the Closer AI itself)

### QuestIRL

 [ ] Event (criminal is on the loose)
 [ ] Actors
 [ ] Challenge Questions
 [ ] Locations
 [ ] Transit / Car / Actors

 [ ] Anyone can set one up

# Interview Process (for Jacob)

 [ ] Initial meet & greet
    [ ] What is one thing you're good at?
    [ ] What is one thing you want to do but haven't done yet?
    [ ] What is one thing you like doing with other people?

 [ ] For each answer, drive to that location and photo them there
    [ ] Create their profile at that location with their answer

# Sum

 [ ] Games
    [ ] Start here by 5pm, icon on map
    [ ]"Get to know meek park"
        [ ] Clues
        [ ] Names on map
 [ ] Scavenger Hunts
    [ ] How to Play
    [ ] Icons for locations (while game is active)
    [ ] Difficulty 1, 2, 3 stars
    [ ] Reward
 [ ] Local Area Groups
 [ ] Profiles of people
    [ ] Discovery
        [ ] From hubs / add your profile to places you frequent
        [ ] From local area chats

### P0 ###

 [o] P0 (Polish) Crash when location is unknown
 [o] P0.1 (Polish) After denying location, don't ask again
 [o] P0.1 (Polish) Contacts not showing up on Android 7.0
 [o] P0.1 (Polish) No network icon in app not toast! "You are offline"
 [o] P0.2 (Polish) HTTPS
 [ ] P0.2 (Polish) Only allow app to talk to backend (signature header, support multiple header values) X-CLOSER-APP-ID
 [0] P0.3 (Polish) Closer only cancel events not in the past
 [0] P0.3 (Polish) Tapping into message box when share is active in event cancels share
 [o] P0.4 (Polish) Some messages/groups are duplicated
 [o] P0.4 (Polish) Event bubble burbbles upon creation
 [ ] P0.4 (Polish) Don't reactivate bubble when using push notification
 [ ] P0.5 (Polish) Settings bubble at end pink [Set event notification time]
 [o] P0.5 (Polish) Randomize message hint
 [o] P0.9 (Feature) Add your name bubble

### P1 ###

 [ ] P1 (Photos) Add photo / video circles to map tap to fly into
 [ ] P1 (Photos) Share photos (in chats, camera icon on send button pre-text entered)
 [ ] P1 (Photos) Camera icon appears on map to add photo (when zoomed in in circle menu around your location)
 [ ] P1 (Photos) Photos on map (when zoomed in, dots when zoomed out)
 [ ] P1 (Photos) Share (multiple) photos to group from external app (tap on group from menu)
 [o] P1 (Photos) Add public photos to suggestion
    [ ] Tap on photos to view all shared to group
        [ ] Share icon on photo
 [o] P1 (Polish) WebSocket (exact same as push notifications, ZERO difference!)
 [o] P1 (Polish) Show "Bun is typing..."
    [.] JobScheduler check events at specified time

### P2 ###

 [b] P2 (Polish) All messages from person go to one notification box
 [o] P2 (Polish) Search for places on the map (green button... suggestions from google...)
 [ ] P2 (Polish) Get Async push notification queues working (copy from Vlllage)
 [o] P2 (Feature) Pinned public groups
 [o] P2 (Feature) Chats on map (Open chat here...) -> Opens group messages, anyone can drop in from hub
 [o] P2 (Feature) Set profile photo
 [o] P2 (Feature) Single emoji floats many of them over your screen
    [o] Bubble to allow draw over screen permission

### P3 ###

 [ ] Closer place search
     [ ] Thumbs up for verified number?
 [ ] Directions button in long press menu
 [ ] Android Auto
 [ ] Search history button, reveals things from the past here
 [ ] OSM MapView (toggleable)
 [ ] Closer Reminders (location, event, suggestion...)

# Incoming - Needs Review

 [ ] find your equals / ai engine app
 [.] Photos of people in Austin blog / things they like to do
 [.] Long press menu directions, share, save, remind, camera, remind
 [ ] Closer home work on map icons on map
 [ ] Closer friends on map
 [ ] Closer weekly/monthly holidays
 [ ] Groups in groups? Or group domains? Layers? I.e. dev squads for X number of projects
 [ ] Closer search bar at top
 [ ] Closer become an ambassador in your area (things to do as an ambassador)
 [ ] Closer enter invite code to join secret group
 [ ] Goodies.  That pop up on the map when you're near by them

 [o] Private Group Invites on map when zoomed in past satallite view
    [ ] Revival chat
    [ ] Revival nerds
    [ ] The Bonnevile House
        -5109 Bonneville Bend-
    [ ] Send notification to people right here
    [ ] Messages expire after 1h
 [ ] Purple events - private to some of your groups

#
# Advertise Closer in SF and NY and LA and CO (NOT Austin!! :P)                  <-- !!
#

[.] Localized screenshots (NY, LA, SF)
[ ] "Pulses" of invites so masses of people join at the exact same time
    [ ] Limited join times (can only join on Saturday at 2pm!)

# MKTG: Marketing Feature Roles
 [ ] Market with free tacos / market closer as a temporary app download / download closer on October 25th treasure box
 [ ] Roles: Suggestion finder, event hoster, etc

# Feature: Mute groups

[ ] P3 (Feature) POST /group?mute=true/false
    [ ] GroupMute model
        [ ] Use GroupMute for push notifications to omit phones
    [ ] Don't send notifications if mute token exists
    [ ] Mute group
        [ ] Bell/slashed-bell icon top right

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

# What isi z

 [ ] The Closer Austin Launch Squad
  - host events
  - engage with users
  - generate random activity



# MKTG # The Truth # MKTG#

 [ ] (The Influecner) Run free classes

 "I'm hosting free Android & Angular classes in my garage for Closer users"

  - Android
  - Angular 6
  - Flute
  - Confidante Training

 [ ] (The Stickers) Laptop, Mazda 3, Miata, Around town

  - Just do it

 [ ] (The Free Tacos)

 Free tacos when you install the Closer app

 [ ] (The Event Games)

 TODAY ONLY - find mermaids on E 6th from 9-11pm with the Closer app!
    involves real people
    FOUND MERMAID - enter her name
    hide and seek feel
    shows area on the map
    shows key location icons on map if you are playing
    answer questions about people

 Start a game - nerds of zilker - get all their names - name games

 GO! You have 2 hours to find all 6 mermaids!
    See mermaids on map
    See area of game on map
    Each mermaid has a challenge for you / your group

 Winners at the end of the event

 Yellow color

 [ ] SH
    [ ] What happened to Albert?

 [ ] (???)

     [ ] Add your profile here
         [ ] Picture with words over it, "first name, says..."
     [ ] See all profiles here
         [ ] Swipe left / right
     [ ] You've found 2/5 of Jacob's profiles.  Keep exploring the map to find them all!
    [ ] Geo chat profiles
        [ ] Profile cards
            [ ] Profile full zoom view (cross activity animation)
        [ ] Add profile placeholder layout
        [ ] Remove profile