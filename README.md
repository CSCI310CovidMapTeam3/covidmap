# Map Covid Team 3

Map Covid is an Android app that tracks Covid information for 5 cities arround Los Angeles.

## Requirement

Android Studio v4.1.2+

## Before you start
On the Tool Bar of Android Studio click File -> Open to open the project in Android Studio.

Goto File -> Settings -> Appearence & Behavior -> System Settings -> Android SDK  
Switch to the SDK Tools bar and enable all Google Play related tools. 

All dependencies are listed in the app level build.gradle file and should be automaticly resolved.

In file "local.properties", add the google maps API key.
```java
MAPS_API_KEY=AIzaSyDjhmz8ZWJUrCnU_DveuFKhkL2nXHWMQHM
```

At Android emulator sidebar, click "...", go to "Location", set location to Los Angeles.

## Home
5 Markers represent 5 cities.

Colors of the markers change with case numbers.

Click on the markers to show info windows.

Default location of the user is Los Angeles.
## News Feed
Shows the Twitter page of Daily Trojan COVID-19 Updates.
## Test Location
Markers represent locations of test stations.

Click on each marker to show the name and precise address of that test center.
## Tracking
Click on any date on the calendar to show entire tracking history on that date.
## Setting
About: Shows the basic information of our app and functions of each tab.

Clear all data: Clears all user history data.

Selected location: The marker on the home page will be highlighted if you choose that location.

Notification: turns on or off notification.
