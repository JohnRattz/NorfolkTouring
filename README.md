# NorfolkTouring
NorfolkTouring is an Android application that presents various locations of 
interest in Norfolk, Virginia.

Its primary purpose is to showcase various features of Android - including RecyclerViews,
DrawerLayouts, Adapters, Fragments (extensively used), click listeners, AsyncTasks, 
Handlers (used for automatic image cycling), Services, MediaSessions 
(including MediaStyle notifications that allow control of media playback), Widgets, and more.

Some APIs used in this project include the Google Maps API 
(both for Web and Android - using both URI queries and the Android API),
the Google Places API (again, using both URI queries and the Android API),
the YouTube Android Player API, and the Volley networking library.

UI tests were conducted with the Espresso testing framework.
These tests can be found in *app/src/androidTest*.

<h3>Executive Overview</h3>

The application opens with an introductory video and navigation instructions.
There is a drawer on the left that may be swiped open from the left side of 
the screen to the right side.

Clicking on any of the drawer elements will open a list of tour locations 
(populated by an Adapter) - displaying information such as the location name, 
rating (from Google reviews), current open status, and distance (updated in real-time). 

It is also possible to open a view of the locations on a map by clicking on the views 
with the text "Google Maps View". Directions to these locations can be obtained 
by clicking on the Views with the text "Plan Route" 
(this opens the user's preferred map application with an implicit Intent).

Clicking on the tour location view will open a detailed view of that location.
This view shows additional information, such as the operating hours, website,
address, contact information, and a description for the corresponding location.

<h3>Description of Software Components</h3>
<h4>Main Components</h4>

`NorfolkTouring`: Used to acquire the application instance in a more reliable way than
through `getApplicationContext()`, defines some API keys, and provides functions for 
setting the `ActionBar` title. 

`MainActivity`: Initializes a Google API Client, contains and initializes 
the Navigation Drawer (a `DrawerLayout`), and launches the `IntroductoryFragment`.

`IntroductoryFragment`: Contains and initializes a `YouTubePlayerFragment` and the
introductory text. Contains a `MediaSession` (though this is often part of a containing `Activity`
rather than a `Fragment`) that allows a YouTube video player (`YouTubePlayer`) to be controlled by 
a MediaStyle notification (used by media playback applications such as Spotify).

`TourLocation:` All information regarding a tour location is stored in a object of this type.

`TourLocation.LocationFeature`: A feature for a `TourLocation`. Has a name, description, and images.

`TourLocationListFragment/TourLocationListFragment`: The description of a list of
`TourLocation` objects. Subclasses only define the `TourLocation` objects that are to be displayed.
The list is a `RecyclerView` rather than a `ListView` to only store visible views in memory.

`TourLocationDetailFragment`: The detailed description of a `TourLocation`. Cycles resource images
and Google images (obtained asynchronously through `Utils/PlacesUtils`). Allows resource images
and Google images to be viewed in an enlarged view that allows manual cycling of images 
in forward and reverse with arrows on both sides of the screen. Features may be expanded and 
collapsed with animated views by clicking on them. Feature images are also automatically cycled. 
All image cycling stops and starts with the lifecycle of the `TourLocationDetailFragment`. 

<h4>Click Listeners</h4>

`MainActivity.DrawerItemClickListener`: Replaces the main view with the `Fragment` (a `TourLocationListFragment`) 
corresponding to a clicked drawer item. The `Fragment` launched will be one of the following: 
`MilitaryFragment`, `MuseumsFragment`, `OtherFragment`, `ParksFragment`, or `RestaurantsFragment`.

`TourLocationListFragment.TourLocationClickListener`: Replaces the main view with a `TourLocationDetailFragment` displaying
more information than the `TourLocationListFragment`.

`NavigationIconClickListeners.DirectionsIconClickListener`: Uses an implicit `Intent` to launch an application that plots a path
between the device's location and the location of the corresponding `TourLocation`.

`NavigationIconClickListeners.MapIconClickListener`: Opens a Google Maps view, places a marker at the location of the 
corresponding `TourLocation`, and zooms to it. Also allows navigation to the device location 
with a button.

<h4>Adapters</h4>

`TourLocationListFragment.TourLocationAdapter`: Used by subclasses of `TourLocationListFragment` to fill a list of 
`TourLocation` objects.

<h4>Services</h4>

`LocationService`: A `Service` subclass for obtaining location updates (serves as a singleton). 
The `LocationService` stops and starts with the `Activity` lifecycle, which includes stopping and 
restarting location updates (e.g. location updates stop being requested when the screen is 
turned off). Uses the Fused Location Provider API.

<h4>Utilities</h4>

`Utils/PlacesUtils`: Records Place IDs in the Google Places API for the locations presented 
in the application. Allows retrieval of images and information for locations via `AsyncTask` 
subclasses also defined in `Utils`.

`Utils/PhotoTask`: Retrieves 10 photos for a location using the Google Places API for Android.

`Utils/AttributedPhoto`: Displaying the photos retrieved via the Google Places API requires 
photo attribution. This class contains an image and its attribution.

`Utils/LocationsByIdsTask`: Not used, but shows how to retrieve latitude and longitude for a place
from the Google Places API for Android (no URI queries here).

`Utils/InfoByIdsTask`: Acquires the location (latitude and longitude), hours of operation, 
rating (Google reviews), and the website for `TourLocation` objects using URI queries and `Volley`.

<h4>Widgets</h4>

`Widget/WidgetProvider`: Draws and updates widgets based on their options (e.g. height and width).
Smaller widgets just have the application background. Larger widgets also have a `GridView`.

`Widget/GridWidgetService`: Populates a larger widget's `GridView` with the categories 
of tour locations. Clicking on a `TextView` item in this `GridView` will open the application
to that category of tour location.

<h4>Other</h4>

`VolleyRequestQueue`: A singleton for a `Volley` `RequestQueue` (used for networking). 
Used in URI queries in `Utils/DirectionsIconClickListener` and `Utils/InfoByIdsTask`. 