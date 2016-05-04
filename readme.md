#Milknote

##About
This app was created for the final project of the Udacity Android Nanodegree. It utilizes that Nuance Speechkit to provide transcription of speech. These are presented in a gmail style interface for editing and managing.

##Setup
You will need a Nuance Developer account to test this functionality. Sign up at: 

https://developer.nuance.com/public/index.php?task=memberServices

Once signed up navigate to Home > My Account > Sandbox Credentials

Add the Speechkit 2.x values to you gradle.properties file:

NUANCE_APP_KEY = "YOUR_APP_KEY";
NUANCE_APP_ID = "YOUR_APP_ID";
NUANCE_SERVER_HOST = "YOUR_SSL_HOST";
NUANCE_SERVER_PORT = "YOUR_SSL_PORT";