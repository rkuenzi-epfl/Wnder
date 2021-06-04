# Wnder
Wnder android app
The Wnder app is an app that proposes pictures of your region to you so you can guess them by clicking or 
walking to the picture location. You can also guess tours that are series of pictures. You can also upload 
you own pictures and tours.

This repository contains all the files necessary to the Wnder app except 2 that contain secret keys.
To make the app work one must add those two files locally:

- gradle.properties
- /app/src/main/res/values/secretStrings.xml

Once that is done you should be able to launch and fully use the Wnder app.

Pay attention, on some devices, the app may crash upon GPS services deactivation, so make sure you ALWAYS
have your GPS activated when using Wnder - if you want to avoid bad surprises.
