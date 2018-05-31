# Blue Connect ([Check out our website!](https://blue-buddy.squarespace.com/?r=66221890))

This app is a final project for CS290-03 Mobile Software Development. The main application Coconut is a mobile app supported by [Firebase backend](https://firebase.google.com/). User authentication is supported by Firebase Facebook Authentication API, and Google Maps API was used to support user/meeting location display and updates.


## Project Structure:

Everything you are looking to modify can be found in Coconut/app/src/main directory.





###   Activities:

  This package comprises of all the basic activities, including Main, Login, and CreateEvent.

###   Adapters:

  All the adapters used in the app, including FeedUpdate, Pager, and ProfileHistory.

###   Fragments:

  Main UI for the application, which are three fragments that correspond to three tabs users see when they sign in to the application: feed, map, and profile.

###   Interfaces:

  All the interfaces in the application are primarily listeners that handles the interactions between the database and the UI.

###   Models:

  Model classes comprise of data models for the User objects, Meeting objects, and the DukeLocation objects that are used to either interact with the database, or flow appropriate data to the UI.

###   Utils:

  Utility package holds two manager classes, DatabaseManager and LocationManager, that contain major business logic for the app dealing with the Firebase database interactions and the location updates in the mapTab.


# blue-buddy-connect
