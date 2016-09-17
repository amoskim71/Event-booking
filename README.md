# Let's Meet Here
An android application for creating events and sharing them with others.

# Description
Register a new account and log in. 
Create a new event and click on the map to set the location. Adjust the name, date and time as needed. You can always edit your events later, any changes will be synchronized with all participants.

<img src="https://github.com/gacsoft/letsmeethere/images/map.png" width="500">

Click Invite then click on the contacts you wish to add, or enter their email address. You can add people who have not yet registered, they will still get the invitation once they sign up and log in. 

<img src="https://github.com/gacsoft/letsmeethere/images/invite.png" width="500">

Upcoming events are displayed on the main menu, but you can switch to the Past Events tab if you need to look up a past event for reference.  

<img src="https://github.com/gacsoft/letsmeethere/images/main.png" width="500">

# Backend
Uses PHP with MySQL database for maximum compatibility. PHP files included. 

# Installing
1. You will need the <a href="https://android.googlesource.com/platform/frameworks/volley/">volley library</a> to compile.<br>
2. Edit <i>php/include/Config.php</i> and fill in your database details.<br>
3. Edit <i>app/src/main/res/values/api_keys.xml</i> and enter your Google Maps API key. (If you don't have one, you can <a href="https://developers.google.com/maps/documentation/javascript/get-api-key">get one for free here</a>).<br>
4. Execute <i>sql/init.sql</i> to set up your database tables.

# License
Distributed under MIT License, see the LICENSE file for details.