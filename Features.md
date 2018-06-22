
---------------------------------------------

Explain how I would use Closer for 25 different scenarios - then make videos

 [ ] Web page with events near austin sourced from Closer API
    [ ] austinsocial.club
    [ ] Heart it button on events

---------------------------------------------


## Interactions

 [.] Right screen
    [.] (TOP) full history of direct messages
    [.] (TOP) open direct messages with that person

 [.] (TOP) Backend
    [ ] Create GroupAction datamodel
    [ ] Return in group response
    [ ] POST /group-action
    [ ] GET /messages (my messages)
    [ ] Push notifications for group action attachment

 [ ] Tap on Group Action
    [ ] Write a message
    [ ] Post

 [ ] Hide Group Actions when selecting contacts
    [ ] Create HideGroup obj .showAll() .hideAll()

 [ ] Order group actions by use
        Store hits on backend, respond with them in priority order
        Save position on frontend, order by position
        Delete group actions for that group that don't come back

 [ ] Group action group message attachment
    [ ] Reply to Daisy button
        [ ] Opens direct message to Daisy
    [ ] For poster: X to end


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

 [ ] (MD) Solve declared dependencies confusion problem (PoolMember)

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

     [ ] (MD) Pay button in events that cost $ -> Opens Google Pay Intent
        [ ] Enter your email to get payed

