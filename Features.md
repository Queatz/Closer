
# Overview

 * Profiles
 * Post videos, audio, files
 * See recent messages
 * Public group cards
 * STOP the doubling

---

Make a Closer Profile

 * Show everyone in the world what's up with you
 * Join the influencer community and build a following
 * Keep in touch with people you've met directly

---------------------------------------------

### Media *********
 [ ] (LG) Closer raw files content server
 [ ] (LG) Select Video
    [ ] Video attachment type
 [ ] (LG) Select Audio
    [ ] Audio attachment type
 [ ] (LG) Select File
    [ ] File attachment type
 [ ] Zoom in on photo

## Feed & Profile **********

 [ ] (LG) View Direct Messages History (recent people + DMs)

 [ ] (LG) Profiles
     [ ] Add closer members into private groups from in closer not phone number
     [ ] Profile Feed
        [ ] Public Groups they're in
        [ ] Public posts
     [ ] Photo
     [ ] Groups for each post where people can chat about them
        [ ] Mini comment-like chat box
    [ ] Notifications on/off for person

 [ ] (SM) Set your name ongoing bubble
    [ ] Make TemporaryBubbleHandler
        [ ] Animate bubble changes

## Public Groups **********
 [ ] (LG) Public Groups
    [ ] Cover Photos
    [ ] Join Group
    [ ] Join -> Joined
    [ ] See group members button
    [ ] Members list
[ ] (LG) @mentions in group messages
    [ ] Popup of people (top left above edittext)
    [ ] Sends that person a notification no matter what (pull someone directly into a conversation)
[ ] (MD) Pinned messages in events and groups
[ ] (MD) Notification Settings
    [ ] Notifications toggleable on/off for groups / area talk

## Itty-bitty **
 [ ] (SM) Icons in map tap minimenu [ directions | share ]
 [ ] (SM) Set event map icon (phone-number-verified users)

## Infrastructure *****
 [ ] (MD) HTTPS x2
    [ ] closer.vlllage.com
        [ ] Set up apache
        [ ] Set up tomcat
        [ ] Run certbot
        [ ] Update app
    [ ] closer-files.vlllage.com
        [ ] Set up apache
        [ ] Set up tomcat
        [ ] Run certbot
        [ ] Update app
 [ ] (MD) Closer server app registry
    [ ] X-CLOSER-APP-ID
 [ ] (SM) Verification code can only be attempted 3 times before it gets deleted
    [ ] Dialog explaining this on 3rd attempt
 [ ] (MD) Solve declared dependencies problem (PoolMember)

# Bugs ***********************
 [ ] (XS) Change: Tap on my bubble to edit what I say, not name

 [ ] (SM) Area Talk: Preserve talk box location on screen
 [ ] (SM) Area Talk: Chat max width

 [ ] (SM) Don't ask for location right away
 [ ] (SM) Show Use my location button when location permission not granted

 [ ] (SM) Fix contacts not showing up on Android 7.0
 [ ] (SM) Fix event not syncing when created offline
 [ ] (SM) Don't reactivate people's bubbles when they receive a push notification

 [ ] (MD) Local Area Talk Notifications (People are talking nearby -> opens Closer with area chat centered)
 [ ] (MD) Offline First Doubles
    [ ] expose luid until client requests removal of luid on server
 [ ] (MD) Use aspect/placeholder (save it on photo group message attachment)
 [ ] (MD) Auto update group when setting group name background upon success
 [ ] (MD) Re-add to groups of old account when verifying number with other accounts with that number
 [ ] (MD) Stop event bubble from burbbling upon creation!

 [ ] (MD) Pay button in events that cost $ -> Opens Google Pay Intent
    [ ] Enter your email to get paied
 [ ] (MD) Dowload actions on photos and videos


### How Jacob will use Closer

Jacob will run a few public groups on Closer, including:

 - Jacob's Events on Closer
 - Miata Friends
 - Huawei Fandroids

Join Closer, Austin's Adventure & Social Club

 - Free flute lessons!
 - Free Angular bootcamp!
 - Free Android bootcamps!
 - Free hikes!
 - Free photoshoots!
 - And much, MUCH more!  Come check us out!

 :::: Closerapp.us / austinsocial.club -> splash page with people and Closer on HUAWEI phone / get in on Google Play badge
    -> Click anywhere to open in Play Store


# Please Consider

    # Range slider on area chat (updates chat in real time)

    ## Friends

     [ ] Tap on person / group message to view profile

     [ ] RL Friends
        [ ] See my RL Friends on map
            [ ] Set areas where I am visible to my RL Friends

    ## Physical Place Discovery

     [ ] Search for places
        [ ] Google Places API

     [ ] Add drop in video mode to closer

     [ ] Closer show dead humans, revive button
        [ ] Pump in energy

    # Web
    [ ] Closer profile closerapp.us/123456


    Photo contest with rewards

    Feed = notifications, incl. Direct Messages

    [ ] (LG) Feed
        [ ] Dynamic content
            [ ] Groups you might like
            [ ] Recent photos from people's posts
            [ ] Recent posts (random order)
            [ ] Events nearby
            [ ] Suggestions nearby
