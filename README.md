Sync Adapter Demo
=================

This application is meant to be an introduction into sync adapters. It involves
authenticating with a server, creating an account object, creating a content
provider, and setting up a sync adapter to sync necessary data.

The server the application communicates with is:

  * http://syncadapterdemo.herokuapp.com/

You must have an account on this site in order to log into the application and
use the book errata data. Once logged in the app will download all of the errata
data to the device. You can edit any erratum you created on the website, and
delete them as well.

The security for this application is very basic. When the app authenticates with
the server it gets an access token that must be sent with any update or delete
requests in order to validate that the user can modify the book erratum data.

The syncing process could also be improved. Currently when the app syncs it will
push any updates made locally, as well as deleting any records from the server
that were deleted locally. Once all the data has been pushed up it will then
pull down the most recent user data, then pull the most recent book errata data.

To see the database columns look at the ContentProviderContract in the
contentprovider package.

